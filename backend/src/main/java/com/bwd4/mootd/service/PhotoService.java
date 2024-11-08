package com.bwd4.mootd.service;

import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.domain.PhotoUsage;
import com.bwd4.mootd.domain.PhotoUsageHistory;
import com.bwd4.mootd.dto.internal.UploadResult;
import com.bwd4.mootd.dto.request.PhotoUploadRequestDTO;
import com.bwd4.mootd.dto.request.PhotoUsageRequestDTO;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import com.bwd4.mootd.dto.response.PhotoDTO;
import com.bwd4.mootd.enums.ImageType;
import com.bwd4.mootd.repository.PhotoRepository;
import com.bwd4.mootd.repository.PhotoUsageHistoryRepository;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import org.springframework.mock.web.MockMultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final S3Service s3Service; // S3 업로드용 서비스
    private final Scheduler asyncScheduler; // 비동기 스케줄러 주입
    private final PhotoUsageHistoryRepository photoUsageHistoryRepository;

    @Autowired
    public PhotoService(PhotoRepository photoRepository, S3Service s3Service, Scheduler asyncScheduler, PhotoUsageHistoryRepository photoUsageHistoryRepository) {
        this.photoRepository = photoRepository;
        this.s3Service = s3Service;
        this.asyncScheduler = asyncScheduler;
        this.photoUsageHistoryRepository = photoUsageHistoryRepository;
    }

    public Mono<Void> uploadPhotoLogics(PhotoUploadRequestDTO request) {
//    public Mono<Void> uploadPhotoLogics(MultipartFile originImageFile, String deviceId, Double latitude, Double longitude) {
        log.info("in service image upload");
        return Mono.fromCallable(() -> {
                    byte[] fileBytes = request.originImageFile().getBytes();
                    MultipartFile copiedFile = new MockMultipartFile(
                            request.originImageFile().getName(),
                            request.originImageFile().getOriginalFilename(),
                            request.originImageFile().getContentType(),
                            fileBytes
                    );
                    // 메타정보 추출
                    Map<String, Object> metadata = extractMetadata(new ByteArrayInputStream(fileBytes));

                    LocalDateTime createdAt = (LocalDateTime) metadata.get("CaptureTime");
//                    Double latitude = (Double) metadata.get("Latitude");
//                    Double longitude = (Double) metadata.get("Longitude");
                    log.info(request.latitude() + " " + request.longitude());
                    // 이미지 S3 업로드
                    String imageUrl = s3Service.upload(copiedFile, ImageType.ORIGINAL);
                    return new UploadResult(imageUrl, createdAt, request.latitude(), request.longitude());
                })
                .flatMap(result -> {
                    // MongoDB에 메타정보와 이미지 URL 저장
                    Photo photo = new Photo();
                    photo.setDeviceId(request.deviceId());
                    photo.setCreatedAt(result.createdAt());
                    photo.setCoordinates(result.longitude(), result.latitude());
                    photo.setOriginImageUrl(result.originImageFile());
                    return photoRepository.save(photo).then();
                })
                .doOnSuccess(v -> log.info("이미지 저장 완료"))
                .doOnError(error -> log.error("오류 발생: ", error))
                .subscribeOn(asyncScheduler);
    }

    private Map<String, Object> extractMetadata(ByteArrayInputStream inputStream) {
        Map<String, Object> metadataMap = new HashMap<>();
        log.info("Extracting metadata for ByteArrayInputStream");

        try {
            // 이미지 파일의 메타데이터 읽기
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);

            // GPS 정보 추출
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                metadataMap.put("Latitude", gpsDirectory.getGeoLocation().getLatitude());
                metadataMap.put("Longitude", gpsDirectory.getGeoLocation().getLongitude());
            }

            // 촬영 시간 정보 추출
            ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifDirectory != null && exifDirectory.getDateOriginal() != null) {
                LocalDateTime captureTime = LocalDateTime.ofInstant(
                        exifDirectory.getDateOriginal().toInstant(), ZoneId.systemDefault());
                metadataMap.put("CaptureTime", captureTime);
            }

            // 추가적인 메타정보 저장 (예: 카메라 모델, ISO 등)
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    metadataMap.put(tag.getTagName(), tag.getDescription());
                }
            }
        } catch (IOException e) {
            log.error("Error reading ByteArrayInputStream: ", e);
            throw new RuntimeException("Failed to extract metadata", e);
        } catch (ImageProcessingException e) {
            log.error("Image processing error: ", e);
            throw new RuntimeException("Failed to process image metadata", e);
        }
        return metadataMap;
    }
    /**
     * 위도, 경도를 기반으로 반경(radius)에 존재하는 이미지를 반환한다.
     */
    public Flux<MapResponseDTO> findPhotosWithinRadius(Double latitude, Double longitude, Double rad) {
        Point location = new Point(longitude, latitude);
        Distance radius = new Distance(rad, Metrics.KILOMETERS);
        return photoRepository.findByCoordinatesNear(location, radius)
                .map(photo -> new MapResponseDTO(
                        photo.getId(),
                        photo.getOriginImageUrl(),
                        photo.getCoordinates().getY(),  // latitude
                        photo.getCoordinates().getX()   // longitude
                ));
    }

    public Mono<List<PhotoUsage>> getRecentUsageByDeviceId(String deviceId) {
        return photoUsageHistoryRepository.findById(deviceId)
                .map(PhotoUsageHistory::getPhotoUsageList);
    }

    public Mono<Void> recordPhotoUsage(PhotoUsageRequestDTO request) {
        //1.request의 deviceId로 history에서 값을 찾는다. 없으면 새로 생성한다. => PhoUsageHistory객체
        //2.request의 photoId로 필요한 값을 뽑아서, photoUsageHistory객체 photoUsageList필드에 넣는다.
        return photoUsageHistoryRepository.findById(request.deviceId())
                .switchIfEmpty(Mono.defer(() -> Mono.just(new PhotoUsageHistory(request.deviceId()))))
                .flatMap(photoUsageHistory -> {
                    return photoRepository.findById(request.photoId())
                            .flatMap(photo -> {
                                // photoUsageList 내부에 동일한 photoId가 있는지 확인
                                Optional<PhotoUsage> existingUsage = photoUsageHistory.getPhotoUsageList().stream()
                                        .filter(usage -> usage.getPhotoId().equals(photo.getId()))
                                        .findFirst();
                                if (existingUsage.isPresent()) {
                                    // 이미 존재하는 경우 lastUsedAt 갱신
                                    existingUsage.get().setLastUsedAt(LocalDateTime.now());
                                } else {
                                    // 존재하지 않으면 새로 추가
                                    photoUsageHistory.addPhotoUsage(
                                            photo.getId(),
                                            LocalDateTime.now(),
                                            photo.getOriginImageUrl(),
                                            photo.getGuideImageUrl(),
                                            photo.getMaskImageUrl()
                                    );
                                }
                                // 최신순으로 정렬(내림차순 정렬)
                                photoUsageHistory.getPhotoUsageList().sort(
                                        Comparator.comparing(PhotoUsage::getLastUsedAt).reversed()
                                );
                                // 갱신된 또는 추가된 photoUsageHistory 저장
                                return photoUsageHistoryRepository.save(photoUsageHistory);
                            });
                }).then();

    }

    /**
     * 태그를 검색하면 태그가 포함된 mongodb에서 사진데이터를 응답하는 service
     * @param tag
     * @return
     */
    public Flux<PhotoDTO> searchTag(String tag){

        return photoRepository.findByTagContaining(tag)
                .map(Photo::toPhotoDTO);
    }

    public Mono<Photo> searchId(String id){

        return photoRepository.findById(id)
                .doOnNext(photo -> log.info("Fetched photo: {}", photo));

    }


}
