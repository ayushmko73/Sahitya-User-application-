package com.sahitya.userapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Model for Stories fetched from the Sahitya Creator App via Supabase backend.
 * Supports flexible JSON mapping for fields created by creator app.
 */
data class UserStoryModel(
    @SerializedName("id")
    val id: String? = "",

    @SerializedName("story_id")
    val storyId: String? = "",

    @SerializedName("title")
    val title: String? = "",

    @SerializedName("name")
    val name: String? = "",

    @SerializedName("story_title")
    val storyTitle: String? = "",

    @SerializedName("subtitle")
    val subtitle: String? = "",

    @SerializedName("tagline")
    val tagline: String? = "",

    @SerializedName("summary")
    val summary: String? = "",

    @SerializedName("description")
    val description: String? = "",

    @SerializedName("synopsis")
    val synopsis: String? = "",

    @SerializedName("category")
    val category: String? = "Roleplay",

    @SerializedName("genre")
    val genre: String? = "",

    @SerializedName("main_thumbnail")
    val mainThumbnail: String? = "",

    @SerializedName("thumbnail")
    val thumbnail: String? = "",

    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = "",

    @SerializedName("cover_image")
    val coverImage: String? = "",

    @SerializedName("image_url")
    val imageUrl: String? = "",

    @SerializedName("avatar_url")
    val avatarUrl: String? = "",

    @SerializedName("banner_image")
    val bannerImage: String? = "",

    @SerializedName("banner")
    val banner: String? = "",

    @SerializedName("banner_url")
    val bannerUrl: String? = "",

    @SerializedName("hero_image")
    val heroImage: String? = "",

    @SerializedName("is_anime")
    val isAnime: Boolean? = false,

    @SerializedName("status")
    val status: String? = "Published",

    @SerializedName("precompiled_system_prompt")
    val precompiledSystemPrompt: String? = "",

    @SerializedName("system_prompt")
    val systemPrompt: String? = "",

    @SerializedName("prompt")
    val prompt: String? = "",

    @SerializedName("character_prompt")
    val characterPrompt: String? = "",

    @SerializedName("greeting_message")
    val greetingMessage: String? = "",

    @SerializedName("greeting")
    val greeting: String? = "",

    @SerializedName("first_message")
    val firstMessage: String? = "",

    @SerializedName("initial_message")
    val initialMessage: String? = "",

    @SerializedName("formatted_timeline")
    val formattedTimeline: String? = "",

    @SerializedName("total_views")
    val totalViews: Long? = 0,

    @SerializedName("total_chats")
    val totalChats: Long? = 0
) {
    fun getResolvedId(index: Int): String {
        val candidate = id?.ifBlank { null } ?: storyId?.ifBlank { null }
        return candidate ?: "creator_story_$index"
    }

    fun getResolvedTitle(): String {
        val candidate = title?.ifBlank { null }
            ?: name?.ifBlank { null }
            ?: storyTitle?.ifBlank { null }
        return candidate ?: "Untitled Story"
    }

    fun getResolvedTagline(): String {
        val candidate = subtitle?.ifBlank { null }
            ?: tagline?.ifBlank { null }
            ?: description?.ifBlank { null }
            ?: synopsis?.ifBlank { null }
            ?: summary?.ifBlank { null }
        return candidate ?: "A captivating story created on Sahitya."
    }

    fun getResolvedCategory(): String {
        val candidate = category?.ifBlank { null } ?: genre?.ifBlank { null }
        return candidate ?: "Roleplay"
    }

    fun getResolvedThumbnail(): String {
        val candidate = mainThumbnail?.ifBlank { null }
            ?: thumbnail?.ifBlank { null }
            ?: thumbnailUrl?.ifBlank { null }
            ?: coverImage?.ifBlank { null }
            ?: imageUrl?.ifBlank { null }
            ?: avatarUrl?.ifBlank { null }
            ?: bannerImage?.ifBlank { null }
        return candidate ?: ""
    }

    fun getResolvedBanner(): String {
        val candidate = bannerImage?.ifBlank { null }
            ?: banner?.ifBlank { null }
            ?: bannerUrl?.ifBlank { null }
            ?: heroImage?.ifBlank { null }
            ?: mainThumbnail?.ifBlank { null }
            ?: thumbnail?.ifBlank { null }
        return candidate ?: ""
    }

    fun getResolvedSystemPrompt(): String {
        val t = getResolvedTitle()
        val desc = getResolvedTagline()
        val candidate = precompiledSystemPrompt?.ifBlank { null }
            ?: systemPrompt?.ifBlank { null }
            ?: prompt?.ifBlank { null }
            ?: characterPrompt?.ifBlank { null }
        return candidate ?: "You are $t. $desc Speak exclusively in conversational Hinglish."
    }

    fun getResolvedGreetingMessage(): String {
        val t = getResolvedTitle()
        val candidate = greetingMessage?.ifBlank { null }
            ?: greeting?.ifBlank { null }
            ?: firstMessage?.ifBlank { null }
            ?: initialMessage?.ifBlank { null }
        return candidate ?: "*Looks up as you approach.* \"Arey, welcome! Main $t hoon. Batao, aaj kya plan hai?\""
    }
}

