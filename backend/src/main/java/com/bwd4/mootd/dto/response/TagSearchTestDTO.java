package com.bwd4.mootd.dto.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TagSearchTestDTO {
    private String id; // MongoDB의 ObjectId가 자동으로 할당됨
    private List<String> tag;
    private String originImageUrl;
    private String guideImageUrl;
    private String maskImageUrl;

}
