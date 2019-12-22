package com.github.nicdesousa.telemetry.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Car {
    @Builder.Default
    private int carIndex = -1;
    private Location curLocation;
    @Builder.Default
    private int position = 0;
    @Builder.Default
    private double totalDistance = 0D;
    @Builder.Default
    private long lastUpdateTimestamp = 0L;
    @Builder.Default
    private double curSpeedMPH = 0D;

    public final void addDistance(final double distance) {
        this.setTotalDistance(this.getTotalDistance() + distance);
    }

    public static Car from(final CarCoordinate carCoordinate) {
        return Car.builder().carIndex(carCoordinate.getCarIndex()).curLocation(carCoordinate.getLocation())
                .lastUpdateTimestamp(carCoordinate.getTimestamp()).build();
    }

}

