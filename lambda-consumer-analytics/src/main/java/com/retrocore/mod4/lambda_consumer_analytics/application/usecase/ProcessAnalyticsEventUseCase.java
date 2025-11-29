package com.retrocore.mod4.lambda_consumer_analytics.application.usecase;

import com.retrocore.mod4.lambda_consumer_analytics.domain.model.AnalyticsEvent;
import com.retrocore.mod4.lambda_consumer_analytics.domain.port.AnalyticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessAnalyticsEventUseCase implements AnalyticsProcessor {

    private static final Logger log = LoggerFactory.getLogger(ProcessAnalyticsEventUseCase.class);

    @Override
    public void process(AnalyticsEvent event) {
        log.info("[ANALYTICS] user={} source={} msg={} type={} ts={}",
                event.userId(),
                event.source(),
                event.message(),
                event.rawType(),
                event.timestamp()
        );
    }
}
