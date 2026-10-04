package com.iaworkflow.ai

import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.core.ParameterizedTypeReference
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

data class UserStoryGen(
    val titre: String = "",
    val description: String = "",
    val criteres: List<String> = emptyList(),
)

data class CarteTechniqueGen(
    val titre: String = "",
    val description: String = "",
    val taches: List<String> = emptyList(),
)

/**
 * Convertisseurs de sortie structurée : le schéma JSON est injecté dans le prompt
 * par [BeanOutputConverter.getFormat] et la réponse est désérialisée vers les types Kotlin.
 */
object SortieIa {

    private val jsonMapper: JsonMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()

    val listeUserStories: BeanOutputConverter<List<UserStoryGen>> =
        BeanOutputConverter(object : ParameterizedTypeReference<List<UserStoryGen>>() {}, jsonMapper)

    val userStory: BeanOutputConverter<UserStoryGen> =
        BeanOutputConverter(UserStoryGen::class.java, jsonMapper)

    val listeCartesTechniques: BeanOutputConverter<List<CarteTechniqueGen>> =
        BeanOutputConverter(object : ParameterizedTypeReference<List<CarteTechniqueGen>>() {}, jsonMapper)
}
