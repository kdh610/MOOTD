package com.bwd4.mootd.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.bwd4.mootd.common.exception.BusinessException;
import com.bwd4.mootd.common.exception.ErrorCode;
import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.enums.ImageType;

import java.io.*;
import java.util.Base64;

import com.bwd4.mootd.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final PhotoRepository photoRepository;

    // 파일 업로드 요청
    public String upload(MultipartFile file, ImageType imageType) throws IOException {
        if(file.isEmpty() || Objects.isNull(file.getOriginalFilename())) {
            throw new BusinessException(ErrorCode.EMPTY_FILE);
        }
        return this.uploadImage(file.getInputStream(), file.getOriginalFilename(), file.getSize(), file.getContentType(), imageType);
    }

    // byte[] 업로드 메서드 (오버로딩)
    public String upload(byte[] fileData, String originalFilename, ImageType imageType) throws IOException {
        if (fileData == null || fileData.length == 0 || originalFilename == null) {
            throw new BusinessException(ErrorCode.EMPTY_FILE);
        }
        String contentType = "image/" + originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        return this.uploadImage(new ByteArrayInputStream(fileData), originalFilename, fileData.length, contentType, imageType);
    }

    // 파일 확장자 확인 & S3저장
    private String uploadImage(InputStream inputStream, String originalFilename, long contentLength, String contentType,ImageType imageType) throws IOException {
        this.validateImageFileExtention(originalFilename);
        return this.uploadImageToS3(inputStream, originalFilename, contentLength, contentType, imageType);
    }

    // 이미지파일이 유효한 확장자인지 확인
    private void validateImageFileExtention(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new BusinessException(ErrorCode.NO_FILE_EXTENTION);
        }

        String extention = filename.substring(lastDotIndex + 1).toLowerCase();
        List<String> allowedExtentionList = Arrays.asList("jpg", "jpeg", "png");

        if (!allowedExtentionList.contains(extention)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENTION);
        }
    }

    // S3에 이미지 저장
    public String uploadImageToS3(InputStream inputStream, String originalFilename, long contentLength, String contentType, ImageType imageType) throws IOException {
        String fileName = UUID.randomUUID().toString().substring(0,10) + originalFilename;
        String path = imageType+"/"+fileName;

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        objectMetadata.setContentLength(contentLength);

        try(inputStream){
            amazonS3.putObject(new PutObjectRequest(bucketName, path, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        }catch (IOException e){
            throw new BusinessException(ErrorCode.FAIL_UPLOAD_S3);
        }
        return amazonS3.getUrl(bucketName,path).toString();
    }

    public void deleteImage(String imageAddress){
        String key = getKeyFromImageAddress(imageAddress);
        try{
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        }catch (Exception e){
            throw new BusinessException(ErrorCode.FAIL_DELETE_S3);
        }
    }


    private String getKeyFromImageAddress(String imageAddress){
        try{
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
            return decodingKey.substring(1);
        }catch (MalformedURLException | UnsupportedEncodingException e){
            throw new BusinessException(ErrorCode.FAIL_DELETE_S3);
        }

    }

    public String uploadBase64(String base64Data, String originalFilename, ImageType imageType) throws IOException {
        if (base64Data == null || base64Data.isEmpty() || originalFilename == null) {
            throw new BusinessException(ErrorCode.EMPTY_FILE);
        }

        // Base64 디코딩
        byte[] decodedBytes = decodeBase64(base64Data);

        // 파일 타입 추출
        String contentType = "image/" + originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        // 기존 메서드를 재사용하여 업로드
        return this.uploadImage(new ByteArrayInputStream(decodedBytes), originalFilename, decodedBytes.length, contentType, imageType);
    }

    private byte[] decodeBase64(String base64Data) {
        try {
            return Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_BASE64_DATA);
        }
    }

    public Mono<Void> generateThumbnailsForAllPhotos() {
        return photoRepository.findAll()
                .concatMap(this::processPhoto) // 순차적으로 처리
                .then();
    }

    private Mono<Photo> processPhoto(Photo photo) {
        return Mono.fromCallable(() -> {
                    if(photo.getThumbnailUrl() != null) return photo;
                    String maskImageUrl = photo.getMaskImageUrl();
                    String thumbnailUrl = uploadAndGetThumbnailUrl(maskImageUrl);
                    // DB에 썸네일 URL 업데이트
                    photo.setThumbnailUrl(thumbnailUrl);

                    return photo;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(photoRepository::save);
    }

    public String uploadAndGetThumbnailUrl(String imageUrl) throws IOException {

        String imageKey = extractKeyFromUrl(imageUrl);
        // S3에서 원본 이미지 가져오기
        S3Object s3Object = amazonS3.getObject(bucketName, imageKey);
        InputStream originalImageStream = s3Object.getObjectContent();

        // 썸네일 생성
        ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
        Thumbnails.of(originalImageStream)
                .size(300, 300) // 썸네일 크기
                .outputFormat("png") // 썸네일 포맷
                .toOutputStream(thumbnailStream);

        // 썸네일 업로드
        String thumbnailKey = "thumbnail/" + imageKey.substring(imageKey.lastIndexOf("/") + 1);
        String thumbnailUrl = uploadImageToS3(
                new ByteArrayInputStream(thumbnailStream.toByteArray()), // InputStream
                thumbnailKey, // 파일명 또는 경로
                thumbnailStream.size(), // 파일 크기
                "image/png", // MIME 타입
                ImageType.THUMBNAIL // ImageType
        );
        return thumbnailUrl;
    }

    private String extractKeyFromUrl(String url) {
        String baseUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/";
        if (url.startsWith(baseUrl)) {
            return url.substring(baseUrl.length());
        }
        throw new IllegalArgumentException("Invalid S3 URL: " + url);
    }

    String createAndUploadThumbnail(byte[] fileBytes, String originalKey) throws IOException {
        // 썸네일 생성
        ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(fileBytes))
                .size(300, 300) // 썸네일 크기
                .outputFormat("png") // 썸네일 포맷
                .toOutputStream(thumbnailStream);

        // S3에 썸네일 업로드
        String thumbnailKey = "thumbnail/" + originalKey.substring(originalKey.lastIndexOf("/") + 1);
        return this.upload(
                thumbnailStream.toByteArray(), // 파일 데이터
                thumbnailKey, // S3 키
                ImageType.THUMBNAIL // 이미지 타입
        );
    }


}
