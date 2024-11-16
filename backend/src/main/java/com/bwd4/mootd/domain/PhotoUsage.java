package com.bwd4.mootd.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PhotoUsage {
    private String photoId;
    private LocalDateTime lastUsedAt;
    private String personGuidelineUrl;
    private String backgroundGuidelineUrl;
    private String maskImageUrl;

    public PhotoUsage(String photoId, LocalDateTime lastUsedAt, String personGuidelineUrl, String backgroundGuidelineUrl, String maskImageUrl) {
        this.photoId = photoId;
        this.lastUsedAt = lastUsedAt;
        this.personGuidelineUrl = personGuidelineUrl;
        this.backgroundGuidelineUrl = backgroundGuidelineUrl;
        this.maskImageUrl = maskImageUrl;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
