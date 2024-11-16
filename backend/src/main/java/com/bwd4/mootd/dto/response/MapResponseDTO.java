package com.bwd4.mootd.dto.response;

public record MapResponseDTO(
        String photoId,
        String maskImageUrl,
        String personGuidelineUrl,
        String backgroundGuidelineUrl,
        Double latitude,
        Double longitude
) {
}
