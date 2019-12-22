package com.github.nicdesousa.telemetry.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RegisterForReflection
public class CarCoordinate {
    private int carIndex = 0;
    private Location location = null;
    private long timestamp = 0L;
}
