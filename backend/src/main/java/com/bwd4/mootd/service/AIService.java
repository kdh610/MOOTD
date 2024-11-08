package com.bwd4.mootd.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Map;

@Service
public class AIService {

    @Value("${ai.host}")
    private String HOST;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        this.webClient = WebClient.builder()
                .baseUrl("http://" + HOST + ":8000")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(java.time.Duration.ofMinutes(2))
                ))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 2MB로 설정
                .build();
    }

    public Mono<byte[]> maskImage(String url) {
        return webClient.post()
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
                    return bytes;
                });
    }
}
