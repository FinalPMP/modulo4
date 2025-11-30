package com.retrocore.mod4.lambda_consumer_monitoring.domain.port;

import com.retrocore.mod4.lambda_consumer_monitoring.domain.model.MonitoringEvent;

public interface MonitoringProcessor {
    void process(MonitoringEvent event);
}
