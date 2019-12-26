package com.github.nicdesousa.telemetry.domain;

import lombok.Data;

@Data
public class CarLap {
    private long startTime = 0L;
    private long endTime = 0L;
    private double averageSpeed = 0D;
    private double distance = 0D;
}
