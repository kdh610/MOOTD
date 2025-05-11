package com.bwd4.mootd.domain;

import com.bwd4.mootd.dto.response.PhotoDTO;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
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

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE) // 2dsphere 인덱스 설정
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

    @Field("person_guideline_url")
    private String personGuidelineUrl;

    @Field("background_guideline_url")
    private String backgroundGuidelineUrl;

    @Field("mask_image_url")
    private String maskImageUrl;

    @Field("thumbnail_url")
    private String thumbnailUrl; // 새로 추가된 컬럼

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

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    public void setOriginImageUrl(String originImageUrl) {
        this.originImageUrl = originImageUrl;
    }

    public void setGuideImageUrl(String guideImageUrl) {
        this.guideImageUrl = guideImageUrl;
    }

    public void setPersonGuidelineUrl(String personGuidelineUrl) {
        this.personGuidelineUrl = personGuidelineUrl;
    }

    public void setBackgroundGuidelineUrl(String backgroundGuidelineUrl) {
        this.backgroundGuidelineUrl = backgroundGuidelineUrl;
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


    /**
     * Photo를 TagSearchResponseDTO로 변환
     * @return
     */
    public TagSearchResponseDTO toTagSearchResponseDTO(){
        return TagSearchResponseDTO.builder()
                .id(this.id)
                .tag(this.tag)
                .originImageUrl(this.originImageUrl)
                .maskImageUrl(this.maskImageUrl)
                .personGuidelineUrl(this.personGuidelineUrl)
                .backgroundGuidelineUrl(this.backgroundGuidelineUrl)
                .createdAt(this.createdAt)
                .build();
    }


}
