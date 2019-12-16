package com.github.nicdesousa.telemetry.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Event {
    private long timestamp = 0L;
    private String text;
}
