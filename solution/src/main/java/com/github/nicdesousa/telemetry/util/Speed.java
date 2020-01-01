package com.github.nicdesousa.telemetry.util;

public final class Speed {
    private Speed() {
        throw new IllegalStateException("This is a static utility class");
    }

    public static final String ERROR_TIME = "time must be greater than 0";

    /**
     * Calculates the speed in MPH for a distance in kilometres and time in milliseconds.
     *
     * @param distanceInKilometres
     * @param timeInMilliseconds   must be greater than 0
     * @return speed in MPH
     * @throws InputValidationException when timeInMillliseconds <= 0
     */
    public static double speedInMPH(final double distanceInKilometres, final long timeInMilliseconds)
            throws InputValidationException {
        if (timeInMilliseconds <= 0L) {
            throw new InputValidationException(ERROR_TIME);
        }

        final double metresPerMilli = Math.abs(distanceInKilometres) / timeInMilliseconds;
        final double metresPerSecond = metresPerMilli * 1000D;
        final double kilometresPerHour = metresPerSecond * 3600D;
        return kilometresPerHour / 1.609344D;
    }

    /**
     * Converts miles per hour to kilometres per hour
     *
     * @param milesPerHour
     * @return kilometres per hour
     */
    public static final double mphToKph(double milesPerHour) {
        return milesPerHour * 1.609344D;
    }

}
