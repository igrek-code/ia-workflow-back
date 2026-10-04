package com.iaworkflow.domain

enum class UserStoryStatut {
    EN_ATTENTE_VALIDATION,
    A_RECTIFIER,
    RECTIFICATION_EN_COURS,
    VALIDEE,
}

enum class GenerationStatut {
    EN_COURS,
    TERMINEE,
    ECHEC,
}

enum class Auteur {
    PO,
    AGENT,
}
