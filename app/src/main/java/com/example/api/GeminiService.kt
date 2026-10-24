package com.example.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateRoleplayResponseWithContext(
        systemPrompt: String,
        userPrompt: String,
        historicalSummaries: List<String>,
        recentMessages: List<Pair<String, String>>,
        apiKeyIndex: Int = 1
    ): String = withContext(Dispatchers.IO) {
        val allKeys = getAllValidApiKeys()
        if (allKeys.isEmpty()) {
            android.util.Log.w("GeminiAPI", "API Key is missing or default. Returning informative message.")
            return@withContext "*System Message: API Keys are not configured correctly. Please enter valid keys for chat_api_1 or chat_api_2 in your Secrets panel.*"
        }

        val preferredKey = getApiKeyForIndex(apiKeyIndex)
        val keysToTry = mutableListOf<String>()
        if (preferredKey in allKeys) keysToTry.add(preferredKey)
        keysToTry.addAll(allKeys.filter { it != preferredKey })

        var lastErrorMsg = ""

        for (apiKey in keysToTry) {
            try {
                val isGroq = apiKey.startsWith("gsk_")
                
                val m1M2ArchitectureRules = """
                    [SLIDING WINDOW CONTEXT ARCHITECTURE]:
                    You are operating within an M1/M2 Context Window Architecture.
                    - M1 (Long-Term Memory): Represented by the 80/20 Narrative Summaries below. Treat these as absolute facts of the world and past events.
                    - M2 (Short-Term Working Memory): The recent message turns provided below. Treat these as the immediate, flowing conversation.
                    Maintain absolute continuity between M1 and M2.
                """.trimIndent()

                val formattingInstructions = """
                    [SAHITYA MARKDOWN RULES]:
                    - Actions/Thoughts MUST be in *italics* like *this*.
                    - Spoken dialogue MUST be in "quotes" like "this".
                    - NEVER break character.
                    - Reply ONLY with the character's response.
                """.trimIndent()

                val summaryContextBuilder = java.lang.StringBuilder()
                if (historicalSummaries.isNotEmpty()) {
                    summaryContextBuilder.append("\n\n[COMPRESSED HISTORICAL MEMORY CHECKPOINTS]:\n")
                    historicalSummaries.forEachIndexed { idx, summaryText ->
                        summaryContextBuilder.append("Checkpoint ${idx + 1}: $summaryText\n")
                    }
                }

                val fullSystemPrompt = "System Instruction: $systemPrompt\n$formattingInstructions\n$m1M2ArchitectureRules\n$summaryContextBuilder"

                val request: Request
                
                if (isGroq) {
                    val messagesArray = JSONArray()
                    
                    messagesArray.put(JSONObject().apply {
                        put("role", "system")
                        put("content", fullSystemPrompt)
                    })
                    messagesArray.put(JSONObject().apply {
                        put("role", "assistant")
                        put("content", "Understood. I will follow all instructions and continuity.")
                    })
                    recentMessages.takeLast(10).forEach { (sender, text) ->
                        val role = if (sender == "user") "user" else "assistant"
                        messagesArray.put(JSONObject().apply {
                            put("role", role)
                            put("content", text)
                        })
                    }
                    messagesArray.put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                    
                    val rootJson = JSONObject().apply {
                        put("model", "llama-3.1-70b-versatile")
                        put("messages", messagesArray)
                        put("temperature", 0.85)
                        put("max_tokens", 500)
                    }
                    val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    request = Request.Builder()
                        .url("https://api.groq.com/openai/v1/chat/completions")
                        .addHeader("Authorization", "Bearer $apiKey")
                        .post(body)
                        .build()
                } else {
                    val contentsArray = JSONArray()
                    contentsArray.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", fullSystemPrompt)))
                    })
                    contentsArray.put(JSONObject().apply {
                        put("role", "model")
                        put("parts", JSONArray().put(JSONObject().put("text", "Understood. I will follow all instructions and continuity.")))
                    })
                    recentMessages.takeLast(10).forEach { (sender, text) ->
                        val role = if (sender == "user") "user" else "model"
                        contentsArray.put(JSONObject().apply {
                            put("role", role)
                            put("parts", JSONArray().put(JSONObject().put("text", text)))
                        })
                    }
                    contentsArray.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                    })
                    val rootJson = JSONObject().apply {
                        put("contents", contentsArray)
                        put("generationConfig", JSONObject().apply {
                            put("temperature", 0.85)
                            put("topP", 0.95)
                            put("maxOutputTokens", 500)
                        })
                    }
                    val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    request = Request.Builder()
                        .url("$BASE_URL?key=$apiKey")
                        .post(body)
                        .build()
                }

                val response = client.newCall(request).execute()
                val responseBodyString = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    android.util.Log.e("GeminiAPI", "HTTP Error: ${response.code} ${response.message}\nBody: $responseBodyString")
                    val errorMsg = try {
                        JSONObject(responseBodyString).getJSONObject("error").getString("message")
                    } catch (e: Exception) {
                        responseBodyString
                    }
                    
                    if (response.code == 429) {
                        lastErrorMsg = "Quota Exceeded (429)"
                        continue
                    } else {
                        lastErrorMsg = "API Error ${response.code}: $errorMsg"
                        continue
                    }
                }

                if (response.isSuccessful && responseBodyString.isNotBlank()) {
                    val responseJson = JSONObject(responseBodyString)
                    if (isGroq) {
                        val choices = responseJson.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val replyText = choices.getJSONObject(0).optJSONObject("message")?.optString("content", "")
                            if (!replyText.isNullOrBlank()) {
                                return@withContext replyText.trim()
                            }
                        }
                    } else {
                        val candidates = responseJson.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCand = candidates.getJSONObject(0)
                            val content = firstCand.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val replyText = parts.getJSONObject(0).optString("text", "")
                                if (replyText.isNotBlank()) {
                                    return@withContext replyText.trim()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GeminiAPI", "Exception calling AI", e)
                lastErrorMsg = "Network Exception: ${e.message}"
            }
        }
        return@withContext "*System Message: All available API keys failed. Last error: $lastErrorMsg*"
    }

    suspend fun generate8020RollingSummary(
        characterTitle: String,
        rawMessages: List<Pair<String, String>>,
        apiKeyIndex: Int = 1
    ): String = withContext(Dispatchers.IO) {
        val allKeys = getAllValidApiKeys()
        if (allKeys.isEmpty()) {
            return@withContext simulate8020SummaryFallback(characterTitle, rawMessages)
        }

        val preferredKey = getApiKeyForIndex(apiKeyIndex)
        val keysToTry = mutableListOf<String>()
        if (preferredKey in allKeys) keysToTry.add(preferredKey)
        keysToTry.addAll(allKeys.filter { it != preferredKey })

        for (apiKey in keysToTry) {
            try {
                val isGroq = apiKey.startsWith("gsk_")
                val transcript = java.lang.StringBuilder()
                rawMessages.forEach { (sender, text) ->
                    val speakerLabel = if (sender == "user") "User" else characterTitle
                    transcript.append("$speakerLabel: $text\n")
                }

                val summarizationInstruction = """
                    You are an AI Narrative Compression Engine.
                    Analyze the following raw message turns from a roleplay story.
                    Extract the core narrative and plot context. Output a dense, 2 to 4 sentence summary.
                    TRANSCRIPT:
                    $transcript
                """.trimIndent()

                val request: Request
                
                if (isGroq) {
                    val messagesArray = JSONArray()
                    messagesArray.put(JSONObject().apply {
                        put("role", "system")
                        put("content", summarizationInstruction)
                    })
                    
                    val rootJson = JSONObject().apply {
                        put("model", "llama-3.1-8b-instant")
                        put("messages", messagesArray)
                        put("temperature", 0.3)
                        put("max_tokens", 300)
                    }
                    val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    request = Request.Builder()
                        .url("https://api.groq.com/openai/v1/chat/completions")
                        .addHeader("Authorization", "Bearer $apiKey")
                        .post(body)
                        .build()
                } else {
                    val contentsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().put(JSONObject().put("text", summarizationInstruction)))
                        })
                    }

                    val rootJson = JSONObject().apply {
                        put("contents", contentsArray)
                        put("generationConfig", JSONObject().apply {
                            put("temperature", 0.3)
                            put("maxOutputTokens", 300)
                        })
                    }

                    val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    request = Request.Builder()
                        .url("$BASE_URL?key=$apiKey")
                        .post(body)
                        .build()
                }

                val response = client.newCall(request).execute()
                val responseBodyString = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    continue
                }

                if (response.isSuccessful && responseBodyString.isNotBlank()) {
                    val responseJson = JSONObject(responseBodyString)
                    if (isGroq) {
                        val choices = responseJson.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val summaryResult = choices.getJSONObject(0).optJSONObject("message")?.optString("content", "")
                            if (!summaryResult.isNullOrBlank()) {
                                return@withContext summaryResult.trim()
                            }
                        }
                    } else {
                        val candidates = responseJson.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val summaryResult = parts.getJSONObject(0).optString("text", "")
                                if (summaryResult.isNotBlank()) {
                                    return@withContext summaryResult.trim()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GeminiAPI", "Exception generating summary", e)
            }
        }
        return@withContext simulate8020SummaryFallback(characterTitle, rawMessages)
    }

    suspend fun generateTextEmbedding(text: String, apiKeyIndex: Int = 1): FloatArray = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext FloatArray(768) { 0.0f }
        
        val allKeys = getAllValidApiKeys()
        if (allKeys.isEmpty()) {
            return@withContext FloatArray(768) { 0.0f }
        }

        val preferredKey = getApiKeyForIndex(apiKeyIndex)
        val keysToTry = mutableListOf<String>()
        if (preferredKey in allKeys) keysToTry.add(preferredKey)
        keysToTry.addAll(allKeys.filter { it != preferredKey })

        for (apiKey in keysToTry) {
            try {
                if (apiKey.startsWith("gsk_")) continue // Skip Groq for embeddings
                
                val rootJson = JSONObject().apply {
                    put("model", "models/text-embedding-004")
                    put("content", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", text)))
                    })
                }

                val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=$apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBodyString = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    continue
                }

                if (response.isSuccessful && responseBodyString.isNotBlank()) {
                    val responseJson = JSONObject(responseBodyString)
                    val embeddingObj = responseJson.optJSONObject("embedding")
                    val valuesArr = embeddingObj?.optJSONArray("values")
                    if (valuesArr != null && valuesArr.length() > 0) {
                        val floatArr = FloatArray(valuesArr.length())
                        for (i in 0 until valuesArr.length()) {
                            floatArr[i] = valuesArr.getDouble(i).toFloat()
                        }
                        return@withContext floatArr
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GeminiAPI", "Exception generating embedding", e)
            }
        }
        return@withContext FloatArray(768) { 0.0f }
    }

    private fun simulate8020SummaryFallback(
        characterTitle: String,
        rawMessages: List<Pair<String, String>>
    ): String {
        val lastTopic = rawMessages.lastOrNull()?.second?.take(60) ?: "a crucial turning point"
        return "80/20 Narrative Summary: The user engaged in high-stakes dialogue with \$characterTitle. Key decisions and emotional tension culminated around '\$lastTopic'."
    }

    private fun simulateRoleplayFallback(systemPrompt: String, userPrompt: String): String {
        return when {
            systemPrompt.contains("samurai", ignoreCase = true) || systemPrompt.contains("Zero", ignoreCase = true) ->
                "*Talwaar thodi bahar nikalta hai, aankhein laal chamakti hain.* \"Tum yahan bina tayyari ke aaye ho, dost.\" (Iska irada kya hai...)"
            systemPrompt.contains("vampire", ignoreCase = true) || systemPrompt.contains("Karan", ignoreCase = true) ->
                "*Black velvet collar thoda adjust karta hai.* \"Arey, ek aur insaan mere ilake mein aagaya.\" (Iski dhadkan tez ho rahi hai...)"
            systemPrompt.contains("cyber", ignoreCase = true) || systemPrompt.contains("Kiara", ignoreCase = true) ->
                "*Holographic screen glint karti hai.* \"Maine city network hack kar liya hai, tension mat lo.\" [Peeche mudkar dekhti hai]"
            else ->
                "*Paas aata hai, dheeme se bolta hai.* \"Tum kya kar rahe ho yahan? Mujhe tumse kuch baat karni hai yaar...\" (Kya ye mera saath dega?)"
        }
    }

    private fun getAllValidApiKeys(): List<String> {
        val keys = mutableSetOf<String>()
        try {
            val fields = BuildConfig::class.java.declaredFields
            for (field in fields) {
                val name = field.name.uppercase()
                val value = field.get(null) as? String ?: continue
                if ((name.startsWith("CHAT_API_") || name == "GEMINI_API_KEY") &&
                    value.isNotBlank() && 
                    !value.startsWith("YOUR_") && 
                    value != "MY_GEMINI_API_KEY") {
                    keys.add(value)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeminiAPI", "Failed to read BuildConfig via reflection", e)
        }

        // Add explicit BuildConfig fields just in case reflection missed them
        listOf(BuildConfig.CHAT_API_1, BuildConfig.CHAT_API_2, BuildConfig.GEMINI_API_KEY).forEach { value ->
            if (value.isNotBlank() && !value.startsWith("YOUR_") && value != "MY_GEMINI_API_KEY") {
                keys.add(value)
            }
        }
        
        return keys.toList()
    }

    private fun getApiKeyForIndex(index: Int): String {
        var key1 = ""
        var key2 = ""
        var geminiKey = ""

        try {
            val fields = BuildConfig::class.java.declaredFields
            for (field in fields) {
                val name = field.name.uppercase()
                val value = field.get(null) as? String ?: continue
                
                // Keep the last valid non-default value we find, prioritizing non-defaults
                if (name == "CHAT_API_1" && value.isNotBlank() && !value.startsWith("YOUR_CHAT_API")) {
                    key1 = value
                } else if (name == "CHAT_API_2" && value.isNotBlank() && !value.startsWith("YOUR_CHAT_API")) {
                    key2 = value
                } else if (name == "GEMINI_API_KEY" && value.isNotBlank() && value != "MY_GEMINI_API_KEY") {
                    geminiKey = value
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeminiAPI", "Failed to read BuildConfig via reflection", e)
        }

        if (key1.isBlank()) key1 = BuildConfig.CHAT_API_1
        if (key2.isBlank()) key2 = BuildConfig.CHAT_API_2
        if (geminiKey.isBlank()) geminiKey = BuildConfig.GEMINI_API_KEY

        android.util.Log.d("GeminiAPI", "Requested API Key Index: \$index")
        val key = when (index) {
            1 -> key1
            2 -> key2
            else -> geminiKey
        }
        
        android.util.Log.d("GeminiAPI", "Resolved Key for Index \$index: \${key.take(10)}...")
        if (key.isNotBlank() && !key.startsWith("YOUR_CHAT_API") && key != "MY_GEMINI_API_KEY") {
            android.util.Log.d("GeminiAPI", "Using Key at Index \$index")
            return key
        }
        
        val validFallbacks = mutableListOf<String>()
        if (key1.isNotBlank() && !key1.startsWith("YOUR_CHAT_API") && key1 != "MY_GEMINI_API_KEY") validFallbacks.add(key1)
        if (key2.isNotBlank() && !key2.startsWith("YOUR_CHAT_API") && key2 != "MY_GEMINI_API_KEY") validFallbacks.add(key2)
        
        if (validFallbacks.isNotEmpty()) {
            val fallbackKey = validFallbacks.random()
            android.util.Log.d("GeminiAPI", "Falling back to valid pool (size=\${validFallbacks.size}), Key: \${fallbackKey.take(10)}...")
            return fallbackKey
        }

        android.util.Log.d("GeminiAPI", "Falling back to default GEMINI_API_KEY")
        return geminiKey
    }
}
