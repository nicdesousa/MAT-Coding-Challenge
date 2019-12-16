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
        } catch (Haversine.InputValidationException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test if the {@link Haversine#distance} method validates the latitude parameters correctly
     */
    @Test
    void testLatitudeRange() {
        Throwable exception = assertThrows(Haversine.InputValidationException.class, () -> Haversine.distance(-91D, 0, 0, 0));
        assertEquals(Haversine.ERROR_LATITUDE, exception.getMessage());
        Throwable exception2 = assertThrows(Haversine.InputValidationException.class, () -> Haversine.distance(0, 0, 91D, 0));
        assertEquals(Haversine.ERROR_LATITUDE, exception2.getMessage());
    }

    /**
     * Test if the {@link Haversine#distance} method validates the longitude parameters correctly
     */
    @Test
    void testLongitudeRange() {
        Throwable exception = assertThrows(Haversine.InputValidationException.class, () -> Haversine.distance(0, -181D, 0, 0));
        assertEquals(Haversine.ERROR_LONGITUDE, exception.getMessage());
        Throwable exception2 = assertThrows(Haversine.InputValidationException.class, () -> Haversine.distance(0, 0, 0, 181D));
        assertEquals(Haversine.ERROR_LONGITUDE, exception2.getMessage());
    }


}
