package com.iaworkflow.web

import com.iaworkflow.domain.UserStoryStatut
import com.iaworkflow.service.UserStoryService
import com.iaworkflow.web.dto.CarteTechniqueDto
import com.iaworkflow.web.dto.CommentaireDto
import com.iaworkflow.web.dto.CommentaireRequest
import com.iaworkflow.web.dto.UserStoryDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user-stories")
class UserStoryController(private val service: UserStoryService) {

    @GetMapping
    fun lister(@RequestParam("statut", required = false) statut: UserStoryStatut?): List<UserStoryDto> =
        service.lister(statut)

    @GetMapping("/{id}")
    fun obtenir(@PathVariable("id") id: Long): UserStoryDto = service.obtenir(id)

    @PostMapping("/{id}/commentaires")
    fun commenter(
        @PathVariable("id") id: Long,
        @Valid @RequestBody requete: CommentaireRequest,
    ): ResponseEntity<CommentaireDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.commenter(id, requete.auteur, requete.contenu))

    @PostMapping("/{id}/rectification")
    fun rectifier(@PathVariable("id") id: Long): ResponseEntity<UserStoryDto> =
        ResponseEntity.accepted().body(service.rectifier(id))

    @PostMapping("/{id}/validation")
    fun valider(@PathVariable("id") id: Long): ResponseEntity<UserStoryDto> =
        ResponseEntity.accepted().body(service.valider(id))

    @GetMapping("/{id}/cartes-techniques")
    fun cartesTechniques(@PathVariable("id") id: Long): List<CarteTechniqueDto> = service.cartesTechniques(id)
}
