package com.iaworkflow.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "comptes_rendus")
class CompteRendu(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var titre: String = "",

    @Column(nullable = false, columnDefinition = "text")
    var contenu: String = "",

    @Enumerated(EnumType.STRING)
    var generationStatut: GenerationStatut? = null,

    @Column(columnDefinition = "text")
    var erreurGeneration: String? = null,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,
) {
    @OneToMany(mappedBy = "compteRendu")
    val userStories: MutableList<UserStory> = mutableListOf()

    @PrePersist
    fun prePersist() {
        createdAt = Instant.now()
    }
}
