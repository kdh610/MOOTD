package com.bwd4.mootd.controller;

import com.bwd4.mootd.common.response.ApiResponse;
import com.bwd4.mootd.dto.request.MapImagesRequestDTO;
import com.bwd4.mootd.dto.request.PhotoUploadRequestDTO;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.dto.request.UploadTestRequestDTO;
import com.bwd4.mootd.service.PhotoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;

@RestController
@RequestMapping("/api/v1/photos")
@Slf4j
public class PhotoController {

    private final PhotoService photoService;

    @Autowired
    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    //TODO 촬영 기기의 고유정보를 입력받아야함.
    @PostMapping(consumes = "multipart/form-data")
    public Mono<ResponseEntity<ApiResponse<String>>> uploadPhoto(PhotoUploadRequestDTO request) {
        //1.일단 "OK"d응답 성공을 반환한다.
        //2.입력받은 이미지를 S3에 업로드한다.
        //3.입력받은 이미지에서 메타정보를 추출하여, 촬영시간, 위치정보(위도,경도)등을 추출한다.
        log.info("file is null = {}", request.originImageFile().isEmpty());
        photoService.uploadPhotoLogics(request).subscribe();
        return Mono.just(new ResponseEntity<>(ApiResponse.success("서버 전송이 완료되었습니다.", null), HttpStatus.OK));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<MapResponseDTO>>>> getImages(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radius) {

        return photoService.findPhotosWithinRadius(latitude, longitude, radius)
                .collectList()  // 모든 데이터를 리스트로 변환
                .map(list -> ResponseEntity.ok(ApiResponse.success("지도 이미지 조회 성공", list)));
    }

    @GetMapping("/{id}")
    public Mono<String> getImage(@PathVariable String id) {
        return Mono.just("ok");
    }


    @GetMapping("/test")
    public Flux<Photo> getImageByTag(@RequestParam(value = "tag") String tag) {
        log.info("tag: {}", tag);
        Flux<Photo> photoFlux = photoService.searchTag(tag);

        // Flux 스트림을 구독하면서 각 Photo 데이터를 로그에 출력
        return photoService.searchTag(tag)
                .doOnSubscribe(subscription -> log.info("Subscription started"))
                .doOnNext(photo -> log.info("Found photo: {}", photo))
                .doOnComplete(() -> log.info("Completed retrieving photos"))
                .doOnError(error -> log.error("Error occurred: ", error));
    }

    @GetMapping("/test2")
    public Mono<Photo> getImageByName(@RequestParam(value = "name") String id) {

        log.info("id: {}", id);


        // Flux 스트림을 구독하면서 각 Photo 데이터를 로그에 출력
        return photoService.searchName(id)
                .doOnNext(photo -> log.info("Fetched photo: {}", photo)) // 데이터가 있을 경우 로그를 찍음
                .doOnError(error -> log.error("Error fetching photo with tag {}: {}", id, error.getMessage())); // 에러가 발생한 경우 로그를 찍음
    }

}
