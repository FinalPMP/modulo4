package com.retrocore.mod4.lambda_consumer_monitoring.application.usecase;

import com.retrocore.mod4.lambda_consumer_monitoring.domain.model.MonitoringEvent;
import com.retrocore.mod4.lambda_consumer_monitoring.domain.port.MonitoringProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessMonitoringEventUseCase implements MonitoringProcessor {

    private static final Logger log = LoggerFactory.getLogger(ProcessMonitoringEventUseCase.class);

    @Override
    public void process(MonitoringEvent event) {
        log.info("[MONITORING] user={} type={} msg={} ts={}",
                event.userId(),
                event.rawType(),
                event.message(),
                event.timestamp()
        );
    }
}
