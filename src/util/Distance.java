package util;



public final class Distance
{
    // everything in meters
    static final long EARTH_CIRCUMFERENCE_EQUATORIAL = 40075017L;
    static final long EARTH_CIRCUMFERENCE_MERIDIONAL = 40007860L;
	static final double EARTH_RADIUS = 6371 * 1000;
	
	
	/* This is actually slower on my machine
	public static double haversine_fm(double lat1, double lon1, double lat2, double lon2)
	{
		double dLat = FastMath.toRadians(lat2 - lat1);
		double dLng = FastMath.toRadians(lon2 - lon1);

		double a = FastMath.sin(dLat / 2) * FastMath.sin(dLat / 2) + FastMath.cos(Math.toRadians(lat1))
				* FastMath.cos(FastMath.toRadians(lat2)) * FastMath.sin(dLng / 2) * FastMath.sin(dLng / 2);
		double c = 2 * FastMath.atan2(Math.sqrt(a), FastMath.sqrt(1 - a));

		return EARTH_RADIUS * c;
	}
	*/
	
	
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
