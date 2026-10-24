package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.regex.Pattern

// Sahitya Dynamic Syntax Engine Colors
val ColorSceneNarration = Color.White        // White (#FFFFFF) for Scene Narration
val ColorDirectDialogue = Color(0xFFCF931B)  // Vibrant Orange (#cf931b) for Direct Dialogue
val ColorInnerMonologue = Color(0xFF8E8E93)  // Muted Gray (#8E8E93) for Inner Thoughts

enum class TokenType {
    SCENE_NARRATION,       // *xyz*
    DIRECT_DIALOGUE,       // "You are good"
    INNER_MONOLOGUE,       // (I shouldn't have said that) or [Thinking to himself]
    NORMAL_TEXT
}

data class ParsedToken(
    val type: TokenType,
    val rawText: String,
    val innerText: String
)

/**
 * Regex matcher for Sahitya Custom Syntax Engine:
 * Group 1/2: *Scene Narration*
 * Group 3/4: "Direct Dialogue" or “Direct Dialogue”
 * Group 5/6: (Inner Monologue) or [Inner Monologue]
 */
private val SYNTAX_PATTERN: Pattern = Pattern.compile(
    """(\*([^*]+)\*)|("([^"]+)")|(“([^”]+)”)|\(([^)]+)\)|\[([^\]]+)\]"""
)

fun stripHashTags(input: String): String {
    return input.replace(Regex("""#[^#]*#"""), "")
        .replace("#", "")
        .trim()
}

/**
 * Strips raw markdown syntax characters like asterisks (*), formatting dialogue and action text cleanly
 * for professional display in chat lists and message previews.
 */
fun cleanChatPreviewText(input: String): String {
    if (input.isBlank()) return ""
    val tokens = parseSahityaTokens(input)
    if (tokens.isEmpty()) return stripHashTags(input).replace("*", "").trim()

    val sb = StringBuilder()
    tokens.forEach { token ->
        when (token.type) {
            TokenType.SCENE_NARRATION -> {
                val clean = token.innerText.trim()
                if (clean.isNotBlank()) {
                    sb.append(clean).append(" ")
                }
            }
            TokenType.DIRECT_DIALOGUE -> {
                val clean = token.innerText.trim()
                if (clean.isNotBlank()) {
                    sb.append("\"").append(clean).append("\" ")
                }
            }
            TokenType.INNER_MONOLOGUE -> {
                val clean = token.innerText.trim()
                if (clean.isNotBlank()) {
                    sb.append(clean).append(" ")
                }
            }
            TokenType.NORMAL_TEXT -> {
                val clean = token.rawText.replace("*", "").trim()
                if (clean.isNotBlank()) {
                    sb.append(clean).append(" ")
                }
            }
        }
    }
    val result = sb.toString().replace(Regex("\\s+"), " ").trim()
    return if (result.isBlank()) input.replace("*", "").trim() else result
}

fun parseSahityaTokens(input: String): List<ParsedToken> {
    val cleanedInput = stripHashTags(input)
    val tokens = mutableListOf<ParsedToken>()
    val matcher = SYNTAX_PATTERN.matcher(cleanedInput)
    var lastIndex = 0

    while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()

        if (start > lastIndex) {
            val normalStr = cleanedInput.substring(lastIndex, start)
            if (normalStr.isNotEmpty()) {
                tokens.add(ParsedToken(TokenType.NORMAL_TEXT, normalStr, normalStr))
            }
        }

        val raw = matcher.group()
        val token = when {
            raw.startsWith("*") && raw.endsWith("*") -> {
                val inner = raw.substring(1, raw.length - 1)
                ParsedToken(TokenType.SCENE_NARRATION, raw, inner)
            }
            (raw.startsWith("\"") && raw.endsWith("\"")) || (raw.startsWith("“") && raw.endsWith("”")) -> {
                val inner = raw.substring(1, raw.length - 1)
                ParsedToken(TokenType.DIRECT_DIALOGUE, raw, inner)
            }
            (raw.startsWith("(") && raw.endsWith(")")) || (raw.startsWith("[") && raw.endsWith("]")) -> {
                val inner = raw.substring(1, raw.length - 1)
                ParsedToken(TokenType.INNER_MONOLOGUE, raw, inner)
            }
            else -> ParsedToken(TokenType.NORMAL_TEXT, raw, raw)
        }

        tokens.add(token)
        lastIndex = end
    }

    if (lastIndex < cleanedInput.length) {
        val remaining = cleanedInput.substring(lastIndex)
        if (remaining.isNotEmpty()) {
            tokens.add(ParsedToken(TokenType.NORMAL_TEXT, remaining, remaining))
        }
    }

    return tokens
}

/**
 * Builds an AnnotatedString using the Sahitya Syntax Engine.
 * - When [isLiveTyping] is true: Delimiters (*, ", (), []) stay visible with their respective styles.
 * - When [isLiveTyping] is false: Delimiters are automatically hidden, leaving clean, styled text.
 */
fun buildSahityaAnnotatedString(
    input: String,
    isLiveTyping: Boolean,
    defaultColor: Color
): AnnotatedString {
    val tokens = parseSahityaTokens(input)
    return buildAnnotatedString {
        tokens.forEach { token ->
            when (token.type) {
                TokenType.SCENE_NARRATION -> {
                    withStyle(
                        SpanStyle(
                            color = ColorSceneNarration,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append(if (isLiveTyping) token.rawText else token.innerText)
                    }
                }
                TokenType.DIRECT_DIALOGUE -> {
                    withStyle(
                        SpanStyle(
                            color = ColorDirectDialogue,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal
                        )
                    ) {
                        append(if (isLiveTyping) token.rawText else "\"${token.innerText}\"")
                    }
                }
                TokenType.INNER_MONOLOGUE -> {
                    withStyle(
                        SpanStyle(
                            color = ColorInnerMonologue,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append(if (isLiveTyping) token.rawText else token.innerText)
                    }
                }
                TokenType.NORMAL_TEXT -> {
                    withStyle(SpanStyle(color = defaultColor)) {
                        append(token.rawText)
                    }
                }
            }
        }
    }
}

/**
 * Composable component to display messages formatted with Sahitya Syntax Engine.
 */
@Composable
fun SahityaFormattedMessage(
    text: String,
    isUser: Boolean,
    defaultTextColor: Color
) {
    val annotatedString = buildSahityaAnnotatedString(
        input = text,
        isLiveTyping = false,
        defaultColor = defaultTextColor
    )
    Text(
        text = annotatedString,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
}

/**
 * Real-time syntax indicator bar shown above input field while typing
 */
@Composable
fun SahityaTypingSyntaxPreview(
    inputText: String
) {
    val tokens = parseSahityaTokens(inputText)
    val hasSpecialSyntax = tokens.any { it.type != TokenType.NORMAL_TEXT }

    if (hasSpecialSyntax) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tokens.filter { it.type != TokenType.NORMAL_TEXT }.distinctBy { it.type }.forEach { token ->
                val (label, bg, color) = when (token.type) {
                    TokenType.SCENE_NARRATION -> Triple("Action", Color(0xFF2C2C2E), ColorSceneNarration)
                    TokenType.DIRECT_DIALOGUE -> Triple("Dialogue", Color(0xFF3D1F00), ColorDirectDialogue)
                    TokenType.INNER_MONOLOGUE -> Triple("Thought", Color(0xFF2C2C2E), ColorInnerMonologue)
                    else -> Triple("", Color.Transparent, Color.Unspecified)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(0.5.dp, color, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "• $label Detected",
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
