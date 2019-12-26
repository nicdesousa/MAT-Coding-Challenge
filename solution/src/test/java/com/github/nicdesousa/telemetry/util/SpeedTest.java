package com.github.nicdesousa.telemetry.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpeedTest {

    /**
     * Test if the {@link Speed#speedInMPH} method calculates the distance that sound travels correctly
     */
    @Test
    void testSpeedOfSound() {
        final double speedOfSoundInMPH = 767D;
        final double distanceOfSoundInKM = speedOfSoundInMPH * 1.609344D;
        final long oneHourInMilliseconds = 1000 * 60 * 60;

        try {
            assertEquals(speedOfSoundInMPH, Speed.speedInMPH(distanceOfSoundInKM, oneHourInMilliseconds));
        } catch (final InputValidationException e) {
            fail();
        }
    }
    
    /**
     * Test if the {@link Speed#speedInMPH} method works with a negative distance
     */
    @Test
    void testNegativeDistance() {
        final double speedOfSoundInMPH = 767D;
        final double distanceOfSoundInKM = 0D - (speedOfSoundInMPH * 1.609344D);
        final long oneHourInMilliseconds = 1000 * 60 * 60;

        try {
            assertEquals(speedOfSoundInMPH, Speed.speedInMPH(distanceOfSoundInKM, oneHourInMilliseconds));
        } catch (final InputValidationException e) {
            fail();
        }
    }

    /**
     * Test if the {@link Speed#speedInMPH} method validates the time parameter correctly
     */
    @Test
    void testInvalidTime() {
        final Throwable e1 = assertThrows(InputValidationException.class, () -> Speed.speedInMPH(1D, -0L));
        assertEquals(Speed.ERROR_TIME, e1.getMessage());

        final Throwable e2 = assertThrows(InputValidationException.class, () -> Speed.speedInMPH(1D, 0L));
        assertEquals(Speed.ERROR_TIME, e2.getMessage());

        final Throwable e3 = assertThrows(InputValidationException.class, () -> Speed.speedInMPH(1D, -1L));
        assertEquals(Speed.ERROR_TIME, e3.getMessage());
    }

}