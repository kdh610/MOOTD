package com.example.mootd.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class UsageRequest(
    val deviceId: String,
    val photoId: String
)

// 응답 데이터가 필요하다면, 응답 데이터 클래스를 정의하세요.
// 예를 들어, 성공 시 아무런 응답을 받지 않는다면 Unit을 반환 타입으로 사용.
interface GuideUsageService {
    @POST("/api/v1/photos/usage")
    fun postUsageData(
        @Body usageRequest: UsageRequest
    ): Call<Unit> // 필요에 따라 적절한 응답 데이터 타입을 설정
}