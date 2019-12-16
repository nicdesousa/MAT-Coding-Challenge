package com.github.nicdesousa.telemetry.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CarCoordinate {

    private int carIndex = 0;
    private Location location = null;
    private long timestamp = 0L;

}