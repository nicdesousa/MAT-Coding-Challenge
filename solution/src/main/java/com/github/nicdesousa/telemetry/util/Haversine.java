package com.github.nicdesousa.telemetry.util;

import com.github.nicdesousa.telemetry.domain.Location;
import org.apache.commons.validator.routines.DoubleValidator;

public final class Haversine {

    private Haversine() {
        throw new IllegalStateException("This is a static utility class");
    }

    private static final DoubleValidator DOUBLE_VALIDATOR = DoubleValidator.getInstance();
    // the volumetric mean radius of the Earth is 6371 kilometers
    public static final double EARTH_RADIUS_IN_KM = 6371D;
    public static final String ERROR_LATITUDE = "Latitude is not within -90 and 90";
    public static final String ERROR_LONGITUDE = "Longitude is not within -180 and 180";
    public static final double MIN_LATITUDE = -90D;
    public static final double MAX_LATITUDE = 90D;
    public static final double MIN_LONGITUDE = -180D;
    public static final double MAX_LONGITUDE = 180D;

    /**
     * Calculates the distance between two latitude and longitude coordinates.
     * <br/>
     * This method uses the haversine formula to determine the great-circle distance between two points on a sphere <br/>
     * (planet Earth in this case) given their longitudes and latitudes.
     *
     * @param startLat  Starting latitude (within -90D and +90D)
     * @param startLong Starting longitude (within -180D and +180D)
     * @param endLat    Ending latitude (within -90D and +90D)
     * @param endLong   Ending longitude (within -180D and +180D)
     * @return Distance in kilometers
     * @throws InputValidationException for invalid parameter values
     * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
     * @see <a href="https://imgs.xkcd.com/comics/coordinate_precision_2x.png">Coordinate digits precision</a>
     */
    public static double distance(final double startLat, final double startLong,
                                  final double endLat, final double endLong) throws InputValidationException {
        // throw a checked exception for invalid input to enable error handling by the caller
        validateCoordinate(startLat, startLong);
        validateCoordinate(endLat, endLong);

        final double dLat = Math.toRadians((endLat - startLat));
        final double dLong = Math.toRadians((endLong - startLong));
        final double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(Math.toRadians(startLat)) *
                Math.cos(Math.toRadians(endLat)) * Math.pow(Math.sin(dLong / 2), 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Haversine.EARTH_RADIUS_IN_KM * c;
    }

    /**
     * Calculates the distance between two {@see Location}'s.
     *
     * @param startLocation {@see Location}
     * @param endLocation   {@see Location}
     * @return Distance in kilometers
     * @throws InputValidationException for invalid parameter values
     */
    public static double distance(final Location startLocation, final Location endLocation)
            throws InputValidationException {
        return distance(startLocation.getLatitude(), startLocation.getLongitude(), endLocation.getLatitude(),
                endLocation.getLongitude());
    }

    /**
     * Validates if latitude is within -90D and +90D and longitude is within -180D and +180D
     *
     * @param latitude
     * @param longitude
     * @throws InputValidationException for invalid parameter values
     */
    public static void validateCoordinate(final double latitude, final double longitude)
            throws InputValidationException {
        if (!Haversine.DOUBLE_VALIDATOR.isInRange(latitude, MIN_LATITUDE, MAX_LATITUDE)) {
            throw new InputValidationException(Haversine.ERROR_LATITUDE);
        }
        if (!Haversine.DOUBLE_VALIDATOR.isInRange(longitude, MIN_LONGITUDE, MAX_LONGITUDE)) {
            throw new InputValidationException(ERROR_LONGITUDE);
        }
    }

}