package com.bwd4.mootd.service;

import com.bwd4.mootd.domain.PhotoUsage;
import com.bwd4.mootd.dto.internal.KafkaPhotoUploadRequestDTO;
import com.bwd4.mootd.dto.request.PhotoUsageRequestDTO;
import com.bwd4.mootd.dto.response.GuideLineResponseDTO;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import com.bwd4.mootd.dto.response.PhotoDetailDTO;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import com.bwd4.mootd.service.command.PhotoCommandService;
import com.bwd4.mootd.service.query.PhotoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final PhotoCommandService photoCommandService;
    private final PhotoQueryService photoQueryService;

    @Override
    public Mono<Void> uploadPhotoLogics(KafkaPhotoUploadRequestDTO request) {
        return photoCommandService.uploadPhotoLogics(request);
    }

    @Override
    public Flux<MapResponseDTO> findPhotosWithinRadius(Double latitude, Double longitude, Double rad) {
        return photoCommandService.findPhotosWithinRadius(latitude, longitude, rad);
    }

    @Override
    public Mono<List<PhotoUsage>> getRecentUsageByDeviceId(String deviceId) {
        return photoCommandService.getRecentUsageByDeviceId(deviceId);
    }

    @Override
    public Mono<Void> recordPhotoUsage(PhotoUsageRequestDTO request) {
        return photoCommandService.recordPhotoUsage(request);
    }

    @Override
    public Mono<GuideLineResponseDTO> makeGuideLine(MultipartFile originImageFile) {
        return photoCommandService.makeGuideLine(originImageFile);
    }

    @Override
    public Flux<TagSearchResponseDTO> findEsByTag(String tag) {
        return photoQueryService.findEsByTag(tag);
    }

    @Override
    public Mono<Page<TagSearchResponseDTO>> findEsByTagWithLimit(String tag, Pageable pageable) {
        return photoQueryService.findEsByTagWithLimit(tag, pageable);
    }

    @Override
    public Mono<PhotoDetailDTO> findPhotoDetail(String photoId) {
        return photoCommandService.findPhotoDetail(photoId);
    }
}
