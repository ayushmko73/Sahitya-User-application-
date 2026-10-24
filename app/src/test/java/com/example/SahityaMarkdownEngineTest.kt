package com.example

import com.example.ui.components.TokenType
import com.example.ui.components.cleanChatPreviewText
import com.example.ui.components.parseSahityaTokens
import com.example.ui.components.stripHashTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SahityaMarkdownEngineTest {

    @Test
    fun `stripHashTags removes tag hash symbols`() {
        val input = "Hello warrior #fantasy"
        val cleaned = stripHashTags(input)
        assertEquals("Hello warrior fantasy", cleaned)
    }

    @Test
    fun `cleanChatPreviewText strips raw asterisks for clean list preview`() {
        val input = "*Wipes blood from cursed katana under red eclipse.* 'You stand before Zero...'"
        val cleaned = cleanChatPreviewText(input)
        assertTrue(!cleaned.contains("*"))
        assertTrue(cleaned.contains("Wipes blood from cursed katana"))
    }

    @Test
    fun `parseSahityaTokens identifies scene narration and dialogue`() {
        val input = "*Enters the hall* \"Who goes there?\" (I must be careful)"
        val tokens = parseSahityaTokens(input)
        
        val narrationToken = tokens.find { it.type == TokenType.SCENE_NARRATION }
        val dialogueToken = tokens.find { it.type == TokenType.DIRECT_DIALOGUE }
        val monologueToken = tokens.find { it.type == TokenType.INNER_MONOLOGUE }
        
        assertEquals("Enters the hall", narrationToken?.innerText)
        assertEquals("Who goes there?", dialogueToken?.innerText)
        assertEquals("I must be careful", monologueToken?.innerText)
    }
}
