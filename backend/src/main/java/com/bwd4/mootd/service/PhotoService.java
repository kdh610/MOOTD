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
    private PhotoRepository photoRepository;

    @Autowired
    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public Mono<String> uploadPhoto(UploadTestRequestDTO request) {
        Photo photo = new Photo();
        photo.setName(request.name());

        // photoRepository.save(photo) 작업이 비동기적으로 완료될 때까지 기다렸다가 "ok" 반환
        return photoRepository.save(photo) // 저장 작업을 Mono로 처리
                .doOnNext(savedPhoto -> log.info("photo={}", savedPhoto.getName())) // doOnNext로 수정
                .then(Mono.just("ok")); // 저장이 완료되면 "ok" 반환
    }
}
