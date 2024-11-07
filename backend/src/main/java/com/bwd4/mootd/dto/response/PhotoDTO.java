package com.bwd4.mootd.dto.response;

import lombok.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoDTO {
    private String id; // MongoDB의 ObjectId가 자동으로 할당됨
    private Long deviceId;//기계 아이디
    private GeoJsonPoint coordinates; // GeoJSON 포인트 타입 사용
    private List<String> tag;//이미지 분석 후 생성되는 태그
    private LocalDateTime createdAt;//촬영시간
    private String name;
    private String originImageUrl;
    private String guideImageUrl;
    private String maskImageUrl;
    private Boolean flag;

    @Override
    public String toString() {
        return "photoDTO{" +
                "id='" + id + '\'' +
                ", deviceId=" + deviceId +
                ", coordinates=" + coordinates +
                ", tag=" + tag +
                ", createdAt=" + createdAt +
                ", name='" + name + '\'' +
                ", originImageUrl='" + originImageUrl + '\'' +
                ", guideImageUrl='" + guideImageUrl + '\'' +
                ", maskImageUrl='" + maskImageUrl + '\'' +
                ", flag=" + flag +
                '}';
    }
}
