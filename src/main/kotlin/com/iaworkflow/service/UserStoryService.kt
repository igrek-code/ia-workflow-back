package com.iaworkflow.service

import com.iaworkflow.domain.Auteur
import com.iaworkflow.domain.CarteTechniqueRepository
import com.iaworkflow.domain.Commentaire
import com.iaworkflow.domain.GenerationStatut
import com.iaworkflow.domain.UserStory
import com.iaworkflow.domain.UserStoryRepository
import com.iaworkflow.domain.UserStoryStatut
import com.iaworkflow.exception.IllegalStatutTransitionException
import com.iaworkflow.exception.NotFoundException
import com.iaworkflow.web.dto.CarteTechniqueDto
import com.iaworkflow.web.dto.CommentaireDto
import com.iaworkflow.web.dto.UserStoryDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Service
class UserStoryService(
    private val userStoryRepository: UserStoryRepository,
    private val carteTechniqueRepository: CarteTechniqueRepository,
    private val aiJobRunner: AiJobRunner,
    private val transactionTemplate: TransactionTemplate,
) {

    @Transactional(readOnly = true)
    fun lister(statut: UserStoryStatut?): List<UserStoryDto> {
        val userStories = if (statut != null) {
            userStoryRepository.findByStatutOrderByIdAsc(statut)
        } else {
            userStoryRepository.findAllByOrderByIdAsc()
        }
        return userStories.map { UserStoryDto.from(it) }
    }

    @Transactional(readOnly = true)
    fun obtenir(id: Long): UserStoryDto = UserStoryDto.from(trouver(id))

    @Transactional
    fun commenter(id: Long, auteur: Auteur, contenu: String): CommentaireDto {
        val userStory = trouver(id)
        if (userStory.statut == UserStoryStatut.VALIDEE) {
            throw IllegalStatutTransitionException("Impossible de commenter une user story déjà validée")
        }
        if (userStory.statut == UserStoryStatut.RECTIFICATION_EN_COURS) {
            throw IllegalStatutTransitionException("Impossible de commenter une user story en cours de rectification par l'agent IA")
        }
        val commentaire = Commentaire(auteur = auteur, contenu = contenu, userStory = userStory)
        userStory.commentaires.add(commentaire)
        if (auteur == Auteur.PO) {
            userStory.statut = UserStoryStatut.A_RECTIFIER
        }
        userStoryRepository.save(userStory)
        return CommentaireDto.from(commentaire)
    }

    /**
     * Lance la rectification par l'agent IA, de façon asynchrone.
     * Le statut passe à RECTIFICATION_EN_COURS dans une transaction courte (commitée
     * avant le lancement) ; l'agent relit la story et les commentaires PO lui-même.
     */
    fun rectifier(id: Long): UserStoryDto {
        val dto = transactionTemplate.execute {
            val userStory = trouver(id)
            if (userStory.statut != UserStoryStatut.A_RECTIFIER) {
                throw IllegalStatutTransitionException(
                    "La rectification n'est possible que lorsque la user story est à rectifier " +
                        "(statut actuel : ${userStory.statut})"
                )
            }
            if (userStory.commentaires.none { it.auteur == Auteur.PO }) {
                throw IllegalStatutTransitionException("Aucun commentaire du PO à traiter")
            }
            userStory.statut = UserStoryStatut.RECTIFICATION_EN_COURS
            userStory.erreurGeneration = null
            UserStoryDto.from(userStoryRepository.save(userStory))
        } ?: throw IllegalStateException("Transaction de rectification sans résultat")
        aiJobRunner.rectifierAsync(id)
        return dto
    }

    /**
     * Valide la user story (fait métier immédiat) puis lance la génération des
     * cartes techniques par l'agent IA, de façon asynchrone.
     */
    fun valider(id: Long): UserStoryDto {
        val dto = transactionTemplate.execute {
            val userStory = trouver(id)
            if (userStory.statut != UserStoryStatut.EN_ATTENTE_VALIDATION) {
                throw IllegalStatutTransitionException(
                    "La validation n'est possible que depuis le statut EN_ATTENTE_VALIDATION " +
                        "(statut actuel : ${userStory.statut})"
                )
            }
            userStory.statut = UserStoryStatut.VALIDEE
            userStory.cartesStatut = GenerationStatut.EN_COURS
            userStory.erreurGeneration = null
            UserStoryDto.from(userStoryRepository.save(userStory))
        } ?: throw IllegalStateException("Transaction de validation sans résultat")
        aiJobRunner.genererCartesAsync(id)
        return dto
    }

    @Transactional(readOnly = true)
    fun cartesTechniques(id: Long): List<CarteTechniqueDto> {
        trouver(id)
        return carteTechniqueRepository.findByUserStoryIdOrderByIdAsc(id).map { CarteTechniqueDto.from(it) }
    }

    private fun trouver(id: Long): UserStory =
        userStoryRepository.findById(id).orElseThrow { NotFoundException("User story $id introuvable") }
}
