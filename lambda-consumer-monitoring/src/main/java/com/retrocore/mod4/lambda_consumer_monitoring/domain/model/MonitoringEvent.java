package com.retrocore.mod4.lambda_consumer_monitoring.domain.model;

import java.time.Instant;

public record MonitoringEvent(
        String userId,
        String message,
        String rawType,
        Instant timestamp
) {}
