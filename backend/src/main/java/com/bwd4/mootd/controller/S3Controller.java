package com.bwd4.mootd.controller;

import com.bwd4.mootd.common.response.ApiResponse;
import com.bwd4.mootd.enums.ImageType;
import com.bwd4.mootd.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class S3Controller {
    private final S3Service s3Service;


    @PostMapping("/thumbnail")
    public Mono<String> upload() {
        return s3Service.generateThumbnailsForAllPhotos()
                .thenReturn("ok");
    }

}
