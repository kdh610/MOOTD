package com.bwd4.mootd.infra.kafka;

import com.bwd4.mootd.dto.internal.KafkaPhotoUploadRequestDTO;
import com.bwd4.mootd.dto.request.PhotoUploadRequestDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service  // Spring의 서비스 계층을 나타내는 애노테이션
public class ProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "photo-upload-topic";

    //KafkaTemplate을 주입받아 초기화
    public ProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Kafka에 메시지를 비동기로 전송하는 메서드
    public Mono<Void> sendPhotoUploadRequest(PhotoUploadRequestDTO request) {
        return Mono.fromRunnable(() ->
                {
                    try {
                        // 지정된 토픽에 메시지를 전송
                        kafkaTemplate.send(TOPIC, KafkaPhotoUploadRequestDTO.fromPhotoUploadRequestDTO(request));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

}