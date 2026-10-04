package com.iaworkflow.ai

import com.iaworkflow.exception.AiGenerationException
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Component

@Component
class UserStoryGeneratorAgent(private val chatClient: ChatClient) {

    fun generer(compteRendu: String): List<UserStoryGen> {
        val prompt = """
            Compte rendu de réunion :
            ---
            $compteRendu
            ---

            ${SortieIa.listeUserStories.format}
        """.trimIndent()

        val debut = System.nanoTime()
        val contenu = appelIa(prompt)
        val userStories = convertir(contenu, SortieIa.listeUserStories::convert)
        if (userStories.isEmpty()) {
            throw AiGenerationException("L'agent IA n'a généré aucune user story à partir du compte rendu")
        }
        log.info("Agent IA : {} user stories générées en {} ms", userStories.size, dureeMs(debut))
        return userStories
    }

    private fun appelIa(prompt: String): String = try {
        chatClient.prompt()
            .system(SYSTEM_PROMPT)
            .user(prompt)
            .call()
            .content()
            ?: throw AiGenerationException("L'agent IA n'a renvoyé aucune réponse")
    } catch (e: AiGenerationException) {
        throw e
    } catch (e: Exception) {
        throw AiGenerationException("Échec de l'appel à l'agent IA (génération des user stories) : ${e.message}", e)
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserStoryGeneratorAgent::class.java)

        private val SYSTEM_PROMPT = """
            Tu es un Product Owner senior francophone.
            À partir d'un compte rendu de réunion, tu extrais et tu rédiges les user stories pertinentes.

            Règles :
            - La description suit le format « En tant que [rôle], je veux [action], afin de [bénéfice] ».
            - Les critères d'acceptation sont au format Gherkin (Étant donné… / Quand… / Alors…), entre 2 et 5 par user story.
            - Le titre est court (moins de 80 caractères) et résume la user story.
            - Ignore les points d'organisation sans valeur produit.
            - Génère entre 1 et 8 user stories focalisées et testables.
        """.trimIndent()
    }
}

internal fun dureeMs(debut: Long): Long = (System.nanoTime() - debut) / 1_000_000

internal fun <T> convertir(contenu: String, conversion: (String) -> T): T = try {
    conversion(contenu)
} catch (e: Exception) {
    throw AiGenerationException("Sortie illisible renvoyée par l'agent IA : ${contenu.take(300)}", e)
}
