package com.github.nicdesousa.telemetry.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
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
        final double expectedDistance = 0.49280434203770757D; // distance in kilometres
        try {
            assertEquals(expectedDistance, Haversine.distance(startLat, startLong, endLat, endLong)); // start to end
            assertEquals(expectedDistance, Haversine.distance(endLat, endLong, startLat, startLong)); // end to start
        } catch (final InputValidationException e) {
            fail();
        }
    }

    /**
     * Test if the {@link Haversine#distance} method calculates the total distance for an array of coordinates correctly
     */
    @Test
    void testCircuitLengthCalculation() {
        final ObjectMapper mapper = new ObjectMapper();
        final InputStream is = this.getClass().getResourceAsStream("/silverstone.json");
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final GeoJsonObject geoJsonObject = mapper.readValue(is, GeoJsonObject.class);
            final List<LngLatAlt> coordinates = ((LineString) geoJsonObject).getCoordinates();
            assertTrue(coordinates.size() > 2);
            if (!coordinates.get(coordinates.size() - 1).equals(coordinates.get(0))) {
                log.debug("the coordinates[] does not create a polygon, adding the first coordinate as the last coordinate");
                coordinates.add(coordinates.get(0));
            }
            double totalDistance = 0D;
            for (int i = 1; i < coordinates.size(); i++) {
                totalDistance += Haversine.distance(coordinates.get(i - 1).getLatitude(),
                        coordinates.get(i - 1).getLongitude(), coordinates.get(i).getLatitude(),
                        coordinates.get(i).getLongitude());
            }
            log.debug(String.format("Total distance (length) defined by the coordinates[]: %s", totalDistance));
            assertEquals(5.119771376289225D, totalDistance);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
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
