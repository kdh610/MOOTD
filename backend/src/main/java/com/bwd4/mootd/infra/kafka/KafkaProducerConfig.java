package com.bwd4.mootd.infra.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration  // Spring Boot 설정 클래스임을 나타냄
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Kafka 프로듀서의 설정을 정의하는 메서드
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        // Kafka 프로듀서의 설정 값들을 저장할 맵 생성
        Map<String, Object> configProps = new HashMap<>();

        // Kafka 브로커 서버 주소 설정
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // 메시지 키와 값을 직렬화하는 클래스 지정
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // JsonSerializer 사용

        // 최대 용량설정
        configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10 * 1024 * 1024); // 10MB


        return new DefaultKafkaProducerFactory<>(configProps);  // 설정을 기반으로 프로듀서 팩토리 생성
    }

    // KafkaTemplate을 빈으로 등록하여 메시지를 Kafka에 전송할 수 있게 함
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
