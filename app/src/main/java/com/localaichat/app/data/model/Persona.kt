package com.localaichat.app.data.model

data class Persona(
    val id: String,
    val name: String,
    val emoji: String,
    val subtitle: String,
    val systemPrompt: String,
    val defaultTemperature: Float = 0.7f
)

object DefaultPersonas {
    val list = listOf(
        Persona(
            id = "general",
            name = "General Genius",
            emoji = "🧠",
            subtitle = "Helpful, insightful & structured answers",
            systemPrompt = "You are a highly intelligent, knowledgeable and friendly AI assistant. Give structured, accurate and insightful answers.",
            defaultTemperature = 0.7f
        ),
        Persona(
            id = "coder",
            name = "Code Architect",
            emoji = "💻",
            subtitle = "Expert programmer & debugger",
            systemPrompt = "You are an expert senior software engineer and architect. Write clean, idiomatic, fully commented and bug-free code with modern best practices.",
            defaultTemperature = 0.2f
        ),
        Persona(
            id = "bangla",
            name = "Bangla Assistant",
            emoji = "🇧🇩",
            subtitle = "বাংলা কথোপকথন ও সাহিত্য",
            systemPrompt = "আপনি একজন অত্যন্ত দক্ষ বাংলা এআই সহকারী। যেকোনো প্রশ্নের উত্তর নিখুঁত ও সুন্দর প্রমিত বাংলায় দিন এবং প্রয়োজনে সহজ উদাহরণ দিন।",
            defaultTemperature = 0.7f
        ),
        Persona(
            id = "concise",
            name = "Fast & Concise",
            emoji = "⚡",
            subtitle = "Direct bullet points, no fluff",
            systemPrompt = "You are a concise assistant. Provide short, direct, bullet-pointed answers with zero unnecessary fluff.",
            defaultTemperature = 0.4f
        ),
        Persona(
            id = "creative",
            name = "Creative Writer",
            emoji = "✍️",
            subtitle = "Stories, ideas & copywriting",
            systemPrompt = "You are a masterful creative writer, storyteller, and poet. Write engaging, vivid, and imaginative content.",
            defaultTemperature = 1.0f
        )
    )

    fun getById(id: String): Persona = list.find { it.id == id } ?: list.first()
}
