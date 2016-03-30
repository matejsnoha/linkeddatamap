package info.snoha.matej.linkeddatamap;

/**
 * Geographic position in latitude / longitude format.
 */
public class Position {

    private Double latitude;
    private Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Position(String latitude, String longitude) {
        this.latitude = Double.valueOf(latitude);
        this.longitude = Double.valueOf(longitude);
    }

    public Position() {
        this.latitude = Double.NaN;
        this.longitude = Double.NaN;
    }

    public boolean isUndefined() {
        return latitude.isNaN() || longitude.isNaN();
    }

    @Override
    public String toString() {
        return getLatitude() + " " + getLongitude();
    }

    /**
     * Calculates approximate geodetic distance to another Position using Haversine formula.
     * @return distance in meters with approx. 0.5% precision
     */
    public Double distanceTo(Position otherPosition) {

        if (isUndefined() || otherPosition == null || otherPosition.isUndefined()) {
            return Double.NaN;
        }

        double lat1 = Math.toRadians(getLatitude());
        double lon1 = Math.toRadians(getLongitude());
        double lat2 = Math.toRadians(otherPosition.getLatitude());
        double lon2 = Math.toRadians(otherPosition.getLongitude());

        final double EARTH_RADIUS = 6371009; // mean radius in m

        double distLong = lon2 - lon1;
        double distLat = lat2 - lat1;

        double a = Math.pow((Math.sin(distLat / 2)), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(distLong / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(a)); // Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}