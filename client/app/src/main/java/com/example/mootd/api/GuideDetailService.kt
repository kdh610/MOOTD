package com.example.mootd.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

data class GuideDetailResponse(
    val status: Int,
    val message: String,
    val data: GuideData
)

data class GuideData(
    val photoId: String,
    val guideImageUrl: String?,
    val maskImageUrl: String?,
    val latitude: Double,
    val longitude: Double

)

interface GuideDetailService {
    @GET("/api/v1/photos/{photoId}")
    fun getPhotoData(
        @Path("photoId") photoId: String
    ): Call<GuideDetailResponse>
}