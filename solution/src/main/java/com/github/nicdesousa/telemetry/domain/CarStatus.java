package com.github.nicdesousa.telemetry.domain;

import com.github.nicdesousa.mat.domain.Car;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CarStatus {
    private long timestamp = 0L;
    private int carIndex = -1;
    private TypeEnum type = TypeEnum.SPEED;
    private double value = 0D;

    public CarStatus(Car car, TypeEnum type) {
        this.setTimestamp(car.getLastUpdateTimestamp());
        this.setCarIndex(car.getCarIndex());
        this.setType(type);
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
