package data_structures.graph.generator;

import java.util.Arrays;

import util.StringUtil;



public final class Node
{
    public long id;
    public double lat;
    public double lon;
    public int tourism_type;
    public String name;
    
    public long[] dests = new long[0];
    public int[] highway_types = new int[0];
    
    
    public void addEdge(long id, int highwayType)
    {
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
        s += "   t_type : " + tourism_type + "\n";
        s += "   name   : " + name + "\n";
        s += "   dests  : " + StringUtil.arrayToString(dests) + "\n";
        s += "   h_types: " + StringUtil.arrayToString(highway_types);
        return s;
    }
            
 }
