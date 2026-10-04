package com.iaworkflow.exception

import java.time.Instant

class NotFoundException(message: String) : RuntimeException(message)

class IllegalStatutTransitionException(message: String) : RuntimeException(message)

class AiGenerationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

data class ApiError(
    val status: Int,
    val erreur: String,
    val horodatage: Instant,
)
