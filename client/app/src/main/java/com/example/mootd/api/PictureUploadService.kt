package com.example.mootd.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class PictureUploadResponse<T>(
    val status: String,
    val message: String,
    val data: T
)

interface PictureUploadService {
    @Multipart
    @POST("/api/v1/photos")
    fun uploadPhoto(
        @Part originImageFile: MultipartBody.Part,
        @Part("deviceId") deviceId: RequestBody,
        @Part("latitude") latitude: RequestBody,
    @Part("longitude") longitude: RequestBody
    ): Call<PictureUploadResponse<String>>
}
