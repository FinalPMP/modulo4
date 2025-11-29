package com.retrocore.mod4.lambda_producer.domain.model;

import java.time.Instant;

public record EventPayload(
        EventType type,
        String userId,
        String source,
        String message,
        Instant timestamp
) {}
