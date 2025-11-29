package com.retrocore.mod4.lambda_producer.api;

import com.retrocore.mod4.lambda_producer.application.usecase.PublishEventUseCase;
import com.retrocore.mod4.lambda_producer.domain.model.EventPayload;
import com.retrocore.mod4.lambda_producer.domain.model.EventType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/events")
public class MessageController {

    private final PublishEventUseCase publishEventUseCase;

    public MessageController(PublishEventUseCase publishEventUseCase) {
        this.publishEventUseCase = publishEventUseCase;
    }

    @PostMapping
    public String publish(@RequestBody EventRequest request) {
        EventPayload payload = new EventPayload(
                request.type(),
                request.userId(),
                request.source(),
                request.message(),
                Instant.now()
        );

        publishEventUseCase.execute(payload);

        return "Evento publicado com sucesso para " + request.type();
    }

    public record EventRequest(
            EventType type,
            String userId,
            String source,
            String message
    ) {}
}
