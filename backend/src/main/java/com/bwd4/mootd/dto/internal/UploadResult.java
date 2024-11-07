package com.bwd4.mootd.dto.internal;

import java.time.LocalDateTime;

public record UploadResult(
        String originImageFile,
        LocalDateTime createdAt,
        Double latitude,
        Double longitude) {}
