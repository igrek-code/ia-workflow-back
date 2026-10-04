package com.iaworkflow.domain

import org.springframework.data.jpa.repository.JpaRepository

interface CompteRenduRepository : JpaRepository<CompteRendu, Long> {
    fun findAllByOrderByIdDesc(): List<CompteRendu>
}

interface UserStoryRepository : JpaRepository<UserStory, Long> {
    fun findByCompteRenduIdOrderByIdAsc(compteRenduId: Long): List<UserStory>
    fun findByStatutOrderByIdAsc(statut: UserStoryStatut): List<UserStory>
    fun findAllByOrderByIdAsc(): List<UserStory>
}

interface CommentaireRepository : JpaRepository<Commentaire, Long>

interface CarteTechniqueRepository : JpaRepository<CarteTechnique, Long> {
    fun findByUserStoryIdOrderByIdAsc(userStoryId: Long): List<CarteTechnique>
    fun findAllByOrderByIdAsc(): List<CarteTechnique>
}
