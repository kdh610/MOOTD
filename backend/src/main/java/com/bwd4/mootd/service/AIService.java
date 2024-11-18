package com.bwd4.mootd.service;

import com.bwd4.mootd.dto.internal.GuideLineModelResponseDTO;
import com.bwd4.mootd.dto.internal.KafkaPhotoUploadRequestDTO;
import com.bwd4.mootd.dto.response.AiTagDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
public class AIService {

    @Value("${ai.host}")
    private String HOST;

    private WebClient maskAIClient;

    private WebClient tagAIClient;

    private WebClient guideLineAiClient;

    @PostConstruct
    private void init() {
        this.maskAIClient = WebClient.builder()
                .baseUrl("http://" + HOST + ":8000")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(java.time.Duration.ofMinutes(2))
                ))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                .build();

        this.tagAIClient = WebClient.builder()
                .baseUrl("http://" + HOST + ":8001")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(java.time.Duration.ofMinutes(2))
                ))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                .build();

        this.guideLineAiClient = WebClient.builder()
//                .baseUrl("http://" + "70.12.130.101:61423/")
                .baseUrl("http://" + HOST + ":8002")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(java.time.Duration.ofMinutes(2))
                ))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                .build();
    }

    public Mono<byte[]> maskImage(String url) {
        return maskAIClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/process_image/")
                        .queryParam("url", url) // url을 query parameter로 추가
                        .build())
                .accept(MediaType.IMAGE_JPEG) // 예상 이미지 타입에 맞게 설정
                .retrieve()
                .bodyToMono(DataBuffer.class)
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });
    }

    public Mono<AiTagDTO> analyzeImageTag(KafkaPhotoUploadRequestDTO request) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(request.originImageData()))
                .header("Content-Disposition", "form-data; name=file; filename=" + request.originImageFilename())
                .contentType(MediaType.parseMediaType(request.contentType()));

        return tagAIClient.post()
                .uri("/upload-image/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build()) // MultipartFile을 multipart/form-data로 전송
                .accept(MediaType.APPLICATION_JSON) // JSON 응답 형식 설정
                .retrieve()
                .bodyToMono(AiTagDTO.class); // JSON 응답을 AiTagDTO로 매핑
    }

    /**
     * 가이드라인을 생성한다.
     * @param originImageFile
     * @return
     */
    public Mono<GuideLineModelResponseDTO> makeGuideLine(MultipartFile originImageFile) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", originImageFile.getResource()) // MultipartFile의 Resource 사용
                .header("Content-Disposition", "form-data; name=file; filename=" + originImageFile.getOriginalFilename())
                .contentType(MediaType.parseMediaType(originImageFile.getContentType()));

        return guideLineAiClient.post()
                .uri("/process_image/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build()) // MultipartBodyBuilder로 multipart/form-data 생성
                .accept(MediaType.APPLICATION_JSON) // JSON 응답 형식 설정
                .retrieve()
                .bodyToMono(GuideLineModelResponseDTO.class); // 응답을 GuideLineModelResponseDTO로 매핑
    }
}
