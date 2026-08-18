package com.localaichat.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.localaichat.app.data.model.ChatMessage
import com.localaichat.app.data.model.ChatSession
import com.localaichat.app.data.model.MessageRole
import com.localaichat.app.data.model.ModelConfig
import org.json.JSONArray
import org.json.JSONObject

class ChatRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("local_ai_chat_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SESSIONS = "key_chat_sessions"
        private const val KEY_ACTIVE_SESSION_ID = "key_active_session_id"
        private const val KEY_MODEL_CONFIG = "key_model_config"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_SELECTED_PERSONA = "key_selected_persona"
    }

    fun saveSessions(sessions: List<ChatSession>, activeSessionId: String) {
        try {
            val jsonArray = JSONArray()
            for (session in sessions) {
                val sObj = JSONObject().apply {
                    put("id", session.id)
                    put("title", session.title)
                    put("createdAt", session.createdAt)
                    put("isPinned", session.isPinned)
                    put("personaId", session.personaId)

                    val msgArray = JSONArray()
                    for (msg in session.messages) {
                        val mObj = JSONObject().apply {
                            put("id", msg.id)
                            put("role", msg.role.name)
                            put("content", msg.content)
                            put("timestamp", msg.timestamp)
                            put("stats", msg.stats ?: "")
                        }
                        msgArray.put(mObj)
                    }
                    put("messages", msgArray)
                }
                jsonArray.put(sObj)
            }

            prefs.edit()
                .putString(KEY_SESSIONS, jsonArray.toString())
                .putString(KEY_ACTIVE_SESSION_ID, activeSessionId)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSessions(): Pair<List<ChatSession>, String?> {
        val jsonString = prefs.getString(KEY_SESSIONS, null) ?: return Pair(emptyList(), null)
        val activeId = prefs.getString(KEY_ACTIVE_SESSION_ID, null)

        val sessions = mutableListOf<ChatSession>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val sObj = jsonArray.getJSONObject(i)
                val id = sObj.getString("id")
                val title = sObj.optString("title", "New Conversation")
                val createdAt = sObj.optLong("createdAt", System.currentTimeMillis())
                val isPinned = sObj.optBoolean("isPinned", false)
                val personaId = sObj.optString("personaId", "general")

                val msgList = mutableListOf<ChatMessage>()
                val msgArray = sObj.optJSONArray("messages")
                if (msgArray != null) {
                    for (j in 0 until msgArray.length()) {
                        val mObj = msgArray.getJSONObject(j)
                        val roleStr = mObj.optString("role", "ASSISTANT")
                        val role = try { MessageRole.valueOf(roleStr) } catch (_: Exception) { MessageRole.ASSISTANT }
                        val statsStr = mObj.optString("stats", "").ifEmpty { null }

                        msgList.add(
                            ChatMessage(
                                id = mObj.optString("id"),
                                role = role,
                                content = mObj.optString("content", ""),
                                timestamp = mObj.optLong("timestamp", System.currentTimeMillis()),
                                stats = statsStr
                            )
                        )
                    }
                }

                sessions.add(
                    ChatSession(
                        id = id,
                        title = title,
                        createdAt = createdAt,
                        isPinned = isPinned,
                        personaId = personaId,
                        messages = msgList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(sessions, activeId)
    }

    fun saveModelConfig(config: ModelConfig) {
        try {
            val obj = JSONObject().apply {
                put("modelUri", config.modelUri ?: "")
                put("modelName", config.modelName)
                put("temperature", config.temperature.toDouble())
                put("topP", config.topP.toDouble())
                put("maxTokens", config.maxTokens)
                put("threadCount", config.threadCount)
                put("systemPrompt", config.systemPrompt)
            }
            prefs.edit().putString(KEY_MODEL_CONFIG, obj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadModelConfig(): ModelConfig {
        val jsonStr = prefs.getString(KEY_MODEL_CONFIG, null) ?: return ModelConfig()
        return try {
            val obj = JSONObject(jsonStr)
            val uriStr = obj.optString("modelUri", "").ifEmpty { null }
            ModelConfig(
                modelUri = uriStr,
                modelName = obj.optString("modelName", "No Model Selected"),
                temperature = obj.optDouble("temperature", 0.7).toFloat(),
                topP = obj.optDouble("topP", 0.9).toFloat(),
                maxTokens = obj.optInt("maxTokens", 512),
                threadCount = obj.optInt("threadCount", 4),
                systemPrompt = obj.optString("systemPrompt", "You are a helpful AI assistant.")
            )
        } catch (e: Exception) {
            ModelConfig()
        }
    }

    fun saveSelectedPersona(personaId: String) {
        prefs.edit().putString(KEY_SELECTED_PERSONA, personaId).apply()
    }

    fun loadSelectedPersona(): String {
        return prefs.getString(KEY_SELECTED_PERSONA, "general") ?: "general"
    }

    fun saveThemeDark(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_THEME_MODE, isDark).apply()
    }

    fun loadThemeDark(): Boolean {
        return prefs.getString(KEY_THEME_MODE, "true")?.toBoolean() ?: true
    }
}
