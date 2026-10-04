package com.iaworkflow.domain

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
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "commentaires")
class Commentaire(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var auteur: Auteur? = null,

    @Column(nullable = false, columnDefinition = "text")
    var contenu: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_story_id", nullable = false)
    var userStory: UserStory? = null,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,
) {
    @PrePersist
    fun prePersist() {
        createdAt = Instant.now()
    }
}
