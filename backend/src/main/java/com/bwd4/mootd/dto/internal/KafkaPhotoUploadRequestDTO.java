package com.bwd4.mootd.dto.internal;

import com.bwd4.mootd.dto.request.PhotoUploadRequestDTO;


import java.io.IOException;

public record KafkaPhotoUploadRequestDTO(
        byte[] originImageData, // 파일 데이터를 byte 배열로 저장
        String name,//파일
        String originImageFilename, // 파일 이름을 따로 저장
        String contentType,
        String deviceId,
        Double latitude,
        Double longitude){

    // 기존의 MultipartFile을 사용하여 DTO를 생성하는 정적 팩토리 메서드
    public static KafkaPhotoUploadRequestDTO fromPhotoUploadRequestDTO(PhotoUploadRequestDTO requestDTO) throws IOException {
        return new KafkaPhotoUploadRequestDTO(
                requestDTO.originImageFile().getBytes(), // 파일을 byte[]로 변환
                requestDTO.originImageFile().getName(),
                requestDTO.originImageFile().getOriginalFilename(), // 파일 이름 저장
                requestDTO.originImageFile().getContentType(),
                requestDTO.deviceId(),
                requestDTO.latitude(),
                requestDTO.longitude()
        );
    }
}