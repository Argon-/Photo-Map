package util;



public final class Distance
{
    // in meters
    static final double EARTH_RADIUS = 6371 * 1000;
    
    
    /**
     * Translate the given latitude by the given distance.
     * 
     * @param lat latitude
     * @param meters distance
     * @return translated latitude
     */
    public static double translateLat(double lat, int meters)
    {
        return lat + Math.toDegrees(meters / EARTH_RADIUS);
    }
    
    
    /**
     * Translate the given longitude by the given distance.
     * 
     * @param lon longitude
     * @param meters distance
     * @param at_lat latitude at this longitude
     * @return translated longitude
     */
    public static double translateLon(double lon, int meters, double at_lat)
    {
        return lon + Math.toDegrees(meters / EARTH_RADIUS) / Math.cos(Math.toRadians(at_lat));
    }
    
    
    /**
     * Computes distance between (lat1, lon1) and (lat2, lon2) according to
     * the Haversine formula.
     * 
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return distance in meters
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2)
    {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
    
}
