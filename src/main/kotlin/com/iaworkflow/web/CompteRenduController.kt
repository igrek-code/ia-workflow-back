package com.iaworkflow.web

import com.iaworkflow.service.CompteRenduService
import com.iaworkflow.web.dto.CompteRenduDto
import com.iaworkflow.web.dto.CompteRenduRequest
import com.iaworkflow.web.dto.CompteRenduResumeDto
import com.iaworkflow.web.dto.UserStoryResumeDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/comptes-rendus")
class CompteRenduController(private val service: CompteRenduService) {

    @PostMapping
    fun creer(@Valid @RequestBody requete: CompteRenduRequest): ResponseEntity<CompteRenduDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.creer(requete.titre, requete.contenu))

    @GetMapping
    fun lister(): List<CompteRenduResumeDto> = service.lister()

    @GetMapping("/{id}")
    fun obtenir(@PathVariable("id") id: Long): CompteRenduDto = service.obtenir(id)

    @GetMapping("/{id}/user-stories")
    fun userStories(@PathVariable("id") id: Long): List<UserStoryResumeDto> = service.userStories(id)

    @PostMapping("/{id}/generation-user-stories")
    fun genererUserStories(@PathVariable("id") id: Long): ResponseEntity<CompteRenduDto> =
        ResponseEntity.accepted().body(service.genererUserStories(id))
}
