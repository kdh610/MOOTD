package com.example.mootd.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface GuideRecentService {
    @GET("/api/v1/photos/recent-usage")
    fun getRecentUsagePhotos(
        @Query("deviceId") deviceId: String
    ): Call<RecentUsageResponse>
}

// RecentUsageResponse 데이터 클래스 정의
data class RecentUsageResponse(
    val status: Int,
    val message: String,
    val data: List<PhotoData>
)

data class PhotoData(
    val photoId: String,
    val lastUsedAt: String,
    val maskImageUrl: String,
    val personGuidelineUrl: String?,
    val backgroundGuidelineUrl: String?
)