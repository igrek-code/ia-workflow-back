package com.iaworkflow.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant

@Entity
@Table(name = "user_stories")
class UserStory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var titre: String = "",

    @Column(nullable = false, columnDefinition = "text")
    var description: String = "",

    @Column(nullable = false, columnDefinition = "text")
    var criteres: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var statut: UserStoryStatut = UserStoryStatut.EN_ATTENTE_VALIDATION,

    @Column(name = "version", nullable = false)
    var version: Int = 1,

    @Version
    var verrouOptimiste: Long? = null,

    @Enumerated(EnumType.STRING)
    var cartesStatut: GenerationStatut? = null,

    @Column(columnDefinition = "text")
    var erreurGeneration: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compte_rendu_id", nullable = false)
    var compteRendu: CompteRendu? = null,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    var updatedAt: Instant? = null,
) {
    @OneToMany(mappedBy = "userStory", cascade = [CascadeType.ALL], orphanRemoval = true)
    val commentaires: MutableList<Commentaire> = mutableListOf()

    @OneToMany(mappedBy = "userStory", cascade = [CascadeType.ALL], orphanRemoval = true)
    val cartesTechniques: MutableList<CarteTechnique> = mutableListOf()

    @PrePersist
    fun prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now()
        }
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}
