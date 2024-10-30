package com.bwd4.mootd.controller;

import com.bwd4.mootd.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

    @PostMapping
    public Mono<String> uploadImage() {
        return Mono.just("ok");
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
