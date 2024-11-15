package com.bwd4.mootd.domain;

import com.bwd4.mootd.dto.response.PhotoDTO;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Document(collection = "photo")
@ToString
public class Photo {
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

    private Long usageCount; //사용량

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCoordinates(double longitude, double latitude) {
        this.coordinates = new GeoJsonPoint(longitude, latitude);
    }

    public void setOriginImageUrl(String originImageUrl) {
        this.originImageUrl = originImageUrl;
    }

    public void setGuideImageUrl(String guideImageUrl) {
        this.guideImageUrl = guideImageUrl;
    }

    public void setMaskImageUrl(String maskImageUrl) {
        this.maskImageUrl = maskImageUrl;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    // Getters, Setters, Constructors

    /**
     * Photo를 PhotoDTO로 변환
     * @return
     */
    public PhotoDTO toPhotoDTO(){
        return PhotoDTO.builder()
                .id(this.id)
                .tag(this.tag)
                .originImageUrl(this.originImageUrl)
                .guideImageUrl(this.guideImageUrl)
                .maskImageUrl(this.maskImageUrl)
                .build();
    }

    /**
     * Photo를 TagSearchResponseDTO로 변환
     * @return
     */
    public TagSearchResponseDTO toTagSearchResponseDTO(){
        return TagSearchResponseDTO.builder()
                .id(this.id)
                .tag(this.tag)
                .originImageUrl(this.originImageUrl)
                .guideImageUrl(this.guideImageUrl)
                .maskImageUrl(this.maskImageUrl)
                .build();
    }


}
