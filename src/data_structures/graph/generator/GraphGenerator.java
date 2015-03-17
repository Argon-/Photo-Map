package data_structures.graph.generator;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import util.Distance;



public class GraphGenerator
{
    public static final String W_TAG_START = "    \"";
    public static final String W_TAG_SEP   = "\"    \"";
    public static final String N_TAG_SEP   = "\"  \"";
    public static final String DATA_SEP    = "   ";
    public static final String ID_SEP      = " ";
    public static final String KV_SEP      = "\" \"";

    
    private final String node_file_loc;
    private final String way_file_loc;
    private final TLongObjectHashMap<Node> nodes = new TLongObjectHashMap<Node>();

    private long edge_num = 0;
    
    
    public GraphGenerator(String node_file, String way_file)
    {
        this.node_file_loc = node_file;
        this.way_file_loc = way_file;
    }
    
    
    public void generate()
    {
        try 
        {
            final BufferedReader node_file = new BufferedReader(new InputStreamReader(new FileInputStream(node_file_loc)), 8192);
            final BufferedReader way_file = new BufferedReader(new InputStreamReader(new FileInputStream(way_file_loc)), 8192);
        
            /* ***************************************************************
             * read ways
             ****************************************************************/
            System.out.println("Reading node IDs from ways");
            String line = "";
            while ((line = way_file.readLine()) != null)
            {
                String[] tmp = line.split(W_TAG_START, 2);
                String[] nodelist = tmp[0].split(DATA_SEP, 2)[1].split(ID_SEP);
                
                int highway = -2;
                boolean oneway = false;
                
                // split tags into key value pairs and parse them
                for (String s : tmp[1].split(W_TAG_SEP)) {
                    String[] pair = s.split(KV_SEP);
                    if (pair.length != 2) {
                        continue;
                    }
                    String k = pair[0].startsWith("\"") ? pair[0].substring(1) : pair[0];
                    String v = pair[1].endsWith("\"") ? pair[1].substring(0, pair[1].length() - 1) : pair[1];
                                        
                    if (k.startsWith("highway")) {
                        highway = highwayType(v, false);
                    }
                    else if (k.startsWith("oneway")) {
                        if (v.equals("yes") || v.equals("true") || v.equals("1"))
                            oneway = true;
                    }
                }
                
                // ignore this way
                if (highway < 0)
                    continue;
                
                // add edges
                long current;
                long next = Long.parseLong(nodelist[0]);
                for (int i = 1; i < nodelist.length; ++i) {
                    ++edge_num;
                    current = next;
                    next = Long.parseLong(nodelist[i]);
                    
                    // "forward" edges
                    if (nodes.containsKey(current)) {
                        nodes.get(current).addEdge(next, highway);
                    }
                    else {
                        Node n = new Node();
                        n.addEdge(next, highway);
                        nodes.put(current, n);
                    }
                    
                    // "backward" edges
                    if (!oneway)
                    {
                        ++edge_num;
                        if (nodes.containsKey(next)) {
                            nodes.get(next).addEdge(current, highway);
                        }
                        else {
                            Node n = new Node();
                            n.addEdge(current, highway);
                            nodes.put(next, n);
                        }
                    }
                    
                    // create entry for last node
                    if (i == (nodelist.length-1)) {
                        if (!nodes.containsKey(next)) {
                            nodes.put(next, new Node());
                        }
                    }
                }                
            }
            
            
            /* ***************************************************************
             * add node info
             ****************************************************************/
            System.out.println("Reading data for relevant nodes");
            while ((line = node_file.readLine()) != null)
            {
                String[] tmp = line.split(ID_SEP, 5);
                if (!nodes.containsKey(Long.parseLong(tmp[1])))
                    continue;
                
                long id = Long.parseLong(tmp[1]);
                double lat = Double.parseDouble(tmp[2]);
                double lon = Double.parseDouble(tmp[3]);
                int tourism = -2;
                String name = null;

                
                if (tmp.length > 4) 
                {
                    // split tags into key value pairs and parse them
                    for (String s : tmp[4].trim().split(N_TAG_SEP)) {
                        String[] pair = s.split(KV_SEP);
                        if (pair.length != 2) {
                            continue;
                        }
                        String k = pair[0].startsWith("\"") ? pair[0].substring(1) : pair[0];
                        String v = pair[1].endsWith("\"") ? pair[1].substring(0, pair[1].length() - 1) : pair[1];
                        
                        if (k.startsWith("tourism")) {
                            tourism = tourismType(v);
                        }
                        else if (k.startsWith("name")) {
                            name = v;
                        }
                    }
                }

                Node n = nodes.get(id);
                n.lat = lat;
                n.lon = lon;
                n.name = name;
                n.tourism_type = tourism;
                //if (n.lat < 0.1)
                //    System.out.println(lat);
                
                /*
                if (tourism >= 0) {
                    System.out.println("----------------------------------------");
                    //System.out.println(line);
                    //System.out.println("   => " + arrayToString(tmp[1].split(TAG_SEP)));
                    //System.out.println("   => " + highway);
                    System.out.println(nodes.get(id));
                    if (++c > 2)
                        break;
                }*/
            }
            
            node_file.close();
            way_file.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void writeOut(String out_file_loc)
    {
        System.out.println("Creating output file");
        
        try {
            BufferedWriter out_file = new BufferedWriter(new FileWriter(out_file_loc));
            
            // write header
            out_file.write(Long.toString(nodes.size()));
            out_file.newLine();
            out_file.write(Long.toString(edge_num));
            out_file.newLine();
            
            
            // write nodes
            TLongObjectIterator<Node> it = nodes.iterator();
            long id = 0;
            while (it.hasNext())
            {
                it.advance();
                Node n = it.value();
                n.id = id++;
                
                out_file.write(n.id + " " + n.lat + " " + n.lon);
                if (n.tourism_type >= 0) {
                    out_file.write(" " + n.tourism_type);
                    if (n.name != null)
                        out_file.write(" " + n.name);
                }
                out_file.newLine();
            }
            
            
            // write edges
            it = nodes.iterator();
            while (it.hasNext())
            {
                it.advance();
                Node n = it.value();
                
                for (int e = 0; e < n.dests.length; ++e) {
                    Node dest = nodes.get(n.dests[e]);
                    if (dest == null) {
                        System.out.println("No mapping for " + n.dests[e]);
                    }
                    long dist = Math.round(Distance.haversine(n.lat, n.lon, dest.lat, dest.lon));
                    out_file.write(n.id + " " + dest.id + " " + dist + " " + n.highway_types[e]);
                    out_file.newLine();
                }
            }
            
            
            out_file.close();
            System.out.println("Finished: " + out_file_loc);
        }
        catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    
    public int highwayType(String s, boolean extended_types)
    {
        // regular roads
        if (s.equals("motorway"))
            return 1;
        else if (s.equals("trunk"))
            return 6;
        else if (s.equals("primary"))
            return 2;
        else if (s.equals("secondary"))
            return 3;
        else if (s.equals("tertiary"))
            return 4;
        else if (s.equals("unclassified"))
            return 12;
        else if (s.equals("residential"))
            return 8;
        else if (s.equals("service"))
            return 11;
        
        // links to regular roads
        else if (s.equals("motorway_link"))
            return 1;
        else if (s.equals("trunk_link"))
            return 6;
        else if (s.equals("primary_link"))
            return 2;
        else if (s.equals("secondary_link"))
            return 3;
        else if (s.equals("tertiary_link"))
            return 4;
        
        // special road types
        else if (s.equals("living_street"))
            return 9;
        else if (s.equals("pedestrian"))
            return extended_types ? 20 : -20;
        else if (s.equals("track"))
            return extended_types ? 21 : -21;
        //else if (s.equals("bus_guideway"))
        //    return extended_types ? 22 : -22;
        //else if (s.equals("raceway"))
        //    return extended_types ? 23 : -23;
        else if (s.equals("road"))
            return 7;
        
        // paths
        else if (s.equals("footway"))
            return extended_types ? 24 : -24;
        else if (s.equals("cycleway"))
            return extended_types ? 25 : -25;
        else if (s.equals("bridleway"))
            return extended_types ? 26 : -26;
        else if (s.equals("steps"))
            return extended_types ? 27 : -27;
        else if (s.equals("path"))
            return extended_types ? 28 : -28;
        
        // other
        // includes highway=no
        else
            return -1;
    }
    
    
    public int tourismType(String s)
    {
        if (s.equals("alpine_hut"))
            return 1;
        else if (s.equals("apartment"))
            return 2;
        else if (s.equals("attraction"))
            return 3;
        else if (s.equals("artwork"))
            return 4;
        else if (s.equals("camp_site"))
            return 5;
        else if (s.equals("caravan_site"))
            return 6;
        else if (s.equals("chalet"))
            return 7;
        else if (s.equals("gallery"))
            return 8;
        else if (s.equals("guest_house"))
            return 9;
        else if (s.equals("hostel"))
            return 10;
        else if (s.equals("hotel"))
            return 11;
        else if (s.equals("information"))
            return 12;
        else if (s.equals("motel"))
            return 13;
        else if (s.equals("museum"))
            return 14;
        else if (s.equals("picnic_site"))
            return 15;
        else if (s.equals("theme_park"))
            return 16;
        else if (s.equals("viewpoint"))
            return 17;
        else if (s.equals("wilderness_hut"))
            return 18;
        else if (s.equals("zoo"))
            return 19;
        // other
        else if (s.equals("yes"))
            return 0;
        else if (s.equals("user defined"))
            return 0;
        else
            return -1;
    }
    
    
    public static String arrayToString(String[] n)
    {
        String s = "";
        for (int i = 0; i < n.length; ++i) {
            s += n[i];
            
            if (i != n.length - 1)
                s += ", ";
            else
                s += " ";
        }
        return s;
    }

}