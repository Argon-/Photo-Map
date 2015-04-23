package util;



public final class StopWatch
{
    private long t        = 0L;
    private long lastDiff = -1L;


    /**
     * Take a lap.
     */
    public StopWatch lap()
    {
        long i = System.nanoTime();
        lastDiff = i - t;
        t = i;
        return this;
    }


    /**
     * Return the time between the last two {@link #lap()} calls in 
     * nanosecond resolution.
     */
    public long getLast()
    {
        return lastDiff;
    }


    /**
     * Return the time between the last two {@link #lap()} calls in 
     * (fractional) second resolution.
     */
    public double getLastInSec()
    {
        return lastDiff / 1000000000.0;
    }

    
    /**
     * Return the time between the last two {@link #lap()} calls as formatted
     * string in (fractional) second resolution and {@code i} decimal places.
     */
    public String getLastInSecStr(int i)
    {
        return String.format("%." + Integer.toString(i) + "f", getLastInSec());
    }

    /**
     * Return the time between the last two {@link #lap()} calls as formatted
     * string in (fractional) second resolution and {@code 9} decimal places.
     */
    public String getLastInSecStrLong()
    {
        return getLastInSecStr(9);
    }

    /**
     * Return the time between the last two {@link #lap()} calls as formatted
     * string in (fractional) second resolution and {@code 3} decimal places.
     */
    public String getLastInSecStr()
    {
        return getLastInSecStr(3);
    }
}
