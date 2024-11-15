package com.bwd4.mootd.domain;

import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import com.bwd4.mootd.dto.response.TagSearchTestDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(indexName = "mootd-photo_test")
public class PhotoEs {
    @Id
    private String id; // MongoDB의 ObjectId가 자동으로 할당됨

    @Field(type = FieldType.Text)
    private String deviceId;//기계 아이디

    //private GeoJsonPoint coordinates; // GeoJSON 포인트 타입 사용

    @Field(type = FieldType.Text)
    private List<String> tag = new ArrayList<>();//이미지 분석 후 생성되는 태그


    //private LocalDateTime createdAt;//촬영시간

    private String name;

    @Field(type =FieldType.Text)
    private String originImageUrl;

    @Field(type =FieldType.Text)
    private String guideImageUrl;

    @Field(type =FieldType.Text)
    private String maskImageUrl;

    private Boolean flag;

    private Long usageCount;

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
