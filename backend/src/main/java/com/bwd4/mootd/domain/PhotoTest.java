package com.bwd4.mootd.domain;

import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import com.bwd4.mootd.dto.response.TagSearchTestDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "photo_test")
public class PhotoTest {
    @Id
    private String id; // MongoDB의 ObjectId가 자동으로 할당됨

    @Field("device_id")
    private String deviceId;//기계 아이디

    private GeoJsonPoint coordinates; // GeoJSON 포인트 타입 사용

    private List<String> tag = new ArrayList<>();//이미지 분석 후 생성되는 태그

    @Field("created_at")
    private LocalDateTime createdAt;//촬영시간

    @Field("name")
    private String name;

    @Field("origin_image_url")
    private String originImageUrl;

    @Field("guide_image_url")
    private String guideImageUrl;

    @Field("mask_image_url")
    private String maskImageUrl;

    private Boolean flag;

    private Long usageCount;

    public TagSearchTestDTO toTagSearchTestDTO(){
        return TagSearchTestDTO.builder()
                .id(this.id)
                .tag(this.tag)
                .originImageUrl(this.originImageUrl)
                .guideImageUrl(this.guideImageUrl)
                .maskImageUrl(this.maskImageUrl)
                .build();
    }

}
