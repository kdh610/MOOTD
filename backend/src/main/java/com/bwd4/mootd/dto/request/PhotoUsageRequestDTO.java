package com.bwd4.mootd.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PhotoUsageRequestDTO(
        @Schema(description = "기기 고유 id")
        String deviceId,
        @Schema(description = "사진 id")
        String photoId
) {
}
