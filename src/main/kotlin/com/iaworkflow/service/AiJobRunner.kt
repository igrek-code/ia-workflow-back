package com.iaworkflow.service

import com.iaworkflow.ai.TechnicalCardGeneratorAgent
import com.iaworkflow.ai.UserStoryGeneratorAgent
import com.iaworkflow.ai.UserStoryRectifierAgent
import com.iaworkflow.domain.Auteur
import com.iaworkflow.domain.CarteTechnique
import com.iaworkflow.domain.Commentaire
import com.iaworkflow.domain.CompteRenduRepository
import com.iaworkflow.domain.GenerationStatut
import com.iaworkflow.domain.UserStory
import com.iaworkflow.domain.UserStoryRepository
import com.iaworkflow.domain.UserStoryStatut
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

/**
 * Exécution asynchrone des appels IA (threads virtuels).
 *
 * Principe : aucun appel LLM ne tient de transaction ouverte — les lectures et
 * les écritures en base se font dans des transactions courtes, avant et après
 * l'appel à l'agent. Tout échec est capturé et enregistré sur la ressource
 * (statut ECHEC + message), jamais relancé : personne n'écoute un void asynchrone.
 */
@Service
class AiJobRunner(
    private val compteRenduRepository: CompteRenduRepository,
    private val userStoryRepository: UserStoryRepository,
    private val userStoryGeneratorAgent: UserStoryGeneratorAgent,
    private val userStoryRectifierAgent: UserStoryRectifierAgent,
    private val technicalCardGeneratorAgent: TechnicalCardGeneratorAgent,
    private val transactionTemplate: TransactionTemplate,
) {

    @Async
    fun genererUserStoriesAsync(compteRenduId: Long) {
        val contenu = transactionTemplate.execute {
            compteRenduRepository.findById(compteRenduId).orElse(null)?.contenu
        }
        if (contenu == null) {
            log.warn("Génération abandonnée : compte rendu {} introuvable", compteRenduId)
            return
        }

        try {
            val generees = userStoryGeneratorAgent.generer(contenu)
            transactionTemplate.execute {
                val compteRendu = compteRenduRepository.findById(compteRenduId).orElse(null)
                    ?: return@execute
                generees.forEach { gen ->
                    userStoryRepository.save(
                        UserStory(
                            titre = gen.titre,
                            description = gen.description,
                            criteres = gen.criteres.joinToString("\n"),
                            statut = UserStoryStatut.EN_ATTENTE_VALIDATION,
                            version = 1,
                            compteRendu = compteRendu,
                        )
                    )
                }
                compteRendu.generationStatut = GenerationStatut.TERMINEE
                compteRendu.erreurGeneration = null
                compteRenduRepository.save(compteRendu)
            }
            log.info("Génération des user stories du compte rendu {} terminée : {} stories", compteRenduId, generees.size)
        } catch (e: Exception) {
            log.error("Échec de la génération des user stories du compte rendu {}", compteRenduId, e)
            transactionTemplate.execute {
                compteRenduRepository.findById(compteRenduId).orElse(null)?.let { compteRendu ->
                    compteRendu.generationStatut = GenerationStatut.ECHEC
                    compteRendu.erreurGeneration = e.message?.take(500)
                    compteRenduRepository.save(compteRendu)
                }
            }
        }
    }

    @Async
    fun rectifierAsync(userStoryId: Long) {
        val donnees = transactionTemplate.execute {
            val userStory = userStoryRepository.findById(userStoryId).orElse(null)
                ?: return@execute null
            val commentairesPo = userStory.commentaires
                .filter { it.auteur == Auteur.PO }
                .map { it.contenu }
            RectificationData(userStory, commentairesPo)
        }
        if (donnees == null) {
            log.warn("Rectification abandonnée : user story {} introuvable", userStoryId)
            return
        }
        if (donnees.commentairesPo.isEmpty()) {
            log.warn("Rectification abandonnée : user story {} sans commentaire PO", userStoryId)
            retablirStatutRectification(userStoryId, "Aucun commentaire du PO à traiter")
            return
        }

        try {
            val rectifiee = userStoryRectifierAgent.rectifier(donnees.userStory, donnees.commentairesPo)
            transactionTemplate.execute {
                val userStory = userStoryRepository.findById(userStoryId).orElse(null)
                    ?: return@execute
                if (userStory.statut != UserStoryStatut.RECTIFICATION_EN_COURS) {
                    log.warn(
                        "Rectification de la user story {} non appliquée : statut devenu {}",
                        userStoryId, userStory.statut,
                    )
                    return@execute
                }
                userStory.titre = rectifiee.titre
                userStory.description = rectifiee.description
                userStory.criteres = rectifiee.criteres.joinToString("\n")
                userStory.version += 1
                userStory.statut = UserStoryStatut.EN_ATTENTE_VALIDATION
                userStory.erreurGeneration = null
                userStory.commentaires.add(
                    Commentaire(
                        auteur = Auteur.AGENT,
                        contenu = "User story rectifiée (version ${userStory.version}) et remise en attente de validation du PO.",
                        userStory = userStory,
                    )
                )
                userStoryRepository.save(userStory)
            }
            log.info("Rectification de la user story {} terminée", userStoryId)
        } catch (e: Exception) {
            log.error("Échec de la rectification de la user story {}", userStoryId, e)
            transactionTemplate.execute {
                userStoryRepository.findById(userStoryId).orElse(null)?.let { userStory ->
                    if (userStory.statut == UserStoryStatut.RECTIFICATION_EN_COURS) {
                        userStory.statut = UserStoryStatut.A_RECTIFIER
                        userStory.erreurGeneration = e.message?.take(500)
                        userStoryRepository.save(userStory)
                    }
                }
            }
        }
    }

    @Async
    fun genererCartesAsync(userStoryId: Long) {
        val userStory = transactionTemplate.execute {
            userStoryRepository.findById(userStoryId).orElse(null)
        }
        if (userStory == null) {
            log.warn("Génération des cartes abandonnée : user story {} introuvable", userStoryId)
            return
        }

        try {
            val cartes = technicalCardGeneratorAgent.generer(userStory)
            transactionTemplate.execute {
                val us = userStoryRepository.findById(userStoryId).orElse(null)
                    ?: return@execute
                if (us.cartesStatut != GenerationStatut.EN_COURS) {
                    log.warn(
                        "Génération des cartes de la user story {} non appliquée : statut devenu {}",
                        userStoryId, us.cartesStatut,
                    )
                    return@execute
                }
                cartes.forEach { gen ->
                    us.cartesTechniques.add(
                        CarteTechnique(
                            titre = gen.titre,
                            description = gen.description,
                            taches = gen.taches.joinToString("\n"),
                            userStory = us,
                        )
                    )
                }
                us.cartesStatut = GenerationStatut.TERMINEE
                us.erreurGeneration = null
                userStoryRepository.save(us)
            }
            log.info("Génération des cartes techniques de la user story {} terminée : {} cartes", userStoryId, cartes.size)
        } catch (e: Exception) {
            log.error("Échec de la génération des cartes techniques de la user story {}", userStoryId, e)
            transactionTemplate.execute {
                userStoryRepository.findById(userStoryId).orElse(null)?.let { us ->
                    us.cartesStatut = GenerationStatut.ECHEC
                    us.erreurGeneration = e.message?.take(500)
                    userStoryRepository.save(us)
                }
            }
        }
    }

    private fun retablirStatutRectification(userStoryId: Long, raison: String) {
        transactionTemplate.execute {
            userStoryRepository.findById(userStoryId).orElse(null)?.let { userStory ->
                userStory.statut = UserStoryStatut.A_RECTIFIER
                userStory.erreurGeneration = raison
                userStoryRepository.save(userStory)
            }
        }
    }

    private data class RectificationData(
        val userStory: UserStory,
        val commentairesPo: List<String>,
    )

    companion object {
        private val log = LoggerFactory.getLogger(AiJobRunner::class.java)
    }
}
