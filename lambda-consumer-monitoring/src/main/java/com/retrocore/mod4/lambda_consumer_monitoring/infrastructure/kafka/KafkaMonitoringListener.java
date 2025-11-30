package com.retrocore.mod4.lambda_consumer_monitoring.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retrocore.mod4.lambda_consumer_monitoring.domain.model.MonitoringEvent;
import com.retrocore.mod4.lambda_consumer_monitoring.domain.port.MonitoringProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class KafkaMonitoringListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaMonitoringListener.class);

    private final MonitoringProcessor monitoringProcessor;
    private final ObjectMapper objectMapper;

    public KafkaMonitoringListener(MonitoringProcessor monitoringProcessor, ObjectMapper objectMapper) {
        this.monitoringProcessor = monitoringProcessor;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(ConsumerRecord<String, String> record) {
        String payloadJson = record.value();

        try {
            IncomingEvent incoming = objectMapper.readValue(payloadJson, IncomingEvent.class);

            if (!"MONITORING".equalsIgnoreCase(incoming.type())) {
                return;
            }

            MonitoringEvent event = new MonitoringEvent(
                    incoming.userId(),
                    incoming.message(),
                    incoming.type(),
                    incoming.timestamp()
            );

            monitoringProcessor.process(event);

        } catch (JsonProcessingException e) {
            log.error("Erro ao desserializar mensagem de monitoring: {}", payloadJson, e);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem de monitoring: {}", payloadJson, e);
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
