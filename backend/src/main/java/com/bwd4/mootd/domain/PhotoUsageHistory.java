package com.bwd4.mootd.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Document(collection = "photo_usage_history")
public class PhotoUsageHistory {
    @Id
    @Field("device_id")
    private String deviceId;

    @Field("photo_usage_list")
    private List<PhotoUsage> photoUsageList = new ArrayList<>();

    public PhotoUsageHistory(String deviceId) {
        this.deviceId = deviceId;
    }

    public void addPhotoUsage(String photoId, LocalDateTime lastUsedAt,
                              String originImageUrl, String guideImageUrl, String maskImageUrl) {
        this.photoUsageList.add(new PhotoUsage(photoId, lastUsedAt, originImageUrl, guideImageUrl, maskImageUrl));

        // 항목 추가 후 즉시 내림차순 정렬
//        this.photoUsageList.sort(Comparator.comparing(PhotoUsage::getLastUsedAt).reversed());
    }

}
