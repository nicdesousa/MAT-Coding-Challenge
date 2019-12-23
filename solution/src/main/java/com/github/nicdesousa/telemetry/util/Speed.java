package com.github.nicdesousa.telemetry.util;

public final class Speed {
    private Speed() {
        throw new IllegalStateException("This is a static utility class");
    }

    public static final String ERROR_TIME = "time must be greater than 0";

    /**
     * Calculates the speed in MPH for a distance in kilometers and time in milliseconds.
     *
     * @param distanceInKilometers
     * @param timeInMillliseconds  must be greater than 0
     * @return speed in MPH
     * @throws InputValidationException when timeInMillliseconds <= 0
     */
    public static double speedInMPH(final double distanceInKilometers, final long timeInMillliseconds)
            throws InputValidationException {
        if (timeInMillliseconds <= 0L) {
            throw new InputValidationException(ERROR_TIME);
        }

        final double metersPerMilli = Math.abs(distanceInKilometers) / timeInMillliseconds;
        final double metersPerSecond = metersPerMilli * 1000D;
        final double kilometersPerHour = metersPerSecond * 3600D;
        return kilometersPerHour / 1.609344D;
    }

    /**
     * Converts miles per hour to kilometers per hour
     *
     * @param milesPerHour
     * @return kilometers per hour
     */
    public static final double mphToKph(double milesPerHour) {
        return milesPerHour * 1.609344D;
    }

}
