package com.bwd4.mootd.service;

import com.bwd4.mootd.domain.PhotoUsage;
import com.bwd4.mootd.dto.internal.KafkaPhotoUploadRequestDTO;
import com.bwd4.mootd.dto.request.PhotoUsageRequestDTO;
import com.bwd4.mootd.dto.response.GuideLineResponseDTO;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import com.bwd4.mootd.dto.response.PhotoDetailDTO;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface PhotoService {
    Mono<Void> uploadPhotoLogics(KafkaPhotoUploadRequestDTO request);
    Flux<MapResponseDTO> findPhotosWithinRadius(Double latitude, Double longitude, Double rad);
    Mono<List<PhotoUsage>> getRecentUsageByDeviceId(String deviceId);
    Mono<Void> recordPhotoUsage(PhotoUsageRequestDTO request);
    Mono<GuideLineResponseDTO> makeGuideLine(MultipartFile originImageFile);
    Flux<TagSearchResponseDTO> findEsByTag(String tag);
    Mono<Page<TagSearchResponseDTO>> findEsByTagWithLimit(String tag, Pageable pageable);
    Mono<PhotoDetailDTO> findPhotoDetail(String photoId);

}
