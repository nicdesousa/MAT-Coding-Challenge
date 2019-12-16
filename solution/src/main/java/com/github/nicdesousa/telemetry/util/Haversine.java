package com.github.nicdesousa.telemetry.util;

import org.apache.commons.validator.routines.DoubleValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Haversine {

    private Haversine() {
        throw new IllegalStateException("This is a static utility class");
    }

    protected static final DoubleValidator doubleValidator = DoubleValidator.getInstance();
    // the volumetric mean radius of the Earth is 6371 kilometers
    public static final double EARTH_RADIUS_IN_KM = 6371D;
    public static final String ERROR_LATITUDE = "Latitude is not within -90 and 90";
    public static final String ERROR_LONGITUDE = "Longitude is not within -180 and 180";
    // rounding constants for lat/long
    public static final int ROUND_11_POINT_1_KILOMETERS = 1;
    public static final int ROUND_1_POINT_1_KILOMETERS = 2;
    public static final int ROUND_110_METERS = 3;
    public static final int ROUND_11_METERS = 4;
    public static final int ROUND_1_POINT_1_METERS = 5;
    public static final int ROUND_11_CENTIMETERS = 6;
    public static final int ROUND_11_MILLIMETERS = 7;
    public static final int ROUND_1_POINT_1_MILLIMETERS = 8;
    public static final int ROUND_110_MICRONS = 9;

    /**
     * The haversine formula determines the great-circle distance between two points on a sphere given their longitudes and latitudes.
     *
     * @param startLat  Starting latitude (within -90D and +90D)
     * @param startLong Starting longitude (within -180D and +180D)
     * @param endLat    Ending latitude (within -90D and +90D)
     * @param endLong   Ending longitude (within -180D and +180D)
     * @return Distance in kilometers
     * @throws IllegalArgumentException for invalid parameter values
     * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
     * @see <a href="https://imgs.xkcd.com/comics/coordinate_precision_2x.png">Coordinate digits precision</a>
     */
    public static double distance(final double startLat, final double startLong,
                                  final double endLat, final double endLong) throws Haversine.InputValidationException {
        // throw a checked exception for invalid input to enable error handling by the caller
        if (!Haversine.doubleValidator.isInRange(startLat, -90D, 90D))
            throw new Haversine.InputValidationException(Haversine.ERROR_LATITUDE);
        if (!Haversine.doubleValidator.isInRange(endLat, -90D, 90D))
            throw new Haversine.InputValidationException(Haversine.ERROR_LATITUDE);
        if (!Haversine.doubleValidator.isInRange(startLong, -180D, 180D))
            throw new Haversine.InputValidationException(ERROR_LONGITUDE);
        if (!Haversine.doubleValidator.isInRange(endLong, -180D, 180D))
            throw new Haversine.InputValidationException(ERROR_LONGITUDE);

        final double dLat = Math.toRadians((endLat - startLat));
        final double dLong = Math.toRadians((endLong - startLong));
        final double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) * Math.pow(Math.sin(dLong / 2), 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Haversine.EARTH_RADIUS_IN_KM * c;
    }

    /**
     * Rounds a distance in kilometers to the specified scale
     *
     * @param distance in kilometers
     * @param scale    {@see Haversine}.ROUND_*
     * @return distance rounded to scale
     */
    public static double roundDistance(final double distance, final int scale) {
        return BigDecimal.valueOf(distance).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static final class InputValidationException extends Exception {
        public InputValidationException(String s) {
            super(s);
        }
    }
}