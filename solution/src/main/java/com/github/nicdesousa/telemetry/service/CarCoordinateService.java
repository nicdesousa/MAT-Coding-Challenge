package com.github.nicdesousa.telemetry.service;

import com.github.nicdesousa.telemetry.domain.CarCoordinate;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.kafka.KafkaMessage;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
@ApplicationScoped
public class CarCoordinateService {

    @Inject
    public TelemetryService telemetryService;

    @Incoming("mqtt-carCoordinates-sub")
    @Outgoing("kafka-carCoordinates-pub")
    @Broadcast
    public KafkaMessage<Long, JsonObject> consumeMqttCarCoordinate(byte[] raw) {
        CarCoordinate carCoordinate = Json.decodeValue(new String(raw), CarCoordinate.class);
        log.debug("Received MQTT CarCoordinate: {}", carCoordinate);
        // publish carCoordinate to Kafka
        return KafkaMessage.of(carCoordinate.getTimestamp(), JsonObject.mapFrom(carCoordinate));
    }

    @Incoming("kafka-carCoordinates-sub")
    public CompletionStage<Void> consumeKafkaCarCoordinateAsync(KafkaMessage<Long, JsonObject> message) {
        return CompletableFuture.runAsync(() -> {
            CarCoordinate carCoordinate = message.getPayload().mapTo(CarCoordinate.class);
            log.debug("Received Kafka CarCoordinate: {}", carCoordinate.toString());
            // process the carCoordinate
            telemetryService.processCarCoordinate(carCoordinate);
        });
    }
}
