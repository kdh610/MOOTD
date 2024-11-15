package com.bwd4.mootd.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bwd4.mootd.common.exception.BusinessException;
import com.bwd4.mootd.common.exception.ErrorCode;
import com.bwd4.mootd.enums.ImageType;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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


}
