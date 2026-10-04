package com.iaworkflow.web

import com.iaworkflow.service.CarteTechniqueService
import com.iaworkflow.web.dto.CarteTechniqueDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cartes-techniques")
class CarteTechniqueController(private val service: CarteTechniqueService) {

    @GetMapping
    fun lister(): List<CarteTechniqueDto> = service.lister()

    @GetMapping("/{id}")
    fun obtenir(@PathVariable("id") id: Long): CarteTechniqueDto = service.obtenir(id)
}
