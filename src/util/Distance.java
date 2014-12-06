package util;

import java.io.IOException;

import org.apache.commons.math3.util.FastMath;

import data_structures.graph.Graph;
import data_structures.graph.GraphFactory;
import data_structures.graph.InvalidGraphFormatException;



public final class Distance
{
	static final double EQUATOR_LON_LENGTH = 111.320;
	static final double EARTH_RADIUS = 6371;
	
	
	public static double haversine(double lat1, double lon1, double lat2, double lon2)
	{
		double dLat = FastMath.toRadians(lat2 - lat1);
		double dLng = FastMath.toRadians(lon2 - lon1);

		double a = FastMath.sin(dLat / 2) * FastMath.sin(dLat / 2) + FastMath.cos(Math.toRadians(lat1))
				* FastMath.cos(FastMath.toRadians(lat2)) * FastMath.sin(dLng / 2) * FastMath.sin(dLng / 2);
		double c = 2 * FastMath.atan2(Math.sqrt(a), FastMath.sqrt(1 - a));

		return EARTH_RADIUS * c;
	}


	// don't use
	public static double approx(double lat1, double lon1, double lat2, double lon2) 
	{
		// http://jonisalonen.com/2014/computing-distance-between-coordinates-can-be-simple-and-fast/
	    double y = lat2 - lat1;
	    double x = (lon2 - lon1) * FastMath.cos(lat1);
	    return EQUATOR_LON_LENGTH * FastMath.sqrt(x * x + y * y);
	}
	

	
	public static void main(String[] args) throws InvalidGraphFormatException, IOException
	{
		System.out.println("Distance Test");
		Graph g = GraphFactory.load("./15000K.bin");
		
		double lat_ref = 48.74670985863194;
		double lon_ref = 9.105284214019775;
		
		double[] lats = g.getLatArray();
		double[] lons = g.getLonArray();

		double r2_c_max = Double.MIN_VALUE;
		double r2_d_max = Double.MIN_VALUE;
		
		System.out.println("Haversine \t Approx");
		
		for (int i = 0; i < lats.length; ++i)
		{
			double r1 = haversine(lat_ref, lon_ref, lats[i], lons[i]);
			double r2 = approx(lat_ref, lon_ref, lats[i], lons[i]);
			
			double r2_change = Math.abs(r2 - r1) / r1;
			double r2_diff   = Math.abs(r2 - r1);
			
			r2_c_max = FastMath.max(r2_c_max, r2_change);
			r2_d_max = FastMath.max(r2_d_max, r2_diff);
			
			//System.out.println(lats[i] + "  " + lons[i]);
			System.out.println(String.format("%.3f \t %.3f (%.3f)  max change = %.3f  max = %.3f", r1, r2_change, r2, r2_c_max, r2_d_max));
		}
	}
}
