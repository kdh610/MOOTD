package com.bwd4.mootd.controller;

import com.bwd4.mootd.common.response.ApiResponse;
import com.bwd4.mootd.domain.PhotoUsage;
import com.bwd4.mootd.dto.request.PhotoUploadRequestDTO;
import com.bwd4.mootd.dto.request.PhotoUsageRequestDTO;
import com.bwd4.mootd.dto.response.GuideLineResponseDTO;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import com.bwd4.mootd.dto.response.PhotoDetailDTO;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import com.bwd4.mootd.infra.kafka.ProducerService;
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
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/test/photos")
@Slf4j
@RequiredArgsConstructor
public class PhotoTestController {

    private final PhotoTestService photoService;


    /** MongoDB
     * 태그를 검색하면 태그가 포함된 사진데이터를 응답하는 controller
     *
     * @param tag
     * @returngin
     */
    @GetMapping("/mongo/tag")
    public Mono<ResponseEntity<ApiResponse<List<TagSearchResponseDTO>>>> getImageByTag(@RequestParam(value = "tag") String tag) {
        log.info("tag: {}", tag);
        return photoService.findMongoByTag(tag)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success("태그 검색 성공", list)));
    }

    /** MongoDB
     * 태그를 검색에 따라 limit수만큼 반환
     * @param tag
     * @return
     */
    @GetMapping("/mongo/limit/tag")
    public Mono<ResponseEntity<ApiResponse<List<TagSearchResponseDTO>>>> getImageMongoByTag(@RequestParam(value = "tag") String tag, @RequestParam(value = "limit")int limit) {
        log.info("tag: {}", tag);

        return photoService.findMongoByTagContainingWithLimit(tag, limit)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success("태그 검색 성공", list)));
    }

    /** ElasticSearch
     * 태그검색에 따라 최대 limit의 수만큼 데이터 반환
     * @param tag
     * @param limit
     * @return
     */
    @GetMapping("/es/limit/tag")
    public Mono<ResponseEntity<ApiResponse<List<TagSearchResponseDTO>>>> getImageEsByTagLimit(@RequestParam(value = "tag") String tag, @RequestParam(value = "limit")int limit) {
        log.info("tag: {}", tag);

        return photoService.findEsByTagWithLimit(tag, limit)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success("태그 검색 성공", list)));
    }

    /** ElasticSearch
     * 태그검색에 따라 모든 데이터 반환
     * @param tag
     * @return
     */
    @GetMapping("/es/tag")
    public Mono<ResponseEntity<ApiResponse<List<TagSearchResponseDTO>>>> getImageEsByTag(@RequestParam(value = "tag") String tag) {
        log.info("tag: {}", tag);

        return photoService.findEsByTag(tag)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success("태그 검색 성공", list)));
    }

}
