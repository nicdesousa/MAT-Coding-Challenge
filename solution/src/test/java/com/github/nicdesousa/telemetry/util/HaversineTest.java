package com.github.nicdesousa.telemetry.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HaversineTest {

    /**
     * Test if the {@link Haversine#distance} method calculates the distance correctly
     */
    @Test
    void testDistanceCalculation() {
        final double startLat = 52.067695316642116D;
        final double startLong = -1.0241639614105225D;
        final double endLat = 52.071236872409735D;
        final double endLong = -1.019829511642456D;
        final double expectedDistance = 0.49280434203770757D; // distance in kilometers
        try {
            assertEquals(expectedDistance, Haversine.distance(startLat, startLong, endLat, endLong)); // start to end
            assertEquals(expectedDistance, Haversine.distance(endLat, endLong, startLat, startLong)); // end to start
        } catch (final InputValidationException e) {
            fail();
        }
    }

    /**
     * Test if the {@link Haversine#validateCoordinate} method validates the latitude parameter correctly
     */
    @Test
    void testLatitudeRange() {
        assertDoesNotThrow(() -> Haversine.validateCoordinate(-90D, 0D));
        final Throwable e1 = assertThrows(InputValidationException.class, () -> Haversine.validateCoordinate(-91D, 0D));
        assertEquals(Haversine.ERROR_LATITUDE, e1.getMessage());

        assertDoesNotThrow(() -> Haversine.validateCoordinate(90D, 0D));
        final Throwable e2 = assertThrows(InputValidationException.class, () -> Haversine.validateCoordinate(91D, 0D));
        assertEquals(Haversine.ERROR_LATITUDE, e2.getMessage());
    }

    /**
     * Test if the {@link Haversine#validateCoordinate} method validates the longitude parameter correctly
     */
    @Test
    void testLongitudeRange() {
        assertDoesNotThrow(() -> Haversine.validateCoordinate(0D, -180D));
        final Throwable e1 = assertThrows(InputValidationException.class, () -> Haversine.validateCoordinate(0D, -181D));
        assertEquals(Haversine.ERROR_LONGITUDE, e1.getMessage());

        assertDoesNotThrow(() -> Haversine.validateCoordinate(0D, 180D));
        final Throwable e2 = assertThrows(InputValidationException.class, () -> Haversine.validateCoordinate(0D, 181D));
        assertEquals(Haversine.ERROR_LONGITUDE, e2.getMessage());
    }


}
