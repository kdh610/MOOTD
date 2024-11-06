package com.bwd4.mootd.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public record PhotoUploadRequestDTO(
        @Schema(description = "촬영한 원본 이미지")
        @RequestPart MultipartFile originImageFile,
        @Schema(description = "기기의 식별자(고유아이디)")
        @RequestPart String deviceId){
}
