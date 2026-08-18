package com.localaichat.app.data.model

data class ModelConfig(
    val modelUri: String? = null,
    val modelName: String = "No Model Selected",
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val maxTokens: Int = 512,
    val threadCount: Int = 4,
    val systemPrompt: String = "You are a helpful, knowledgeable, and polite AI assistant. Answer concisely and accurately."
)
