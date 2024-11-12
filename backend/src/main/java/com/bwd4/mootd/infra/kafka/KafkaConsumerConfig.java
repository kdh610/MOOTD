package com.bwd4.mootd.infra.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka  // Kafka 리스너를 활성화하는 애노테이션
@Configuration  // Spring Boot 설정 클래스임을 나타냄
public class KafkaConsumerConfig {

    // Kafka 컨슈머 팩토리를 설정하는 메서드
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Kafka 브로커 서버 주소 설정
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // 컨슈머 그룹 ID 설정
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "webflux-consumer-group");

        // 메시지 키와 값을 역직렬화하는 클래스 지정
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // 메시지를 처음부터 읽도록 설정
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        //신뢰할 수 있는 패키지 설정
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.bwd4.mootd.dto.internal");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    // Kafka 리스너 컨테이너 팩토리를 설정하여 여러 리스너가 동시에 동작할 수 있게 함
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());  // 컨슈머 팩토리를 설정
        return factory;
    }
}
