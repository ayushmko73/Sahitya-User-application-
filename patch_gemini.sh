sed -i 's/val apiKey = BuildConfig.GEMINI_API_KEY/val apiKey = getApiKeyForIndex(apiKeyIndex)/g' app/src/main/java/com/example/api/GeminiService.kt
sed -i 's/suspend fun generateRoleplayResponseWithContext(/suspend fun generateRoleplayResponseWithContext(\n        apiKeyIndex: Int = 1,/g' app/src/main/java/com/example/api/GeminiService.kt
sed -i 's/suspend fun generate8020RollingSummary(/suspend fun generate8020RollingSummary(\n        apiKeyIndex: Int = 1,/g' app/src/main/java/com/example/api/GeminiService.kt
sed -i 's/suspend fun generateTextEmbedding(text: String): FloatArray/suspend fun generateTextEmbedding(text: String, apiKeyIndex: Int = 1): FloatArray/g' app/src/main/java/com/example/api/GeminiService.kt
