package com.github.nicdesousa.telemetry.service;

import com.github.nicdesousa.telemetry.domain.Car;
import com.github.nicdesousa.telemetry.domain.CarCoordinate;
import com.github.nicdesousa.telemetry.domain.CarStatus;
import com.github.nicdesousa.telemetry.domain.Event;
import com.github.nicdesousa.telemetry.util.Haversine;
import com.github.nicdesousa.telemetry.util.InputValidationException;
import com.github.nicdesousa.telemetry.util.Speed;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ApplicationScoped
public class TelemetryService {

    @Inject
    public CarStatusService carStatusService;
    @Inject
    public EventsService eventsService;
    // identity key lookup map of Car's for which CarCoordinate messages have been received
    private final Map<Integer, Car> cars = new ConcurrentHashMap<>();
    // sorted list of Car positions by total distance travelled in descending order
    private final List<Car> sortedCarPositions = new ArrayList<>();

    /**
     * Process CarCoordinate telemetry messages and publish aggregated and enriched data for each Car's position, <br/>
     * speed, and overtake events.
     *
     * @param carCoordinate message received from the "carCoordinates" topic
     */
    public void processCarCoordinate(final CarCoordinate carCoordinate) {
        if (!cars.containsKey(carCoordinate.getCarIndex())) {
            // create a new Car from a carCoordinate
            final Car car = Car.from(carCoordinate);
            cars.put(car.getCarIndex(), car);
            car.setPosition(cars.size() + 1);
            sortedCarPositions.add(car);
            return;
        }
        // process a CarCoordinate update for an existing Car
        updateCarTotalDistanceAndSpeed(carCoordinate);
        updateCarPositionsAndOvertakes();
    }

    /**
     * Update the referenced Car's total distance travelled and publish its speed
     *
     * @param carCoordinate message received from the "carCoordinates" topic
     */
    private void updateCarTotalDistanceAndSpeed(final CarCoordinate carCoordinate) {
        try {
            final Car car = cars.get(carCoordinate.getCarIndex());

            if (car.getLastUpdateTimestamp() >= carCoordinate.getTimestamp()) {
                // only process messages that are newer than the last update time
                return;
            }

            // calculate the distance (in kilometers) between the Car's current Location and the carCoordinate Location
            final double distance = Haversine.distance(car.getCurLocation(), carCoordinate.getLocation());
            // calculate the speed (in MPH)
            final double speed = Speed.speedInMPH(distance, carCoordinate.getTimestamp() - car.getLastUpdateTimestamp());

            // update Car
            car.setCurLocation(carCoordinate.getLocation());
            car.addDistance(distance);
            car.setLastUpdateTimestamp(carCoordinate.getTimestamp());
            car.setCurSpeedMPH(speed);

            // publish a CarStatus speed message
            carStatusService.publish(new CarStatus(car, CarStatus.TypeEnum.SPEED));
        } catch (final InputValidationException e) {
            log.error(carCoordinate.toString(), e);
        }
    }

    /**
     * Sort, update and publish Car positions based on the total distance travelled and then publish events for any overtakes
     */
    private void updateCarPositionsAndOvertakes() {
        // store the current sortedCarPositions index
        final int[] prevPositions = new int[sortedCarPositions.size()];
        for (int i = 0; i < prevPositions.length; i++) {
            prevPositions[i] = sortedCarPositions.get(i).getCarIndex();
        }

        // sort the Car positions by total distance in descending order
        Collections.sort(sortedCarPositions, (o1, o2) -> o1.getTotalDistance() > o2.getTotalDistance() ? -1 :
                (o1.getTotalDistance() < o2.getTotalDistance() ? 1 : 0));

        // store the new sortedCarPositions index and publish position updates
        final int[] updatedPositions = new int[sortedCarPositions.size()];
        for (int i = 0; i < updatedPositions.length; i++) {
            updatedPositions[i] = sortedCarPositions.get(i).getCarIndex();
            sortedCarPositions.get(i).setPosition(i + 1);
            // publish the Car's position
            carStatusService.publish(new CarStatus(sortedCarPositions.get(i), CarStatus.TypeEnum.POSITION));
        }

        // compare the previous and updated sortedCarPositions indexes for position changes and overtakes
        for (int i = 0; i < updatedPositions.length; i++) {
            final Car curCar = cars.get(updatedPositions[i]);
            final Car prevCar = cars.get(prevPositions[i]);
            if (curCar.getCarIndex() != prevCar.getCarIndex() && curCar.getPosition() < prevCar.getPosition()) {
                // curCar overtakes prevCar
                eventsService.publish(new Event(curCar.getLastUpdateTimestamp(), "Car " + curCar.getCarIndex()
                        + " races ahead of Car " + prevCar.getCarIndex() + " in a dramatic overtake."));
            }
        }
    }
}


