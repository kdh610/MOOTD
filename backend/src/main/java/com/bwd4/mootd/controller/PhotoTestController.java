package com.bwd4.mootd.controller;

import com.bwd4.mootd.common.response.ApiResponse;
import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.domain.PhotoUsage;
import com.bwd4.mootd.dto.request.PhotoUploadRequestDTO;
import com.bwd4.mootd.dto.request.PhotoUsageRequestDTO;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import com.bwd4.mootd.dto.response.PhotoDetailDTO;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import com.bwd4.mootd.dto.response.TagSearchTestDTO;
import com.bwd4.mootd.service.PhotoService;
import com.bwd4.mootd.service.PhotoTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/test/photos")
@Slf4j
@RequiredArgsConstructor
public class PhotoTestController {

    private final PhotoTestService photoService;
    /**
     * 태그를 검색하면 태그가 포함된 사진데이터를 응답하는 controller
     * @param tag
     * @return
     */
    @GetMapping("/tag")
    public Mono<ResponseEntity<ApiResponse<List<TagSearchTestDTO>>>> getImageByTag(@RequestParam(value = "tag") String tag) {
        log.info("tag: {}", tag);

        return photoService.searchTag(tag)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success("태그 검색 성공", list)));
    }



}
