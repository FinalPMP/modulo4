package com.retrocore.mod4.lambda_consumer_analytics.domain.model;

import java.time.Instant;

public record AnalyticsEvent(
        String userId,
        String source,
        String message,
        String rawType,
        Instant timestamp
) {}
