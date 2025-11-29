package com.retrocore.mod4.lambda_producer.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retrocore.mod4.lambda_producer.domain.model.EventPayload;
import com.retrocore.mod4.lambda_producer.domain.model.EventType;
import com.retrocore.mod4.lambda_producer.domain.port.EventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String analyticsTopic;
    private final String monitoringTopic;

    public KafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topic.analytics}") String analyticsTopic,
            @Value("${app.kafka.topic.monitoring}") String monitoringTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.analyticsTopic = analyticsTopic;
        this.monitoringTopic = monitoringTopic;
    }

    @Override
    public void publish(EventPayload event) {
        try {
            String payloadJson = objectMapper.writeValueAsString(event);
            String topic = resolveTopic(event.type());

            kafkaTemplate.send(topic, event.userId(), payloadJson);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar EventPayload", e);
        }
    }

    private String resolveTopic(EventType type) {
        return switch (type) {
            case ANALYTICS -> analyticsTopic;
            case MONITORING -> monitoringTopic;
        };
    }
}
