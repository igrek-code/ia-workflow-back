package com.iaworkflow.service

import com.iaworkflow.domain.CompteRendu
import com.iaworkflow.domain.CompteRenduRepository
import com.iaworkflow.domain.GenerationStatut
import com.iaworkflow.domain.UserStoryRepository
import com.iaworkflow.exception.IllegalStatutTransitionException
import com.iaworkflow.exception.NotFoundException
import com.iaworkflow.web.dto.CompteRenduDto
import com.iaworkflow.web.dto.CompteRenduResumeDto
import com.iaworkflow.web.dto.UserStoryResumeDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Service
class CompteRenduService(
    private val compteRenduRepository: CompteRenduRepository,
    private val userStoryRepository: UserStoryRepository,
    private val aiJobRunner: AiJobRunner,
    private val transactionTemplate: TransactionTemplate,
) {

    @Transactional
    fun creer(titre: String, contenu: String): CompteRenduDto =
        CompteRenduDto.from(compteRenduRepository.save(CompteRendu(titre = titre, contenu = contenu)))

    @Transactional(readOnly = true)
    fun lister(): List<CompteRenduResumeDto> =
        compteRenduRepository.findAllByOrderByIdDesc().map { CompteRenduResumeDto.from(it) }

    @Transactional(readOnly = true)
    fun obtenir(id: Long): CompteRenduDto = CompteRenduDto.from(trouver(id))

    @Transactional(readOnly = true)
    fun userStories(id: Long): List<UserStoryResumeDto> =
        userStoryRepository.findByCompteRenduIdOrderByIdAsc(id).map { UserStoryResumeDto.from(it) }

    /**
     * Lance la génération des user stories par l'agent IA, de façon asynchrone.
     * Le statut passe à EN_COURS dans une transaction courte (commitée avant le lancement),
     * le client suit l'avancement en interrogeant le compte rendu (polling).
     */
    fun genererUserStories(id: Long): CompteRenduDto {
        val dto = transactionTemplate.execute {
            val compteRendu = trouver(id)
            if (compteRendu.generationStatut == GenerationStatut.EN_COURS) {
                throw IllegalStatutTransitionException("Une génération de user stories est déjà en cours pour ce compte rendu")
            }
            if (compteRendu.userStories.isNotEmpty()) {
                throw IllegalStatutTransitionException("Les user stories de ce compte rendu ont déjà été générées")
            }
            compteRendu.generationStatut = GenerationStatut.EN_COURS
            compteRendu.erreurGeneration = null
            CompteRenduDto.from(compteRenduRepository.save(compteRendu))
        } ?: throw IllegalStateException("Transaction de génération sans résultat")
        aiJobRunner.genererUserStoriesAsync(id)
        return dto
    }

    private fun trouver(id: Long): CompteRendu =
        compteRenduRepository.findById(id).orElseThrow { NotFoundException("Compte rendu $id introuvable") }
}
