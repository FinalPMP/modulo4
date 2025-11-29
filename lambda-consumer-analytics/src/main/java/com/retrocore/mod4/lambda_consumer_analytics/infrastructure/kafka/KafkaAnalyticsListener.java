package com.retrocore.mod4.lambda_consumer_analytics.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retrocore.mod4.lambda_consumer_analytics.domain.model.AnalyticsEvent;
import com.retrocore.mod4.lambda_consumer_analytics.domain.port.AnalyticsProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class KafkaAnalyticsListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaAnalyticsListener.class);

    private final AnalyticsProcessor analyticsProcessor;
    private final ObjectMapper objectMapper;

    public KafkaAnalyticsListener(AnalyticsProcessor analyticsProcessor, ObjectMapper objectMapper) {
        this.analyticsProcessor = analyticsProcessor;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(ConsumerRecord<String, String> record) {
        String payloadJson = record.value();

        try {
            IncomingEvent incoming = objectMapper.readValue(payloadJson, IncomingEvent.class);

            if (!"ANALYTICS".equalsIgnoreCase(incoming.type())) {
                return;
            }

            AnalyticsEvent event = new AnalyticsEvent(
                    incoming.userId(),
                    incoming.source(),
                    incoming.message(),
                    incoming.type(),
                    incoming.timestamp()
            );

            analyticsProcessor.process(event);

        } catch (JsonProcessingException e) {
            log.error("Erro ao desserializar mensagem de analytics: {}", payloadJson, e);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem de analytics: {}", payloadJson, e);
        }
    }
    private record IncomingEvent(
            String type,
            String userId,
            String source,
            String message,
            Instant timestamp
    ) {}
}
