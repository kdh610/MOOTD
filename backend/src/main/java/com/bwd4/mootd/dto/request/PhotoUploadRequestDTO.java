package com.bwd4.mootd.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public record PhotoUploadRequestDTO(
        @Schema(description = "촬영한 원본 이미지", required = true)
        MultipartFile originImageFile,
        @Schema(description = "기기의 식별자(고유아이디)", required = true)
        String deviceId,
        @Schema(description = "위도", required = true)
        Double latitude,
        @Schema(description = "경도", required = true)
        Double longitude){
}
