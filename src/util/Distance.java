package util;



public final class Distance
{
	static final double EQUATOR_LON_LENGTH = 111.320;
	static final double EARTH_RADIUS = 6371;
	
	
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
	
	
	public static double haversine(double lat1, double lon1, double lat2, double lon2)
	{
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS * c;
	}


	/*
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
		
		double r2_c_max = -Double.MAX_VALUE;
		double r2_d_max = -Double.MAX_VALUE;


		long t1 = 0, acc1 = 0, t2 = 0, acc2 = 0;
		double avg1 = 0.0, avg2 = 0.0;
		final int num_iterations = 100;
		
		System.out.println("\nHaversine FastMath vs Math");
		System.out.println(num_iterations + " Iterations, results in seconds\n");
		
		for (int iteration = 0; iteration < num_iterations; ++iteration)
		{
			for (int i = 0; i < lats.length; ++i)
			{
				t1 = System.nanoTime();
				double r1 = haversine_fm(lat_ref, lon_ref, lats[i], lons[i]);
				acc1 += System.nanoTime() - t1;
				
				t2 = System.nanoTime();
				double r2 = haversine(lat_ref, lon_ref, lats[i], lons[i]);
				acc2 += System.nanoTime() - t2;
				
				double r2_change = Math.abs(r2 - r1) / r1;
				double r2_diff   = Math.abs(r2 - r1);
				
				r2_c_max = FastMath.max(r2_c_max, r2_change);
				r2_d_max = FastMath.max(r2_d_max, r2_diff);
			}
			
			System.out.println(String.format("org.apache.commons.math3.util.FastMath: %.6f \t java.lang.Math: %.6f", acc1 / 1000000000.0, acc2 / 1000000000.0));
			avg1 += acc1;
			avg2 += acc2;
			acc1 = 0;
			acc2 = 0;
		}
		avg1 /= num_iterations;
		avg2 /= num_iterations;
		System.out.println("\nAverage:");
		System.out.println(String.format("org.apache.commons.math3.util.FastMath: %.6f \t java.lang.Math: %.6f", avg1 / 1000000000.0, avg2 / 1000000000.0));
	}
	
	
	public static void main2(String[] args) throws InvalidGraphFormatException, IOException
	{
		System.out.println("Distance Test");
		Graph g = GraphFactory.load("./15000K.bin");
		
		double lat_ref = 48.74670985863194;
		double lon_ref = 9.105284214019775;
		
		double[] lats = g.getLatArray();
		double[] lons = g.getLonArray();

		double r2_c_max = -Double.MAX_VALUE;
		double r2_d_max = -Double.MAX_VALUE;
		
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
    */
}
