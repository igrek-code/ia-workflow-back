package com.iaworkflow.service

import com.iaworkflow.domain.CarteTechniqueRepository
import com.iaworkflow.exception.NotFoundException
import com.iaworkflow.web.dto.CarteTechniqueDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CarteTechniqueService(private val carteTechniqueRepository: CarteTechniqueRepository) {

    @Transactional(readOnly = true)
    fun lister(): List<CarteTechniqueDto> =
        carteTechniqueRepository.findAllByOrderByIdAsc().map { CarteTechniqueDto.from(it) }

    @Transactional(readOnly = true)
    fun obtenir(id: Long): CarteTechniqueDto = CarteTechniqueDto.from(trouver(id))

    private fun trouver(id: Long) =
        carteTechniqueRepository.findById(id).orElseThrow { NotFoundException("Carte technique $id introuvable") }
}
