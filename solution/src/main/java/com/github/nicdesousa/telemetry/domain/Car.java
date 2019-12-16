package com.github.nicdesousa.mat.domain;

import com.github.nicdesousa.telemetry.domain.CarCoordinate;
import com.github.nicdesousa.telemetry.domain.Location;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Car {
    private int carIndex = -1;
    private Location curLocation;
    private int position = 0;
    private double totalDistance = 0D;
    private long lastUpdateTimestamp = 0L;
    private double curSpeedKPH = 0D;
    private double curSpeedMPH = 0D;

    public Car(final CarCoordinate carCoordinate) {
        carIndex = carCoordinate.getCarIndex();
        this.setCurLocation(carCoordinate.getLocation());
        this.setLastUpdateTimestamp(carCoordinate.getTimestamp());
    }

}
