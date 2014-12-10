package util;



public final class StopWatch {
	static private long t = 0L;
	static private long lastDiff = -1L;
	
	
	static public long lap()
	{
		long i = System.nanoTime();
		lastDiff = i - t;
		t = i;
		return lastDiff;
	}
	
	static public double lapSec()
	{
		return StopWatch.lap() / 1000000000.0;
	}
	
	static public long getLastLap() 
	{
		return lastDiff;
	}
	
	static public double getLastLapSec()
	{
		return lastDiff / 1000000000.0;
	}
	
	static public String lapSecStr()
	{
		return String.format("%.9f", lapSec());
	}
}
