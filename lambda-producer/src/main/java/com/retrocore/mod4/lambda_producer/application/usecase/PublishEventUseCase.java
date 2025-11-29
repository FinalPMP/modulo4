package com.retrocore.mod4.lambda_producer.application.usecase;

import com.retrocore.mod4.lambda_producer.domain.model.EventPayload;
import com.retrocore.mod4.lambda_producer.domain.port.EventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PublishEventUseCase {

    private final EventPublisher eventPublisher;

    public PublishEventUseCase(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void execute(EventPayload payload) {
        if (payload.userId() == null || payload.userId().isBlank()) {
            throw new IllegalArgumentException("userId é obrigatório");
        }

        eventPublisher.publish(payload);
    }
}
