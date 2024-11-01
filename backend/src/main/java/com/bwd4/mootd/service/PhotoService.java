package com.bwd4.mootd.service;

import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.dto.request.UploadTestRequestDTO;
import com.bwd4.mootd.repository.PhotoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PhotoService {
    private final PhotoRepository photoRepository;

    @Autowired
    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public Mono<String> uploadPhoto(UploadTestRequestDTO request) {
        Photo photo = new Photo();
        photo.setName(request.name());

        return photoRepository.save(photo) // 저장 작업을 Mono로 처리
                .doOnNext(savedPhoto -> log.info("photo={}", savedPhoto.getName())) // doOnNext로 수정
                .doOnError(error -> log.error("Failed to save photo", error)) // 오류 시 로그 출력
                .then(Mono.just("ok")); // 저장이 완료되면 "ok" 반환
    }
}
