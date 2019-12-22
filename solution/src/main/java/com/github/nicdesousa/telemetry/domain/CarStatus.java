package com.github.nicdesousa.telemetry.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RegisterForReflection
public class CarStatus {
    private long timestamp = 0L;
    private int carIndex = -1;
    private TypeEnum type = TypeEnum.SPEED;
    private double value = 0D;

    public CarStatus(final Car car, final TypeEnum carStatusType) {
        this.setTimestamp(car.getLastUpdateTimestamp());
        this.setCarIndex(car.getCarIndex());
        this.setType(carStatusType);
        if (this.getType() == TypeEnum.SPEED) {
            this.setValue(car.getCurSpeedMPH());
        } else {
            this.setValue(car.getPosition());
        }
    }

    public enum TypeEnum {
        SPEED,
        POSITION
    }
}
