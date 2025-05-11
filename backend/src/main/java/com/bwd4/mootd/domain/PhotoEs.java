package com.bwd4.mootd.domain;

import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(indexName = "mootd.photo")
public class PhotoEs {
    @Id
    private String id; // MongoDB의 ObjectId가 자동으로 할당됨

    @Field(type = FieldType.Text)
    private String deviceId;//기계 아이디

    @Field(type = FieldType.Text)
    private List<String> tag = new ArrayList<>();//이미지 분석 후 생성되는 태그


    private String name;

    @Field(type =FieldType.Text, name="origin_image_url")
    private String originImageUrl;

    @Field(type =FieldType.Text, name = "mask_image_url")
    private String maskImageUrl;

    @Field(type =FieldType.Text, name="person_guideline_url")
    private String personGuidelineUrl;

    @Field(type =FieldType.Text, name = "background_guideline_url")
    private String backgroundGuidelineUrl;

    private Boolean flag;

    private Long usageCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, // 또는 yyyy-MM-dd'T'HH:mm:ss
            pattern = "yyyy-MM-dd HH:mm:ss", name = "created_at") // 저장된 날짜 형식에 맞춰 지정
    private LocalDateTime createdAt;

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
