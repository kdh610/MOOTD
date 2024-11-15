package com.bwd4.mootd.controller;

import com.bwd4.mootd.common.response.ApiResponse;
import com.bwd4.mootd.domain.PhotoUsage;
import com.bwd4.mootd.dto.request.PhotoUploadRequestDTO;
import com.bwd4.mootd.dto.request.PhotoUsageRequestDTO;
import com.bwd4.mootd.dto.response.GuideLineResponseDTO;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.dto.response.PhotoDetailDTO;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import com.bwd4.mootd.infra.kafka.ProducerService;
import com.bwd4.mootd.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/photos")
@Slf4j
public class PhotoController {

    private final PhotoService photoService;
    private final ProducerService producerService;

    @Autowired
    public PhotoController(PhotoService photoService, ProducerService producerService) {
        this.photoService = photoService;
        this.producerService = producerService;
    }

    @Operation(summary = "촬영 후 사진을 서버에 업로드하는 api", description = "촬영 사진을 서버로 업로드 합니다. !! 향후에 안드로이드 기기에서 가이드라인까지 생성한다면 가이드라인도 서버로 전송해야합니다.!!")
    @PostMapping(consumes = "multipart/form-data")
    public Mono<ResponseEntity<ApiResponse<String>>> uploadPhoto(@ModelAttribute PhotoUploadRequestDTO request) {
        log.info("file is null = {}", request.originImageFile().isEmpty());
        // Kafka로 메시지를 전송
        producerService.sendPhotoUploadRequest(request).subscribe();
        return Mono.just(new ResponseEntity<>(ApiResponse.success("서버 전송이 완료되었습니다.", null), HttpStatus.OK));
    }

    @Operation(summary = "위도, 경도, 반경(km)를 기반으로 해당 지역에 있는 사진목록을 조회합니다.")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<MapResponseDTO>>>> getImages(
            @Parameter(description = "위도")
            @RequestParam Double latitude,
            @Parameter(description = "경도")
            @RequestParam Double longitude,
            @Parameter(description = "(위도,경도)기반으로 조회할 반경(km)")
            @RequestParam Double radius) {
        return photoService.findPhotosWithinRadius(latitude, longitude, radius)
                .collectList()  // 모든 데이터를 리스트로 변환
                .map(list -> ResponseEntity.ok(ApiResponse.success("지도 이미지 조회 성공", list)));
    }

    @Operation(summary = "최근 사용 사진 \"등록\" API", description = "사용여부를 이 api로 등록을 해야만, 최근에 사용한 목록조회 api에서 확인이 가능합니다.")
    @PostMapping("/usage")
    public Mono<ResponseEntity<ApiResponse<?>>> photoUsage(@RequestBody PhotoUsageRequestDTO request) {
        return photoService.recordPhotoUsage(request).
                then(Mono.just(ResponseEntity.ok(ApiResponse.success("성공", null))));
    }

    @Operation(summary = "최근 사용 사진을 \"조회\" 하는 API", description = "최근 사용한 이미지들을 조회합니다. 정렬 순서는 최신순(사용일 기준 내림차순)입니다.")
    @GetMapping("/recent-usage")
    public Mono<ResponseEntity<ApiResponse<List<PhotoUsage>>>> getRecentImages(
            @Parameter(description = "디바이스 고유 id")
            @RequestParam String deviceId) {
        return photoService.getRecentUsageByDeviceId(deviceId)
                .map(list -> ResponseEntity.ok(ApiResponse.success("최근 사용한 이미지 조회 성공", list)));
    }

    /**
     * 태그를 검색하면 태그가 포함된 사진데이터를 응답하는 controller
     *
     * @param tag
     * @return
     */
    @GetMapping("/tag")
    public Mono<ResponseEntity<ApiResponse<List<TagSearchResponseDTO>>>> getImageByTag(
            @RequestParam(value = "tag") String tag) {
        log.info("tag: {}", tag);

        return photoService.searchTag(tag)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success("태그 검색 성공", list)));
    }

    @Operation(summary = "사진을 단일로 조회하는 API", description = "photoId를 사용해서 이미지를 단일 조회하는 API")
    @GetMapping("/{photoId}")
    public Mono<ResponseEntity<ApiResponse<PhotoDetailDTO>>> getPhotoDetail(
            @Parameter(description = "조회할 photoId") @PathVariable("photoId") String photoId) {
        return photoService.findPhotoDetail(photoId)
                .map(photoDetail -> ResponseEntity.ok(ApiResponse.success("photo 단일 조회 성공", photoDetail)));
    }

    @Operation(summary = "가이드라인 생성 api")
    @PostMapping(value = "/guild", consumes = "multipart/form-data")
    public Mono<ResponseEntity<ApiResponse<GuideLineResponseDTO>>> makeGuideLine(
            @RequestPart @Parameter(description = "가이드라인을 생성할 파일") MultipartFile originImageFile) {
        return photoService.makeGuideLine(originImageFile)
                .map(response -> ResponseEntity.ok(ApiResponse.success("가이드라인 생성 성공", response)));
    }

    @GetMapping("/test2")
    public Mono<ResponseEntity<ApiResponse<Photo>>> getImageByName(@RequestParam(value = "id") String id) {

        log.info("id: {}", id);
        return photoService.searchId(id)
                .map(photo -> {
                    ApiResponse<Photo> response = ApiResponse.success(photo);
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(400, "Photo not found", null)));
    }

}
