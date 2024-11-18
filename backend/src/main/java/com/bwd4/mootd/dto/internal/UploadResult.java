package com.bwd4.mootd.dto.internal;

import java.time.LocalDateTime;

public record UploadResult(
        String originImageUrl,
        String thumbnailUrl,
        LocalDateTime createdAt,
        Double latitude,
        Double longitude) {}
