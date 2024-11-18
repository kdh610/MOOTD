package com.bwd4.mootd.service;

import com.bwd4.mootd.common.exception.BusinessException;
import com.bwd4.mootd.common.exception.ErrorCode;
import com.bwd4.mootd.domain.*;
import com.bwd4.mootd.dto.internal.KafkaPhotoUploadRequestDTO;
import com.bwd4.mootd.dto.internal.UploadResult;
import com.bwd4.mootd.dto.request.PhotoUsageRequestDTO;
import com.bwd4.mootd.dto.response.GuideLineResponseDTO;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import com.bwd4.mootd.dto.response.PhotoDetailDTO;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import com.bwd4.mootd.dto.response.TagSearchTestDTO;
import com.bwd4.mootd.enums.ImageType;
import com.bwd4.mootd.repository.PhotoElasticSearchRepository;
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
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
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
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final S3Service s3Service; // S3 업로드용 서비스
    private final Scheduler asyncScheduler; // 비동기 스케줄러 주입
    private final PhotoUsageHistoryRepository photoUsageHistoryRepository;
    private final AIService aiService;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final PhotoElasticSearchRepository photoElasticSearchRepository;



    @Autowired
    public PhotoService(PhotoRepository photoRepository, S3Service s3Service, Scheduler asyncScheduler, PhotoUsageHistoryRepository photoUsageHistoryRepository, AIService aiService, ReactiveMongoTemplate reactiveMongoTemplate, PhotoElasticSearchRepository photoElasticSearchRepository) {
        this.photoRepository = photoRepository;
        this.s3Service = s3Service;
        this.asyncScheduler = asyncScheduler;
        this.photoUsageHistoryRepository = photoUsageHistoryRepository;
        this.aiService = aiService;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.photoElasticSearchRepository = photoElasticSearchRepository;
    }

    /**
     * 이미지를 업로드하는 로직, 많은 ai모델을 활용합니다.
     *
     * @param request
     * @return
     */
    @KafkaListener(topics = "photo-upload-topic", groupId = "photo-consumer-group")
    public Mono<Void> uploadPhotoLogics(KafkaPhotoUploadRequestDTO request) {
        return Mono.fromCallable(() -> copyFileAndExtractMetaData(request))
                .flatMap(result -> saveInitialPhotoMetadata(result, request))
                .flatMap(this::processMaskAndUpdatePhoto)
                .flatMap(photo -> analyzeImageTagAndUpdatePhoto(photo, request))
                .flatMap(photo -> makeGuideLineAndUpdatePhoto(photo, request))
                .subscribeOn(asyncScheduler);
    }

    private Mono<Void> makeGuideLineAndUpdatePhoto(Photo photo, KafkaPhotoUploadRequestDTO request) {
        return this.makeGuideLine(
                new MockMultipartFile(  request.name(),
                    request.originImageFilename(),
                    request.contentType(),
                    request.originImageData()))
                .flatMap(guideLineUrls -> {
                            photo.setPersonGuidelineUrl(guideLineUrls.personGuideLineURL());
                            photo.setBackgroundGuidelineUrl(guideLineUrls.backgroundGuideLineURL());
                            return photoRepository.save(photo);
                        })
                .doOnSuccess(updatedPhoto -> log.info("가이드라인 추가된 Photo 객체: {}", updatedPhoto))
                .then();
    }

    private Mono<Photo> analyzeImageTagAndUpdatePhoto(Photo photo, KafkaPhotoUploadRequestDTO request) {
        return aiService.analyzeImageTag(request)
                .flatMap(response -> {
                    photo.setTag(response.keywords());
                    return photoRepository.save(photo);
                })
                .doOnSuccess(updatedPhoto -> log.info("태그가 추가된 Photo 객체: {}", updatedPhoto));

    }

    /**
     * masking처리하고 해당 이미지를 기존 mongoDB Document에 저장하는 메서드
     *
     * @param photo
     * @return
     */
    private Mono<Photo> processMaskAndUpdatePhoto(Photo photo) {
        return aiService.maskImage(photo.getOriginImageUrl())
                .flatMap(maskBytes -> Mono.fromCallable(
                        () -> s3Service.upload(maskBytes, "masked_file.png", ImageType.MASKING)))
                .flatMap(maskUrl -> {
                    photo.setMaskImageUrl(maskUrl);
                    return photoRepository.save(photo);
                })
                .doOnSuccess(updatedPhoto -> log.info("마스크 이미지 추가 저장된 Photo 객체: {}", updatedPhoto));
    }

    /**
     * 초기 originImageUrl를 기준으로 mongoDB에 저장하는 메서드
     *
     * @param result
     * @param request
     * @return
     */
    private Mono<Photo> saveInitialPhotoMetadata(UploadResult result, KafkaPhotoUploadRequestDTO request) {
        Photo photo = new Photo();
        photo.setDeviceId(request.deviceId());
        photo.setCreatedAt(result.createdAt());
        photo.setCoordinates(result.longitude(), result.latitude());
        photo.setOriginImageUrl(result.originImageUrl());

        return photoRepository.save(photo)
                .doOnSuccess(savedPhoto -> log.info("초기 MongoDB에 저장된 Photo 객체: {}", savedPhoto))
                .doOnError(error -> log.error("MongoDB 초기 저장 중 오류 발생: ", error));
    }

    /**
     * S3에 업로드하기 위해 메타데이터를 추줄하는 메서드
     *
     * @param request
     * @return
     * @throws IOException
     */
    private UploadResult copyFileAndExtractMetaData(KafkaPhotoUploadRequestDTO request) throws IOException {
        byte[] fileBytes = request.originImageData();
        MultipartFile copiedFile = new MockMultipartFile(
                request.name(),
                request.originImageFilename(),
                request.contentType(),
                fileBytes
        );
        // 메타정보 추출
        Map<String, Object> metadata = extractMetadata(new ByteArrayInputStream(fileBytes));

        LocalDateTime createdAt = (LocalDateTime) metadata.get("CaptureTime");
        log.info(request.latitude() + " " + request.longitude());
        // 이미지 S3 업로드
        String imageUrl = s3Service.upload(fileBytes, "파일명.png", ImageType.ORIGINAL);

        //마스크 처리
        return new UploadResult(imageUrl, createdAt, request.latitude(), request.longitude());
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
                .filter(photo -> photo.getBackgroundGuidelineUrl() != null) // BackgroundGuidelineUrl이 null이 아닌 경우에만 필터링
                .map(photo -> new MapResponseDTO(
                        photo.getId(),
                        photo.getMaskImageUrl(),
                        photo.getPersonGuidelineUrl(),
                        photo.getBackgroundGuidelineUrl(),
                        photo.getCoordinates().getY(),  // latitude
                        photo.getCoordinates().getX()   // longitude
                ));
    }

    public Mono<List<PhotoUsage>> getRecentUsageByDeviceId(String deviceId) {
        return photoUsageHistoryRepository.findById(deviceId)
                .map(PhotoUsageHistory::getPhotoUsageList)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.RECENT_USAGE_NOT_FOUND)));

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
                                            photo.getPersonGuidelineUrl(),
                                            photo.getBackgroundGuidelineUrl(),
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

    /** MongoDB
     * 태그를 검색하면 태그가 포함된 mongodb에서 모든 사진데이터를 응답하는 service
     *
     * @param tag
     * @return
     */
    public Flux<TagSearchResponseDTO> findMongoByTag(String tag) {

        return photoRepository.findByTagContaining(tag)
                .map(Photo::toTagSearchResponseDTO);
    }

    /** MongoDB
     * 태그검색에 따라 최대 limit의 수만큼 데이터 반환
     * @param tag
     * @param limit
     * @return
     */
    public Flux<TagSearchResponseDTO> findMongoByTagContainingWithLimit(String tag, int limit) {
        // Create a query to find PhotoTest with the given tag
        Query query = new Query(Criteria.where("tag").regex(tag));

        // Apply limit
        query.limit(limit);

        // Execute the query with ReactiveMongoTemplate
        return reactiveMongoTemplate.find(query, Photo.class)
                .map(Photo::toTagSearchResponseDTO);
    }

    /** ElasticSearch
     * 태그검색에 따라 모든 데이터 반환
     * @param tag
     * @return
     */
    public Flux<TagSearchResponseDTO> findEsByTag(String tag) {
        return photoElasticSearchRepository.findByTag(tag)
                .map(PhotoEs::toTagSearchResponseDTO);
    }

    /** ElasticSearch
     * 태그검색에 따라 최대 limit의 수만큼 데이터 반환
     * @param tag
     * @param limit
     * @return
     */
    public Flux<TagSearchResponseDTO> findEsByTagWithLimit(String tag, int limit) {
        return photoElasticSearchRepository.findByTag(tag).take(limit)
                .map(PhotoEs::toTagSearchResponseDTO);
    }




    public Mono<PhotoDetailDTO> findPhotoDetail(String photoId) {
        return photoRepository.findById(photoId)
                .map(photo -> new PhotoDetailDTO(photo.getId(),
                        photo.getMaskImageUrl(),
                        photo.getPersonGuidelineUrl(),
                        photo.getBackgroundGuidelineUrl(),
                        photo.getCoordinates().getY(),
                        photo.getCoordinates().getX())
                );
    }

    public Mono<GuideLineResponseDTO> makeGuideLine(MultipartFile originImageFile) {
        return aiService.makeGuideLine(originImageFile)
                .flatMap(afterModel -> {
                    // background_edge 업로드
                    Mono<String> backgroundGuideUrlMono = uploadEdgeToS3(afterModel.background_edge(),
                            "backgroundGuideLine.png", ImageType.BACKGROUND);

                    // person_edge 업로드
                    Mono<String> personGuideUrlMono = uploadEdgeToS3(afterModel.person_edge(),
                            "personGuideLine.png", ImageType.PEOPLE);

                    // 두 작업 완료 후 결과 조합
                    return Mono.zip(backgroundGuideUrlMono, personGuideUrlMono)
                            .map(tuple -> new GuideLineResponseDTO(tuple.getT2(), tuple.getT1()));
                });
    }

    // 블로킹 작업을 비동기로 처리하는 S3 업로드 메서드
    private Mono<String> uploadEdgeToS3(String edgeData, String fileName, ImageType imageType) {
        if (edgeData == null) {
            return Mono.just(null); // edgeData가 없으면 null 반환
        }
        return Mono.fromCallable(() -> s3Service.uploadBase64(edgeData, fileName, imageType))
                .subscribeOn(Schedulers.boundedElastic()) // 블로킹 작업을 별도 스레드 풀에서 처리
                .onErrorMap(IOException.class, e -> new RuntimeException("S3 업로드 실패", e));
    }
}
