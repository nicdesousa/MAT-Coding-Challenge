package com.github.nicdesousa.telemetry.service;

import com.github.nicdesousa.telemetry.domain.*;
import com.github.nicdesousa.telemetry.util.Haversine;
import com.github.nicdesousa.telemetry.util.InputValidationException;
import com.github.nicdesousa.telemetry.util.Speed;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@ApplicationScoped
public class TelemetryService {

    public static final String LAP_TIME_FORMAT = "Car %d completed lap %02d in %d:%02d.%03d with an average speed of %03.02f MPH";
    public static final String FASTEST_LAP_TIME_FORMAT = "Car %d has set the new fastest lap time at %d:%02d.%03d with an average speed of %03.02f MPH";
    public static final String EVENT_OVERTAKE_FORMAT = "Car %d races ahead of Car %d in a dramatic overtake, faster than... %s!";
    // add some comic relief for the fans
    private final Random rand = new Random();
    private final String[] FASTER_THAN = {"the Stig can buy the newest McLaren",
            "Clarkson becomes speechless in a McLaren",
            "the real 007 will swap his car for a McLaren",
            "a speeding ticket is voided because it's for a McLaren",
            "Maverick's jet loses to the McLaren P1",
            "hypercar manufacturers that claim the race isn't fair when they see a McLaren",
            "the laws of physics are being challenged by McLaren", "a Porsche 918 loses to the McLaren P1",
            "a Ferrari LaFerrari loses to the McLaren P1", "superbikes lose to a McLaren P1"
    };
    @Inject
    public CarStatusService carStatusService;
    @Inject
    public EventsService eventsService;
    // please see: geojson/README.md for an explanation of the circuit length
    @ConfigProperty(name = "telemetryService.circuitLengthInKM", defaultValue = "5.119771376289225")
    public Double circuitLengthInKM;

    // identity key lookup map of Car's for which CarCoordinate messages have been received
    private final Map<Integer, Car> cars = new ConcurrentHashMap<>();
    // sorted list of Car positions by total distance travelled in descending order
    private final List<Car> sortedCarPositions = new ArrayList<>();
    // fastest lap Car
    private int fastestLapCar = -1;
    // fastest lap time in milliseconds
    private long fastestLapTimeInMs = Long.MAX_VALUE;

    /**
     * Process CarCoordinate telemetry messages and publish aggregated and enriched data for each Car's position, <br/>
     * speed, and overtake events.
     *
     * @param carCoordinate message received from the "carCoordinates" topic
     */
    public void processCarCoordinate(final CarCoordinate carCoordinate) {
        if (!this.cars.containsKey(carCoordinate.getCarIndex())) {
            // create a new Car from a carCoordinate
            final Car car = Car.from(carCoordinate);
            this.cars.put(car.getCarIndex(), car);
            car.setPosition(this.cars.size() + 1);
            this.sortedCarPositions.add(car);
            return;
        }
        // process a CarCoordinate update for an existing Car
        this.updateCarTotalDistanceAndSpeed(carCoordinate);
        this.updateCarPositionsAndOvertakes();
    }

    /**
     * Update the referenced Car's total distance travelled and publish its speed
     *
     * @param carCoordinate message received from the "carCoordinates" topic
     */
    private void updateCarTotalDistanceAndSpeed(final CarCoordinate carCoordinate) {
        try {
            final Car car = this.cars.get(carCoordinate.getCarIndex());

            if (carCoordinate.getTimestamp() <= car.getLastUpdateTimestamp()) {
                // only process messages that are newer than the car's last update time
                // log an error since this should not happen
                log.error("Received carCoordinate.getTimestamp %d <= car.getLastUpdateTimestamp %d",
                        carCoordinate.getTimestamp(), car.getLastUpdateTimestamp());
                return;
            }

            // calculate the distance (in kilometres) between the Car's current Location and the carCoordinate Location
            final double distance = Haversine.distance(car.getCurLocation(), carCoordinate.getLocation());
            // calculate the speed (in MPH)
            final double speed = Speed.speedInMPH(distance, carCoordinate.getTimestamp() - car.getLastUpdateTimestamp());

            // update Car
            car.setCurLocation(carCoordinate.getLocation());
            car.addDistance(distance);
            car.setLastUpdateTimestamp(carCoordinate.getTimestamp());
            car.setCurSpeedMPH(speed);

            // publish a CarStatus speed message
            this.carStatusService.publish(new CarStatus(car, CarStatus.TypeEnum.SPEED));

            // calculate Car lap(s)
            final double circuitLengthLaps = car.getTotalDistance() / this.circuitLengthInKM;
            final int completedLaps = (int) (circuitLengthLaps);
            if (car.getLaps().size() < completedLaps) {
                // perform lap calculations using offsets, i.e. correct for distance "overshoots"
                final double offsetDistance = car.getTotalDistance() - (completedLaps * this.circuitLengthInKM);
                final double offsetPercentage = offsetDistance / this.circuitLengthInKM;
                final long offsetTime = BigDecimal.valueOf(car.getLastUpdateTimestamp() - car.getLapStartTime())
                        .multiply(BigDecimal.valueOf(offsetPercentage)).longValue();
                final long endTime = car.getLastUpdateTimestamp() - (offsetTime);

                // create a new CarLap for a Car
                final CarLap carLap = new CarLap();
                car.getLaps().add(carLap);
                carLap.setStartTime(car.getLapStartTime());
                carLap.setEndTime(endTime);
                carLap.setDistance(this.circuitLengthInKM);
                final long lapTimeInMs = carLap.getEndTime() - carLap.getStartTime();
                carLap.setAverageSpeed(Speed.speedInMPH(this.circuitLengthInKM, lapTimeInMs));

                // convert lapTimeInMS into lapMinutes:lapSeconds.lapMillis
                final long lapMinutes = lapTimeInMs / TimeUnit.MINUTES.toMillis(1);
                final long lapSeconds = (lapTimeInMs - TimeUnit.MINUTES.toMillis(lapMinutes)) / TimeUnit.SECONDS.toMillis(1);
                final long lapMillis = lapTimeInMs - TimeUnit.MINUTES.toMillis(lapMinutes) - TimeUnit.SECONDS.toMillis(lapSeconds);
                final String lapTimeEvent = String.format(LAP_TIME_FORMAT, car.getCarIndex(),
                        car.getLaps().size(), lapMinutes, lapSeconds, lapMillis, carLap.getAverageSpeed());
                log.debug(lapTimeEvent);
                car.setLapStartTime(carLap.getEndTime());

                // publish an Event message for lap time
                // use car.getLastUpdateTimestamp() to keep emitted events synchronized to source timestamps
                this.eventsService.publish(new Event(car.getLastUpdateTimestamp(), lapTimeEvent));

                if (lapTimeInMs < this.fastestLapTimeInMs) {
                    this.fastestLapTimeInMs = lapTimeInMs;
                    this.fastestLapCar = car.getCarIndex();
                    final String newFastestLapEvent = String.format(FASTEST_LAP_TIME_FORMAT, car.getCarIndex(),
                            lapMinutes, lapSeconds, lapMillis, carLap.getAverageSpeed());
                    log.debug(newFastestLapEvent);
                    // publish an Event message for new fastest lap time
                    // use car.getLastUpdateTimestamp() to keep emitted events synchronized to source timestamps
                    this.eventsService.publish(new Event(car.getLastUpdateTimestamp(), newFastestLapEvent));
                }
            }
        } catch (final InputValidationException e) {
            log.error(carCoordinate.toString(), e);
        }
    }

    /**
     * Sort, update and publish Car positions based on the total distance travelled and then publish events for any overtakes
     */
    private void updateCarPositionsAndOvertakes() {
        // store the current sortedCarPositions index
        final int[] prevPositions = new int[this.sortedCarPositions.size()];
        for (int i = 0; i < prevPositions.length; i++) {
            prevPositions[i] = this.sortedCarPositions.get(i).getCarIndex();
        }

        // sort the Car positions by total distance in descending order
        Collections.sort(this.sortedCarPositions, (o1, o2) -> o1.getTotalDistance() > o2.getTotalDistance() ? -1 :
                (o1.getTotalDistance() < o2.getTotalDistance() ? 1 : 0));

        // store the new sortedCarPositions index and publish position updates
        final int[] updatedPositions = new int[this.sortedCarPositions.size()];
        for (int i = 0; i < updatedPositions.length; i++) {
            updatedPositions[i] = this.sortedCarPositions.get(i).getCarIndex();
            this.sortedCarPositions.get(i).setPosition(i + 1);
            // publish the Car's position
            this.carStatusService.publish(new CarStatus(this.sortedCarPositions.get(i), CarStatus.TypeEnum.POSITION));
        }

        // compare the previous and updated sortedCarPositions indexes for position changes and overtakes
        for (int i = 0; i < updatedPositions.length; i++) {
            final Car curCar = this.cars.get(updatedPositions[i]);
            final Car prevCar = this.cars.get(prevPositions[i]);
            if (curCar.getCarIndex() != prevCar.getCarIndex() && curCar.getPosition() < prevCar.getPosition()) {
                // curCar overtakes prevCar
                this.eventsService.publish(new Event(curCar.getLastUpdateTimestamp(), String.format(EVENT_OVERTAKE_FORMAT
                        , curCar.getCarIndex(), prevCar.getCarIndex(), this.FASTER_THAN[this.rand.nextInt(this.FASTER_THAN.length)])));
            }
        }
    }
}


