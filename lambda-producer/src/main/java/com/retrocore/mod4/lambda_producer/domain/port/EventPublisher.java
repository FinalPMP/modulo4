package com.retrocore.mod4.lambda_producer.domain.port;

import com.retrocore.mod4.lambda_producer.domain.model.EventPayload;

public interface EventPublisher {
    void publish(EventPayload event);
}
