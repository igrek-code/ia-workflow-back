package com.iaworkflow.ai

import com.iaworkflow.domain.UserStory
import com.iaworkflow.exception.AiGenerationException
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Component

@Component
class TechnicalCardGeneratorAgent(private val chatClient: ChatClient) {

    fun generer(userStory: UserStory): List<CarteTechniqueGen> {
        val debut = System.nanoTime()
        val prompt = buildString {
            appendLine("User story validée :")
            appendLine("Titre : ${userStory.titre}")
            appendLine("Description : ${userStory.description}")
            appendLine("Critères d'acceptation :")
            userStory.criteres.lines().filter { it.isNotBlank() }.forEach { appendLine("- $it") }
            appendLine()
            appendLine(SortieIa.listeCartesTechniques.format)
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
            throw AiGenerationException("Échec de l'appel à l'agent IA (génération des cartes techniques) : ${e.message}", e)
        }

        val cartes = try {
            SortieIa.listeCartesTechniques.convert(contenu)
        } catch (e: Exception) {
            throw AiGenerationException("Sortie illisible renvoyée par l'agent IA : ${contenu.take(300)}", e)
        }
        if (cartes.isEmpty()) {
            throw AiGenerationException("L'agent IA n'a généré aucune carte technique")
        }
        log.info("Agent IA : {} cartes techniques générées en {} ms", cartes.size, dureeMs(debut))
        return cartes
    }

    companion object {
        private val log = LoggerFactory.getLogger(TechnicalCardGeneratorAgent::class.java)

        private val SYSTEM_PROMPT = """
            Tu es un tech lead francophone.
            À partir d'une user story validée, tu produis des cartes techniques PUREMENT techniques.

            Règles :
            - Chaque carte est technique : composants, endpoints API, modèle de données, configuration, sécurité, performance, tests.
            - Aucune reformulation fonctionnelle : la user story est validée, on passe à la réalisation.
            - La description technique est précise et autonome (lisible sans la user story).
            - Les tâches sont concrètes et réalisables (une ligne = une action).
            - Découpe en 1 à 5 cartes cohérentes et aussi indépendantes que possible.
        """.trimIndent()
    }
}
