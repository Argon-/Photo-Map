package data_structures.graph.generator;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

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
    private final LinkedList<TourismNode> tourism_nodes = new LinkedList<TourismNode>();

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
                if (highway < 0) {
                    continue;
                }
                
                // extract edges from current way
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
                
                long id = Long.parseLong(tmp[1]);
                double lat = Double.parseDouble(tmp[2]);
                double lon = Double.parseDouble(tmp[3]);
                
                
                // node was previously picked up as part of a way -> add lat/lon
                Node n = nodes.get(id);
                if (n != null) {
                    n.setLoc(lat, lon);
                }

                
                // checking if this is an interesting tourism node
                int tourism = -2;
                String name = null;

                if (tmp.length > 4) 
                {
                    // split tags into key value pairs and parse them
                    boolean checked = false;
                    for (String s : tmp[4].trim().split(N_TAG_SEP)) {
                        String[] pair = s.split(KV_SEP);
                        if (pair.length != 2) {
                            continue;
                        }
                        String k = pair[0].startsWith("\"") ? pair[0].substring(1) : pair[0];
                        String v = pair[1].endsWith("\"") ? pair[1].substring(0, pair[1].length() - 1) : pair[1];
                        
                        if (k.startsWith("tourism")) {
                            tourism = tourismType(v, false, false);
                            if (checked)
                                break;
                            checked = true;
                        }
                        else if (k.startsWith("name")) {
                            name = v;
                            if (checked)
                                break;
                            checked = true;
                        }
                    }
                }
                
                // not a relevant tourism node
                if (tourism < 0) {
                    continue;
                }

                // ignore nodes with either a blank name (what is a user supposed to do without a name?)
                // and such including "closed" in their name
                if (name != null && !(name.contains("closed") || name.contains("geschlossen"))) {
                    name = name.replace("&amp;", "&").replace("&quot;", "\"");
                    tourism_nodes.add(new TourismNode(lat, lon, tourism, name));
                }
    
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
            out_file.write(Long.toString(tourism_nodes.size()));
            out_file.newLine();
            
            
            // write nodes
            TLongObjectIterator<Node> it = nodes.iterator();
            long id = 0;
            while (it.hasNext())
            {
                it.advance();
                Node n = it.value();
                n.setID(id++);
                
                out_file.write(n.getLat() + " " + n.getLon());
                out_file.newLine();
            }
            
            
            // write edges
            it = nodes.iterator();
            while (it.hasNext())
            {
                it.advance();
                Node n = it.value();
                
                for (int e = 0; e < n.getDests().length; ++e) {
                    Node dest = nodes.get(n.getDests()[e]);
                    if (dest == null) {
                        System.out.println("No mapping for " + n.getDests()[e]);
                    }
                    long dist = Math.round(Distance.haversine(n.getLat(), n.getLon(), dest.getLat(), dest.getLon()));
                    out_file.write(n.getID() + " " + dest.getID() + " " + dist + " " + n.getHighwayTypes()[e]);
                    out_file.newLine();
                }
            }
            
            
            // write tourism nodes
            for (TourismNode tn : tourism_nodes)
            {
                out_file.write(tn.getLat() + " " + tn.getLon() + " " + tn.getTourismType());
                if (tn.hasName()) {
                    out_file.write(" " + tn.getName());
                }
                out_file.newLine();
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
    
    
    public int tourismType(String s, boolean include_long_term_stay, boolean include_attractions)
    {
        if (s.equals("alpine_hut"))
            return 1;
        else if (s.equals("apartment"))
            return include_long_term_stay ? 2 : -2;
        else if (s.equals("attraction"))
            return include_attractions ? 3 : -3;
        else if (s.equals("artwork"))
            return include_attractions ? 4 : -4;
        else if (s.equals("camp_site"))
            return include_attractions ? 5 : -5;
        else if (s.equals("caravan_site"))
            return include_attractions ? 6 : -6;
        else if (s.equals("chalet"))
            return include_long_term_stay ? 7 : -7;
        else if (s.equals("gallery"))
            return include_attractions ? 8 : -8;
        else if (s.equals("guest_house"))
            return include_long_term_stay ? 9 : -9;
        else if (s.equals("hostel"))
            return 10;
        else if (s.equals("hotel"))
            return 11;
        else if (s.equals("information"))
            return include_attractions ? 12 : -12;
        else if (s.equals("motel"))
            return 13;
        else if (s.equals("museum"))
            return include_attractions ? 14 : -14;
        else if (s.equals("picnic_site"))
            return include_attractions ? 15 : -15;
        else if (s.equals("theme_park"))
            return include_attractions ? 16 : -16;
        else if (s.equals("viewpoint"))
            return include_attractions ? 17 : -17;
        else if (s.equals("wilderness_hut"))
            return include_long_term_stay ? 18 : -18;
        else if (s.equals("zoo"))
            return include_attractions ? 19 : -19;
        // other
        //else if (s.equals("yes"))
        //    return -99;
        //else if (s.equals("user defined"))
        //    return -99;
        else
            return -1;
    }

}
