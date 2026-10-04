package com.iaworkflow.config

import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {

    @Bean
    fun chatClient(
        openAiChatModel: ObjectProvider<OpenAiChatModel>,
        anthropicChatModel: ObjectProvider<AnthropicChatModel>,
        ollamaChatModel: ObjectProvider<OllamaChatModel>,
        @Value("\${app.ai.provider}") provider: String,
    ): ChatClient {
        val chatModel = when (provider.lowercase()) {
            "zai", "openai" -> openAiChatModel.getObject()
            "anthropic" -> anthropicChatModel.getObject()
            "ollama" -> ollamaChatModel.getObject()
            else -> throw IllegalStateException(
                "Provider IA non supporté : $provider (attendu : zai, openai, anthropic ou ollama)"
            )
        }
        return ChatClient.builder(chatModel).build()
    }
}
