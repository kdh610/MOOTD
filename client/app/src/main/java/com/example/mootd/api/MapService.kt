package com.example.mootd.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MapService {
    @GET("/api/v1/photos")
    fun getPhotos(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int
    ): Call<ResponseData>
}

data class ResponseData(
    val status: Int,
    val message: String,
    val data: List<PhotoResponse>
)

data class PhotoResponse(
    val photoId: String,
    val maskImageUrl: String,
    val latitude: Double,
    val longitude: Double
)
