sed -i '/private fun getApiKeyForIndex(index: Int): String {/,/return BuildConfig.GEMINI_API_KEY/d' app/src/main/java/com/example/api/GeminiService.kt
sed -i '/    }$/d' app/src/main/java/com/example/api/GeminiService.kt
cat << 'INNER_EOF' >> app/src/main/java/com/example/api/GeminiService.kt
    private fun getApiKeyForIndex(index: Int): String {
        android.util.Log.d("GeminiAPI", "Requested API Key Index: $index")
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
        
        android.util.Log.d("GeminiAPI", "Resolved Key for Index $index: ${key.take(10)}...")
        if (key.isNotBlank() && !key.startsWith("YOUR_CHAT_API")) {
            android.util.Log.d("GeminiAPI", "Using Key at Index $index")
            return key
        }
        
        val key1 = BuildConfig.CHAT_API_1
        val key2 = BuildConfig.CHAT_API_2
        
        val validFallbacks = mutableListOf<String>()
        if (key1.isNotBlank() && !key1.startsWith("YOUR_CHAT_API")) validFallbacks.add(key1)
        if (key2.isNotBlank() && !key2.startsWith("YOUR_CHAT_API")) validFallbacks.add(key2)
        
        if (validFallbacks.isNotEmpty()) {
            val fallbackKey = validFallbacks.random()
            android.util.Log.d("GeminiAPI", "Falling back to valid pool (size=${validFallbacks.size}), Key: ${fallbackKey.take(10)}...")
            return fallbackKey
        }

        android.util.Log.d("GeminiAPI", "Falling back to GEMINI_API_KEY")
        return BuildConfig.GEMINI_API_KEY
    }
}
INNER_EOF
