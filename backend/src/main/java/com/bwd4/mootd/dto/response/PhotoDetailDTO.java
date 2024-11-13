package com.bwd4.mootd.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "photoId를 사용해서 단일조회API 응답 DTO")
public record PhotoDetailDTO(
        @Schema(description = "조회할 사진/가이드라인의 고유ID")
        String photoId,
        @Schema(description = "사람을 가린 이미지 url")
        String maskImageUrl,
        @Schema(description = "가이드 Url")
        String guideImageUrl,
        @Schema(description = "사진을 촬영한 위치, 위도")
        Double latitude,
        @Schema(description = "사진을 촬영한 위치, 경도")
        Double longitude
) {
}
