package com.bwd4.mootd.service;

import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.dto.internal.UploadResult;
import com.bwd4.mootd.dto.request.PhotoUploadRequestDTO;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import com.bwd4.mootd.enums.ImageType;
import com.bwd4.mootd.repository.PhotoRepository;
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
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final S3Service s3Service; // S3 업로드용 서비스
    private final Scheduler asyncScheduler; // 비동기 스케줄러 주입

    @Autowired
    public PhotoService(PhotoRepository photoRepository, S3Service s3Service, Scheduler asyncScheduler) {
        this.photoRepository = photoRepository;
        this.s3Service = s3Service;
        this.asyncScheduler = asyncScheduler;
    }

    public Mono<Void> uploadPhotoLogics(PhotoUploadRequestDTO request) {
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
                    Double latitude = (Double) metadata.get("Latitude");
                    Double longitude = (Double) metadata.get("Longitude");
                    log.info(latitude + " " + longitude);
                    // 이미지 S3 업로드
                    String imageUrl = s3Service.upload(copiedFile, ImageType.ORIGINAL);
                    return new UploadResult(imageUrl, createdAt, latitude, longitude);
                })
                .flatMap(result -> {
                    // MongoDB에 메타정보와 이미지 URL 저장
                    Photo photo = new Photo();
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
                        photo.getOriginImageUrl(),
                        photo.getCoordinates().getY(),  // latitude
                        photo.getCoordinates().getX()   // longitude
                ));
    }
}
