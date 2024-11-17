package com.example.mootd.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class GuideCreateResponse(
    val status: Int,
    val message: String,
    val data: GuideResult
)

data class GuideResult(
    val personGuideLineURL: String,
    val backgroundGuideLineURL: String
)

interface GuideCreateService {
    @Multipart
    @POST("/api/v1/photos/guide")
    suspend fun createGuide(
        @Part originImageFile: MultipartBody.Part
    ): Response<GuideCreateResponse>
}