package com.retrocore.mod4.lambda_consumer_analytics.domain.port;

import com.retrocore.mod4.lambda_consumer_analytics.domain.model.AnalyticsEvent;

public interface AnalyticsProcessor {
    void process(AnalyticsEvent event);
}
