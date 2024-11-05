package com.bwd4.mootd.controller;

import com.bwd4.mootd.common.response.ApiResponse;
import com.bwd4.mootd.dto.request.PhotoUploadRequestDto;
import com.bwd4.mootd.dto.request.UploadTestRequestDTO;
import com.bwd4.mootd.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/photos")
public class PhotoController {

    private final PhotoService photoService;

    @Autowired
    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    //TODO 촬영 기기의 고유정보를 입력받아야함.
    @PostMapping(consumes = "multipart/form-data")
    public Mono<ResponseEntity<ApiResponse<String>>> uploadPhoto(PhotoUploadRequestDto request)  {
        //1.일단 "OK"d응답 성공을 반환한다.
        //2.입력받은 이미지를 S3에 업로드한다.
        //3.입력받은 이미지에서 메타정보를 추출하여, 촬영시간, 위치정보(위도,경도)등을 추출한다.
        photoService.uploadPhotoLogics(request).subscribe();
        return Mono.just(new ResponseEntity<>(ApiResponse.success(null),HttpStatus.OK));
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
    public Mono<String> test() {
        asyncMethod();
        return Mono.just("ok");
    }

    private void asyncMethod() {
        Mono.delay(Duration.ofSeconds(3)) // 3초 대기 후 시작
                .thenMany(Flux.range(1, 10) // 1부터 10까지 출력
                        ) // 각 숫자마다 1초 대기
                .doOnNext(number -> System.out.println("Number: " + number)) // 숫자 출력
                .subscribeOn(Schedulers.boundedElastic()) // 비동기 스레드에서 실행
                .subscribe(); // 실행 시작
    }
}
