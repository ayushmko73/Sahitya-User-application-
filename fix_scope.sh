sed -i '433,435d' app/src/main/java/com/example/data/SahityaRepository.kt
sed -i '/val characterReply = GeminiService.generateRoleplayResponseWithContext(/i \        val existingChat = dao.getActiveChats().firstOrNull()?.find { it.characterId == characterId }\n        val assignedKeyIndex = existingChat?.apiKeyIndex ?: 1' app/src/main/java/com/example/data/SahityaRepository.kt
