package com.github.nicdesousa.telemetry.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RegisterForReflection
public class Event {
    private long timestamp = 0L;
    private String text;
}
