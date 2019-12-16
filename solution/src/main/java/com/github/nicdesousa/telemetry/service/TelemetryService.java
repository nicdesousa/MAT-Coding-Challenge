package com.github.nicdesousa.telemetry.service;

import com.github.nicdesousa.mat.domain.Car;
import com.github.nicdesousa.telemetry.domain.CarCoordinate;
import com.github.nicdesousa.telemetry.domain.CarStatus;
import com.github.nicdesousa.telemetry.domain.Event;
import com.github.nicdesousa.telemetry.util.Haversine;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@Slf4j
@ApplicationScoped
public class TelemetryService {

    @Inject
    public CarStatusService carStatusService;
    @Inject
    public EventsService eventsService;
    // identity key lookup map of Car's for which CarCoordinate messages have been received
    private final HashMap<Integer, Car> cars = new HashMap<>();
    // sorted list of Car positions by total distance travelled in descending order
    private final ArrayList<Car> sortedCarPositions = new ArrayList<>();

    /**
     * Process CarCoordinate telemetry messages and publish aggregated and enriched data for each Car's position, speed, and overtake events.
     *
     * @param carCoordinate message received from the "carCoordinates" topic
     */
    public void processCarCoordinate(final CarCoordinate carCoordinate) {
        if (!cars.containsKey(carCoordinate.getCarIndex())) {
            // create a new Car from a carCoordinate
            final Car car = new Car(carCoordinate);
            cars.put(car.getCarIndex(), car);
            car.setPosition(cars.size());
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
            // calculate the distance between the Car's current Location and the carCoordinate Location
            final double distance = Haversine.distance(car.getCurLocation().getLatitude(), car.getCurLocation().getLongitude(),
                    carCoordinate.getLocation().getLatitude(), carCoordinate.getLocation().getLongitude());

            // calculate the Car's speed
            final double time = (double) (carCoordinate.getTimestamp() - car.getLastUpdateTimestamp());
            final double metersPerMilli = distance / time;
            final double metersPerSecond = metersPerMilli * 1000D;
            final double kilometersPerHour = metersPerSecond * 3600D;
            final double milesPerHour = kilometersPerHour / 1.609344D;

            // update Car
            car.setCurLocation(carCoordinate.getLocation());
            car.setTotalDistance(car.getTotalDistance() + distance);
            car.setLastUpdateTimestamp(carCoordinate.getTimestamp());
            car.setCurSpeedKPH(kilometersPerHour);
            car.setCurSpeedMPH(milesPerHour);

            // publish a CarStatus speed message
            carStatusService.publish(new CarStatus(car, CarStatus.TypeEnum.SPEED));
        } catch (Haversine.InputValidationException e) {
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
        Collections.sort(sortedCarPositions, (o1, o2) -> o1.getTotalDistance() > o2.getTotalDistance() ? -1 : (o1.getTotalDistance() < o2.getTotalDistance() ? 1 : 0));

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


