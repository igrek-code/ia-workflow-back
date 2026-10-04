package com.iaworkflow.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun notFound(e: NotFoundException): ResponseEntity<ApiError> =
        build(HttpStatus.NOT_FOUND, e.message ?: "Ressource introuvable")

    @ExceptionHandler(IllegalStatutTransitionException::class)
    fun conflitStatut(e: IllegalStatutTransitionException): ResponseEntity<ApiError> =
        build(HttpStatus.CONFLICT, e.message ?: "Transition de statut interdite")

    @ExceptionHandler(AiGenerationException::class)
    fun generationIa(e: AiGenerationException): ResponseEntity<ApiError> =
        build(HttpStatus.BAD_GATEWAY, e.message ?: "Échec de la génération IA")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val message = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field} : ${it.defaultMessage}" }
        return build(HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(Exception::class)
    fun generique(e: Exception): ResponseEntity<ApiError> =
        build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne : ${e.message}")

    private fun build(status: HttpStatus, message: String): ResponseEntity<ApiError> =
        ResponseEntity.status(status).body(ApiError(status.value(), message, Instant.now()))
}
