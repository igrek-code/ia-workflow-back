package com.iaworkflow.web.dto

import com.iaworkflow.domain.Auteur
import com.iaworkflow.domain.CarteTechnique
import com.iaworkflow.domain.Commentaire
import com.iaworkflow.domain.CompteRendu
import com.iaworkflow.domain.GenerationStatut
import com.iaworkflow.domain.UserStory
import com.iaworkflow.domain.UserStoryStatut
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class CompteRenduRequest(
    @field:NotBlank val titre: String,
    @field:NotBlank val contenu: String,
)

data class CommentaireRequest(
    @field:NotNull val auteur: Auteur,
    @field:NotBlank val contenu: String,
)

data class CompteRenduResumeDto(
    val id: Long?,
    val titre: String,
    val generationStatut: GenerationStatut?,
    val createdAt: Instant?,
) {
    companion object {
        fun from(cr: CompteRendu) = CompteRenduResumeDto(cr.id, cr.titre, cr.generationStatut, cr.createdAt)
    }
}

data class CompteRenduDto(
    val id: Long?,
    val titre: String,
    val contenu: String,
    val generationStatut: GenerationStatut?,
    val erreurGeneration: String?,
    val createdAt: Instant?,
    val userStories: List<UserStoryResumeDto>,
) {
    companion object {
        fun from(cr: CompteRendu) = CompteRenduDto(
            id = cr.id,
            titre = cr.titre,
            contenu = cr.contenu,
            generationStatut = cr.generationStatut,
            erreurGeneration = cr.erreurGeneration,
            createdAt = cr.createdAt,
            userStories = cr.userStories.sortedBy { it.id ?: 0 }.map { UserStoryResumeDto.from(it) },
        )
    }
}

data class UserStoryResumeDto(
    val id: Long?,
    val titre: String,
    val statut: UserStoryStatut,
    val version: Int,
    val cartesStatut: GenerationStatut?,
) {
    companion object {
        fun from(us: UserStory) = UserStoryResumeDto(us.id, us.titre, us.statut, us.version, us.cartesStatut)
    }
}

data class CommentaireDto(
    val id: Long?,
    val auteur: Auteur?,
    val contenu: String,
    val createdAt: Instant?,
) {
    companion object {
        fun from(c: Commentaire) = CommentaireDto(c.id, c.auteur, c.contenu, c.createdAt)
    }
}

data class CarteTechniqueResumeDto(
    val id: Long?,
    val titre: String,
) {
    companion object {
        fun from(ct: CarteTechnique) = CarteTechniqueResumeDto(ct.id, ct.titre)
    }
}

data class UserStoryDto(
    val id: Long?,
    val titre: String,
    val description: String,
    val criteres: List<String>,
    val statut: UserStoryStatut,
    val version: Int,
    val cartesStatut: GenerationStatut?,
    val erreurGeneration: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val compteRenduId: Long?,
    val compteRenduTitre: String?,
    val commentaires: List<CommentaireDto>,
    val cartesTechniques: List<CarteTechniqueResumeDto>,
) {
    companion object {
        fun from(us: UserStory) = UserStoryDto(
            id = us.id,
            titre = us.titre,
            description = us.description,
            criteres = us.criteres.lines().map { it.trim() }.filter { it.isNotEmpty() },
            statut = us.statut,
            version = us.version,
            cartesStatut = us.cartesStatut,
            erreurGeneration = us.erreurGeneration,
            createdAt = us.createdAt,
            updatedAt = us.updatedAt,
            compteRenduId = us.compteRendu?.id,
            compteRenduTitre = us.compteRendu?.titre,
            commentaires = us.commentaires.sortedBy { it.id ?: 0 }.map { CommentaireDto.from(it) },
            cartesTechniques = us.cartesTechniques.sortedBy { it.id ?: 0 }.map { CarteTechniqueResumeDto.from(it) },
        )
    }
}

data class CarteTechniqueDto(
    val id: Long?,
    val titre: String,
    val description: String,
    val taches: List<String>,
    val createdAt: Instant?,
    val userStoryId: Long?,
    val userStoryTitre: String?,
    val userStoryStatut: UserStoryStatut?,
    val compteRenduId: Long?,
    val compteRenduTitre: String?,
) {
    companion object {
        fun from(ct: CarteTechnique) = CarteTechniqueDto(
            id = ct.id,
            titre = ct.titre,
            description = ct.description,
            taches = ct.taches.lines().map { it.trim() }.filter { it.isNotEmpty() },
            createdAt = ct.createdAt,
            userStoryId = ct.userStory?.id,
            userStoryTitre = ct.userStory?.titre,
            userStoryStatut = ct.userStory?.statut,
            compteRenduId = ct.userStory?.compteRendu?.id,
            compteRenduTitre = ct.userStory?.compteRendu?.titre,
        )
    }
}
