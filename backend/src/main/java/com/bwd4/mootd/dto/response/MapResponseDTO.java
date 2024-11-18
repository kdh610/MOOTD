package com.bwd4.mootd.dto.response;

public record MapResponseDTO(
        String photoId,
        String maskImageUrl,
        String personGuidelineUrl,
        String backgroundGuidelineUrl,
        String thumbnailUrl,
        Double latitude,
        Double longitude
) {
}
