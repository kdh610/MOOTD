package com.bwd4.mootd.service;

import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.dto.internal.UploadResult;
import com.bwd4.mootd.dto.request.PhotoUploadRequestDto;
import com.bwd4.mootd.dto.request.UploadTestRequestDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

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

    @Autowired
    public PhotoService(PhotoRepository photoRepository, S3Service s3Service) {
        this.photoRepository = photoRepository;
        this.s3Service = s3Service;
    }

    public Mono<Void> uploadPhotoLogics(PhotoUploadRequestDto request) {
        log.info("in service");
        if(request.originImageFile().isEmpty()){
            log.info("image file is null");
        }
        return Mono.fromCallable(() -> {
                    // 메타정보 추출
                    Map<String, Object> metadata = extractMetadata(request.originImageFile());
                    LocalDateTime createdAt = (LocalDateTime) metadata.get("CaptureTime");
                    Double latitude = (Double) metadata.get("Latitude");
                    Double longitude = (Double) metadata.get("Longitude");
                    log.info(latitude + " " + longitude);
                    // 이미지 S3 업로드
                    String imageUrl = s3Service.upload(request.originImageFile(), ImageType.ORIGINAL);
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
                .doOnError(error -> log.error("오류 발생: ", error));
    }

    private Map<String, Object> extractMetadata(MultipartFile file) {
        Map<String, Object> metadataMap = new HashMap<>();
        // 메타데이터 추출 로직 구현
        try {
            // 이미지 파일의 메타데이터 읽기
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());

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
            e.printStackTrace();
        } catch (ImageProcessingException e) {
            throw new RuntimeException(e);
        }
        return metadataMap;

    }
}
