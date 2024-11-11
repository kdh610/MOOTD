package com.example.mootd.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GuideSearchService {
    @GET("/api/v1/photos/tag")
    fun searchPhotosByTag(
        @Query("tag") tag: String
    ): Call<SearchResponse>
}

data class SearchResponse(
    val status: Int,
    val message: String,
    val data: List<SearchPhotoData>
)

data class SearchPhotoData(
    val id: String,
    val tag: List<String>,
    val originImageUrl: String,
    val guideImageUrl: String,
    val maskImageUrl: String
)