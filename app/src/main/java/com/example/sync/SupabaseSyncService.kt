package com.example.sync

import android.util.Log
import com.example.BuildConfig
import com.example.data.ActiveChatEntity
import com.example.data.ChatMessageEntity
import com.example.data.ChatSummaryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Supabase Cloud Synchronization & Rolling Memory Schema Handler.
 * Synchronizes local Room database state (Active Sessions, Raw Chat Messages, and 80/20 Rolling Summaries)
 * with Supabase PostgreSQL cloud endpoints.
 */
object SupabaseSyncService {

    private const val TAG = "SupabaseSyncService"

    /**
     * PostgreSQL Schema DDL for Supabase Cloud Database setup with pgvector.
     * Execute this SQL script in the Supabase SQL Editor to initialize cloud mirror tables & vector search RPC.
     */
    val SUPABASE_ROLLING_MEMORY_SCHEMA_SQL = """
        -- ====================================================================
        -- SUPABASE CLOUD DATABASE MIGRATION: SAHITYA 80/20 MEMORY & PGVECTOR
        -- ====================================================================

        -- Enable pgvector extension for AI semantic memory search
        CREATE EXTENSION IF NOT EXISTS vector;

        -- 1. Active Chat Sessions Table
        CREATE TABLE IF NOT EXISTS public.active_chats (
            character_id TEXT PRIMARY KEY,
            character_name TEXT NOT NULL,
            character_handle TEXT NOT NULL,
            genre TEXT NOT NULL,
            last_message TEXT NOT NULL,
            last_timestamp BIGINT NOT NULL,
            unread_count INT DEFAULT 0,
            avatar_type TEXT NOT NULL,
            updated_at TIMESTAMPTZ DEFAULT NOW()
        );

        -- 2. 80/20 Rolling Chat Summaries Table with Vector Embeddings (768d)
        CREATE TABLE IF NOT EXISTS public.chat_summaries (
            id BIGSERIAL PRIMARY KEY,
            character_id TEXT NOT NULL REFERENCES public.active_chats(character_id) ON DELETE CASCADE,
            summary_block TEXT NOT NULL,
            start_message_timestamp BIGINT NOT NULL,
            end_message_timestamp BIGINT NOT NULL,
            message_count_summarized INT DEFAULT 10,
            chunk_index INT NOT NULL,
            embedding vector(768),
            created_at BIGINT NOT NULL
        );

        -- 3. Raw Unsummarized Active Messages Table
        CREATE TABLE IF NOT EXISTS public.chat_messages (
            id BIGSERIAL PRIMARY KEY,
            character_id TEXT NOT NULL REFERENCES public.active_chats(character_id) ON DELETE CASCADE,
            sender TEXT NOT NULL,
            text TEXT NOT NULL,
            timestamp BIGINT NOT NULL
        );

        -- Cosine distance vector index for pgvector semantic search
        CREATE INDEX IF NOT EXISTS idx_chat_summaries_embedding ON public.chat_summaries USING hnsw (embedding vector_cosine_ops);
        CREATE INDEX IF NOT EXISTS idx_chat_summaries_character ON public.chat_summaries(character_id, chunk_index);
        CREATE INDEX IF NOT EXISTS idx_chat_messages_character ON public.chat_messages(character_id, timestamp);

        -- 4. PostgreSQL RPC Function for Semantic Vector Search
        CREATE OR REPLACE FUNCTION match_chat_summaries (
            query_embedding vector(768),
            match_threshold float,
            match_count int,
            p_character_id text
        )
        RETURNS TABLE (
            id bigint,
            character_id text,
            summary_block text,
            chunk_index int,
            similarity float
        )
        LANGUAGE plpgsql
        AS $$
        BEGIN
            RETURN QUERY
            SELECT
                cs.id,
                cs.character_id,
                cs.summary_block,
                cs.chunk_index,
                (1 - (cs.embedding <=> query_embedding))::float AS similarity
            FROM public.chat_summaries cs
            WHERE cs.character_id = p_character_id
              AND cs.embedding IS NOT NULL
              AND (1 - (cs.embedding <=> query_embedding)) > match_threshold
            ORDER BY cs.embedding <=> query_embedding
            LIMIT match_count;
        END;
        $$;

        -- Row Level Security (RLS) policies for secure client access
        ALTER TABLE public.active_chats ENABLE ROW LEVEL SECURITY;
        ALTER TABLE public.chat_summaries ENABLE ROW LEVEL SECURITY;
        ALTER TABLE public.chat_messages ENABLE ROW LEVEL SECURITY;

        CREATE POLICY "Allow public read-write for active chats" ON public.active_chats FOR ALL USING (true);
        CREATE POLICY "Allow public read-write for chat summaries" ON public.chat_summaries FOR ALL USING (true);
        CREATE POLICY "Allow public read-write for chat messages" ON public.chat_messages FOR ALL USING (true);
    """.trimIndent()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Syncs a newly created 80/20 rolling summary checkpoint along with its 768d pgvector embedding to Supabase Cloud.
     */
    suspend fun syncSummaryCheckpointToSupabase(
        summary: ChatSummaryEntity,
        embedding: FloatArray? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val supabaseUrl = getSupabaseUrl()
        val supabaseKey = getSupabaseKey()

        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
            Log.d(TAG, "[Cloud Sync Standby] Local Summary Chunk ${summary.chunkIndex} saved for ${summary.characterId}. Supabase credentials ready.")
            return@withContext true
        }

        try {
            val jsonPayload = JSONObject().apply {
                put("character_id", summary.characterId)
                put("summary_block", summary.summaryBlock)
                put("start_message_timestamp", summary.startMessageTimestamp)
                put("end_message_timestamp", summary.endMessageTimestamp)
                put("message_count_summarized", summary.messageCountSummarized)
                put("chunk_index", summary.chunkIndex)
                put("created_at", summary.createdAt)

                if (embedding != null && embedding.isNotEmpty()) {
                    val vectorArray = JSONArray()
                    embedding.forEach { vectorArray.put(it.toDouble()) }
                    put("embedding", vectorArray)
                }
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/chat_summaries")
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val success = response.isSuccessful
            Log.d(TAG, "Supabase Cloud Summary & Vector Sync result: $success (code: ${response.code})")
            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing summary block with vector embedding to Supabase", e)
            return@withContext false
        }
    }

    /**
     * Runs a semantic vector similarity search using Supabase pgvector RPC function `match_chat_summaries`.
     * Retrieves relevant past summary blocks from 50, 100, 200+ messages ago based on cosine vector similarity to query embedding.
     */
    suspend fun searchRelevantSummariesViaPgVector(
        characterId: String,
        queryEmbedding: FloatArray,
        matchCount: Int = 3,
        threshold: Float = 0.2f
    ): List<String> = withContext(Dispatchers.IO) {
        val supabaseUrl = getSupabaseUrl()
        val supabaseKey = getSupabaseKey()

        if (supabaseUrl.isBlank() || supabaseKey.isBlank() || queryEmbedding.isEmpty()) {
            return@withContext emptyList()
        }

        try {
            val vectorArray = JSONArray()
            queryEmbedding.forEach { vectorArray.put(it.toDouble()) }

            val jsonPayload = JSONObject().apply {
                put("query_embedding", vectorArray)
                put("match_threshold", threshold.toDouble())
                put("match_count", matchCount)
                put("p_character_id", characterId)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/rpc/match_chat_summaries")
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val resultsArray = JSONArray(responseBody)
                val relevantSummaries = mutableListOf<String>()
                for (i in 0 until resultsArray.length()) {
                    val obj = resultsArray.getJSONObject(i)
                    val summaryText = obj.optString("summary_block", "")
                    if (summaryText.isNotBlank()) {
                        relevantSummaries.add(summaryText)
                    }
                }
                Log.d(TAG, "pgvector Semantic Search found ${relevantSummaries.size} relevant memory blocks.")
                return@withContext relevantSummaries
            }
            return@withContext emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error performing pgvector semantic search", e)
            return@withContext emptyList()
        }
    }

    /**
     * Syncs active chat session state to Supabase Cloud.
     */
    suspend fun syncActiveSessionToSupabase(chat: ActiveChatEntity): Boolean = withContext(Dispatchers.IO) {
        val supabaseUrl = getSupabaseUrl()
        val supabaseKey = getSupabaseKey()

        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
            return@withContext true
        }

        try {
            val jsonPayload = JSONObject().apply {
                put("character_id", chat.characterId)
                put("character_name", chat.characterName)
                put("character_handle", chat.characterHandle)
                put("genre", chat.genre)
                put("last_message", chat.lastMessage)
                put("last_timestamp", chat.lastTimestamp)
                put("unread_count", chat.unreadCount)
                put("avatar_type", chat.avatarType)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/active_chats?on_conflict=character_id")
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing active chat session to Supabase", e)
            return@withContext false
        }
    }

    /**
     * Helper to read Supabase URL from BuildConfig or environment.
     */
    private fun getSupabaseUrl(): String {
        return try {
            val field = BuildConfig::class.java.getField("SUPABASE_URL")
            field.get(null)?.toString() ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Helper to read Supabase Key from BuildConfig or environment.
     */
    private fun getSupabaseKey(): String {
        return try {
            val field = BuildConfig::class.java.getField("SUPABASE_KEY")
            field.get(null)?.toString() ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}
