package com.bwd4.mootd.controller;

import com.bwd4.mootd.common.response.ApiResponse;
import com.bwd4.mootd.dto.request.UploadTestRequestDTO;
import com.bwd4.mootd.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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

}
