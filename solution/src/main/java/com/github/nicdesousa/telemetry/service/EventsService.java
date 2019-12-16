package com.github.nicdesousa.telemetry.service;

import com.github.nicdesousa.telemetry.domain.Event;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

@Slf4j
@ApplicationScoped
public class EventsService {

    private FlowableEmitter<Message<JsonObject>> emitter;
    private Flowable<Message<JsonObject>> outgoingStream;

    public void publish(Event event) {
        log.debug("Publishing MQTT Event: {}", event.toString());
        emitter.onNext(Message.of(JsonObject.mapFrom(event)));
    }

    @PostConstruct
    void init() {
        outgoingStream = Flowable.create(emitter -> this.emitter = emitter, BackpressureStrategy.BUFFER);
    }

    @PreDestroy
    void dispose() {
        emitter.onComplete();
    }

    @Outgoing("EventsService")
    Publisher<Message<JsonObject>> produceMessage() {
        return outgoingStream;
    }

    @Incoming("EventsService")
    @Outgoing("mqtt-Events-pub")
    Message<JsonObject> transform(Message<JsonObject> arg) {
        return arg;
    }

}
