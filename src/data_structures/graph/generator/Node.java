package data_structures.graph.generator;

import java.util.Arrays;

import util.StringUtil;



/**
 * See {@link #Node(double, double)}.
 */
public class Node
{
    private long id;
    private double lat;
    private double lon;
    
    private long[] dests = new long[0];
    private int[] highway_types = new int[0];
    
    
    /**
     * See {@link #Node(double, double)}.
     */
    public Node() {}
    
    
    /**
     * Provides an encapsulation for information extracted during
     * graph generation.
     * Data is supposed to be added incrementally, prioritizing
     * memory consumption. 
     */
    public Node(double lat, double lon)
    {
        this.lat = lat;
        this.lon = lon;
    }
    
    
    public void addEdge(long id, int highwayType)
    {
        // that's expensive but ensures not a single byte will be unnecessarily allocated
        dests = Arrays.copyOf(dests, dests.length + 1);
        highway_types = Arrays.copyOf(highway_types, highway_types.length + 1);
        
        dests[dests.length - 1] = id;
        highway_types[highway_types.length - 1] = highwayType;
    }
    
    
    public String toString()
    {
        String s = "";
        s += "ID: " + id + "\n";
        s += "   lat    : " + lat + "\n";
        s += "   lon    : " + lon + "\n";
        s += "   dests  : " + StringUtil.arrayToString(dests) + "\n";
        s += "   h_types: " + StringUtil.arrayToString(highway_types);
        return s;
    }
    
    
    public void setLoc(double lat, double lon)
    {
        this.lat = lat;
        this.lon = lon;
    }
    
        
    public void setID(long i)      { this.id = i; }
    public long getID()            { return id; }
    public double getLat()         { return lat; }
    public double getLon()         { return lon; }
    public long[] getDests()       { return dests; }
    public int[] getHighwayTypes() { return highway_types; }

 }
