package com.bwd4.mootd.controller;

import com.bwd4.mootd.common.response.ApiResponse;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/photos")
public class PhotoController {

    private final PhotoService photoService;

    @Autowired
    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping
    public Mono<String> uploadPhoto(@RequestBody UploadTestRequestDTO request) {

        return photoService.uploadPhoto(request);
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<String>>> getImages() {
            return Mono.just(new ResponseEntity<>(ApiResponse.success("data"), HttpStatus.OK));
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
