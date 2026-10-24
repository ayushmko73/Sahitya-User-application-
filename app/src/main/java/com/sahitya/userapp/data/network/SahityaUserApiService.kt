package com.sahitya.userapp.data.network

import com.sahitya.userapp.data.model.UserStoryModel
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SahityaUserApiService {

    /**
     * Fetches all published stories created by the Sahitya Creator App.
     * Selects all columns without imposing column filters that may cause HTTP 400 if column names differ in PostgREST.
     */
    @GET("rest/v1/published_stories")
    suspend fun fetchPublishedStories(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String? = null,
        @Query("select") select: String = "*",
        @Query("status") status: String? = null,
        @Query("order") order: String? = null
    ): List<UserStoryModel>

    /**
     * Diagnostic endpoint to fetch the raw JSON response as a string.
     */
    @GET("rest/v1/published_stories")
    suspend fun fetchRawPublishedStoriesDiagnostic(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String? = null,
        @Query("select") select: String = "*",
        @Query("limit") limit: Int = 3
    ): okhttp3.ResponseBody

    /**
     * Secondary fallback endpoint querying 'stories' table.
     */
    @GET("rest/v1/stories")
    suspend fun fetchStoriesFallback(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String? = null,
        @Query("select") select: String = "*",
        @Query("status") status: String? = null
    ): List<UserStoryModel>

    /**
     * Tertiary fallback endpoint querying 'user_stories' table.
     */
    @GET("rest/v1/user_stories")
    suspend fun fetchUserStories(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String? = null,
        @Query("select") select: String = "*"
    ): List<UserStoryModel>

    /**
     * Quaternary fallback endpoint querying 'creator_stories' table.
     */
    @GET("rest/v1/creator_stories")
    suspend fun fetchCreatorStories(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String? = null,
        @Query("select") select: String = "*"
    ): List<UserStoryModel>
}

