package com.iaworkflow.ai

import com.iaworkflow.domain.UserStory
import com.iaworkflow.exception.AiGenerationException
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Component

@Component
class UserStoryRectifierAgent(private val chatClient: ChatClient) {

    fun rectifier(userStory: UserStory, commentairesPo: List<String>): UserStoryGen {
        val debut = System.nanoTime()
        val prompt = buildString {
            appendLine("User story actuelle (version ${userStory.version}) :")
            appendLine("Titre : ${userStory.titre}")
            appendLine("Description : ${userStory.description}")
            appendLine("Critères d'acceptation :")
            userStory.criteres.lines().filter { it.isNotBlank() }.forEach { appendLine("- $it") }
            appendLine()
            appendLine("Commentaires de rectification du PO :")
            commentairesPo.forEach { appendLine("- $it") }
            appendLine()
            appendLine(SortieIa.userStory.format)
        }

        val contenu = try {
            chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content()
                ?: throw AiGenerationException("L'agent IA n'a renvoyé aucune réponse")
        } catch (e: AiGenerationException) {
            throw e
        } catch (e: Exception) {
            throw AiGenerationException("Échec de l'appel à l'agent IA (rectification de la user story) : ${e.message}", e)
        }

        return try {
            SortieIa.userStory.convert(contenu).also {
                log.info("Agent IA : user story rectifiée en {} ms", dureeMs(debut))
            }
        } catch (e: Exception) {
            throw AiGenerationException("Sortie illisible renvoyée par l'agent IA : ${contenu.take(300)}", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserStoryRectifierAgent::class.java)

        private val SYSTEM_PROMPT = """
            Tu es un assistant du Product Owner chargé de rectifier des user stories.
            On te fournit une user story existante et les commentaires de rectification du PO.

            Règles :
            - Applique TOUS les commentaires du PO dans la user story corrigée.
            - Retourne la user story complète corrigée, pas uniquement les modifications.
            - Ce qui n'est visé par aucun commentaire doit être conservé tel quel.
            - La description conserve le format « En tant que [rôle], je veux [action], afin de [bénéfice] ».
            - Les critères d'acceptation restent au format Gherkin (Étant donné… / Quand… / Alors…).
            - Le titre reste court (moins de 80 caractères).
        """.trimIndent()
    }
}
