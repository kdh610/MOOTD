package com.bwd4.mootd.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PhotoUsage {
    private String photoId;
    private LocalDateTime lastUsedAt;
    private String originImageUrl;
    private String guideImageUrl;
    private String maskImageUrl;

    public PhotoUsage(String photoId, LocalDateTime lastUsedAt, String originImageUrl, String guideImageUrl, String maskImageUrl) {
        this.photoId = photoId;
        this.lastUsedAt = lastUsedAt;
        this.originImageUrl = originImageUrl;
        this.guideImageUrl = guideImageUrl;
        this.maskImageUrl = maskImageUrl;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
