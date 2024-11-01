package com.bwd4.mootd.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Document
public class Photo {
    @Id
    private String id; // MongoDB의 ObjectId가 자동으로 할당됨

    @Field("device_id")
    private Long deviceId;//기계 아이디

    private GeoJsonPoint location; // GeoJSON 포인트 타입 사용

    private List<String> tag;//이미지 분석 후 생성되는 태그

    @Field("created_at")
    private LocalDateTime createdAt;//촬영시간

    @Field("name")
    private String name;

    @Field("origin_url")
    private String originUrl;

    @Field("guide_url")
    private String guideUrl;

    @Field("mask_url")
    private String maskUrl;

    private Boolean flag;

    public void setName(String name) {
        this.name = name;
    }

    // Getters, Setters, Constructors
}
