cat << 'INNER_EOF' > app/src/main/java/com/example/api/GeminiService.kt
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
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateRoleplayResponseWithContext(
        systemPrompt: String,
        userPrompt: String,
        historicalSummaries: List<String>,
        recentMessages: List<Pair<String, String>>,
        apiKeyIndex: Int = 1
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKeyForIndex(apiKeyIndex)
        android.util.Log.d("GeminiAPI", "Roleplay API Key resolved (starts with): \${apiKey.take(10)}... blank? \${apiKey.isBlank()}")

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            android.util.Log.w("GeminiAPI", "Falling back to simulated roleplay response.")
            return@withContext simulateRoleplayFallback(systemPrompt, userPrompt)
        }

        try {
            val contentsArray = JSONArray()

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
                    summaryContextBuilder.append("Checkpoint \${idx + 1}: \$summaryText\n")
                }
            }

            val fullSystemPrompt = "System Instruction: \$systemPrompt\n\$formattingInstructions\n\$m1M2ArchitectureRules\n\$summaryContextBuilder"

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
            val request = Request.Builder()
                .url("\$BASE_URL?key=\$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                android.util.Log.e("GeminiAPI", "HTTP Error: \${response.code} \${response.message}\nBody: \$responseBodyString")
            }

            if (response.isSuccessful && responseBodyString.isNotBlank()) {
                val responseJson = JSONObject(responseBodyString)
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
            return@withContext simulateRoleplayFallback(systemPrompt, userPrompt)
        } catch (e: Exception) {
            android.util.Log.e("GeminiAPI", "Exception calling Gemini", e)
            return@withContext simulateRoleplayFallback(systemPrompt, userPrompt)
        }
    }

    suspend fun generate8020RollingSummary(
        characterTitle: String,
        rawMessages: List<Pair<String, String>>,
        apiKeyIndex: Int = 1
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKeyForIndex(apiKeyIndex)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext simulate8020SummaryFallback(characterTitle, rawMessages)
        }

        try {
            val transcript = java.lang.StringBuilder()
            rawMessages.forEach { (sender, text) ->
                val speakerLabel = if (sender == "user") "User" else characterTitle
                transcript.append("\$speakerLabel: \$text\n")
            }

            val summarizationInstruction = """
                You are an AI Narrative Compression Engine.
                Analyze the following raw message turns from a roleplay story.
                Extract the core narrative and plot context. Output a dense, 2 to 4 sentence summary.
                TRANSCRIPT:
                \$transcript
            """.trimIndent()

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
            val request = Request.Builder()
                .url("\$BASE_URL?key=\$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (response.isSuccessful && responseBodyString.isNotBlank()) {
                val responseJson = JSONObject(responseBodyString)
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
            return@withContext simulate8020SummaryFallback(characterTitle, rawMessages)
        } catch (e: Exception) {
            android.util.Log.e("GeminiAPI", "Exception generating summary", e)
            return@withContext simulate8020SummaryFallback(characterTitle, rawMessages)
        }
    }

    suspend fun generateTextEmbedding(text: String, apiKeyIndex: Int = 1): FloatArray = withContext(Dispatchers.IO) {
        val apiKey = getApiKeyForIndex(apiKeyIndex)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || text.isBlank()) {
            return@withContext FloatArray(768) { 0.0f }
        }

        try {
            val rootJson = JSONObject().apply {
                put("model", "models/text-embedding-004")
                put("content", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", text)))
                })
            }

            val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=\$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

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
            return@withContext FloatArray(768) { 0.0f }
        } catch (e: Exception) {
            android.util.Log.e("GeminiAPI", "Exception generating embedding", e)
            return@withContext FloatArray(768) { 0.0f }
        }
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

    private fun getApiKeyForIndex(index: Int): String {
        android.util.Log.d("GeminiAPI", "Requested API Key Index: \$index")
        val key = when (index) {
            1 -> BuildConfig.CHAT_API_1
            2 -> BuildConfig.CHAT_API_2
            3 -> BuildConfig.CHAT_API_3
            4 -> BuildConfig.CHAT_API_4
            5 -> BuildConfig.CHAT_API_5
            6 -> BuildConfig.CHAT_API_6
            7 -> BuildConfig.CHAT_API_7
            8 -> BuildConfig.CHAT_API_8
            9 -> BuildConfig.CHAT_API_9
            10 -> BuildConfig.CHAT_API_10
            else -> BuildConfig.GEMINI_API_KEY
        }
        
        android.util.Log.d("GeminiAPI", "Resolved Key for Index \$index: \${key.take(10)}...")
        if (key.isNotBlank() && !key.startsWith("YOUR_CHAT_API")) {
            android.util.Log.d("GeminiAPI", "Using Key at Index \$index")
            return key
        }
        
        val key1 = BuildConfig.CHAT_API_1
        val key2 = BuildConfig.CHAT_API_2
        
        val validFallbacks = mutableListOf<String>()
        if (key1.isNotBlank() && !key1.startsWith("YOUR_CHAT_API")) validFallbacks.add(key1)
        if (key2.isNotBlank() && !key2.startsWith("YOUR_CHAT_API")) validFallbacks.add(key2)
        
        if (validFallbacks.isNotEmpty()) {
            val fallbackKey = validFallbacks.random()
            android.util.Log.d("GeminiAPI", "Falling back to valid pool (size=\${validFallbacks.size}), Key: \${fallbackKey.take(10)}...")
            return fallbackKey
        }

        android.util.Log.d("GeminiAPI", "Falling back to GEMINI_API_KEY")
        return BuildConfig.GEMINI_API_KEY
    }
}
INNER_EOF
