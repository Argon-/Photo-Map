package data_structures.graph;

import gui.MainWindow;
import gui.overlay.OverlayAggregate;
import gui.overlay.OverlayElement;
import gui.overlay.OverlayLabel;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.LinkedList;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import util.Distance;
import data_structures.grid.InvalidCoordinateArraysException;
import data_structures.grid.LookupGrid;



/**
 * See {@link #ArrayRepresentation(String) ArrayRepresentation}.
 */
final public class ArrayRepresentation implements Graph, Serializable {

    private static final long serialVersionUID = 8955873766213220121L;
    
    
    /*
     * Routable nodes info
     */
    private double    lat[] = null;
    private double    lon[] = null;
    private int    source[] = null;
    private int    target[] = null;
    private int    offset[] = null;
    private int      dist[] = null;
    private int    dist_w[] = null;
    private final double types[];
    
    /*
     * non-routable nodes info (tourism nodes)
     * Reasoning for not saving them with the routable nodes and providing
     * some kind of index structure to know which nodes are not routable is
     * the assumption of a way lower number of not routable nodes.
     * A lookup for these nodes can be provided tremendously faster when they
     * are not mixed with routable ones. Additionally, fields like "tourism"
     * or "name" waste way less space and one is not required to resort to other
     * (slower) data structures because of them being very sparse.
     */
    private double nlat[] = null;
    private double nlon[] = null;
    private byte   tour[] = null;
    private String name[] = null;

    private double minLat =  Double.MAX_VALUE;
    private double maxLat = -Double.MAX_VALUE;
    private double minLon =  Double.MAX_VALUE;
    private double maxLon = -Double.MAX_VALUE;
    
    private LookupGrid grid = null;
    private LookupGrid ngrid = null;
    
    
    /**
     * Read graph data from the given file and provide various access methods.
     * <br><br>
     * This class is effectively final. No (global) state modification occurs after
     * loading of the graph. Getters don't have side effects and get by without locking.
     * <br><br>
     * The graph can either be loaded from a plain text file or as the binary result
     * of {@link #save(String)}, the latter is considerably faster.
     * <br><br>
     * Graph file specification:
     * <pre>
     * {@code
     * <number_of_nodes>
     * <number_of_edges>
     * <number_of_not_routable_nodes>
     * <node>  * <number_of_nodes>
     * <edge>  * <number_of_edges>
     * <nnode> * <number_of_not_routable_nodes>
     * }
     * </pre>
     * With
     * <pre>
     * node  ::= double double            // lat lon
     * edge  ::= int int int int          // node_id node_id dist hightway_type
     * nnode ::= double double int string // lat lon tourism_type name
     * </pre>
     * The ID of a node is implicitly given by its position (first node = 0, second node = 1, ...).
     * 
     * @param f graph file
     * @throws InvalidGraphFormatException
     * @throws IOException
     */
    public ArrayRepresentation(String f) throws InvalidGraphFormatException, IOException
    {
        super();
        types = new double[29];
        Arrays.fill(types, -1);
        types[1]  = 1.3;   // motorway
        types[2]  = 1.2;   // primary
        types[3]  = 0.8;   // secondary
        types[4]  = 0.7;   // tertiary
        types[6]  = 1.3;   // trunk
        types[7]  = 0.5;   // road
        types[8]  = 0.45;  // residential
        types[9]  = 0.3;   // living_street
        types[10] = 0.5;   // turning_circle
        types[11] = 0.3;   // service
        types[12] = 0.5;   // unclassified
        
        load(f);
    }
    
    
    /**
     * Save the graph as a binary blob.
     * 
     * @param f file location
     */
    public void save(String f) throws IOException
    {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
        
        oos.writeObject(lat);
        oos.writeObject(lon);

        oos.writeObject(source);
        oos.writeObject(target);
        oos.writeObject(offset);
        oos.writeObject(dist);
        oos.writeObject(dist_w);
        
        oos.writeObject(nlat);
        oos.writeObject(nlon);
        oos.writeObject(tour);
        oos.writeObject(name);

        oos.writeDouble(minLat);
        oos.writeDouble(maxLat);
        oos.writeDouble(minLon);
        oos.writeDouble(maxLon);
        
        oos.close();
    }
    
    
    private void loadFromText(BufferedReader b) throws IOException, InvalidGraphFormatException
    {
        try {
            System.out.println("Found graph text file, parsing...");
            
            final int node_num = Integer.parseInt(b.readLine());
            final int edge_num = Integer.parseInt(b.readLine());
            final int tour_num = Integer.parseInt(b.readLine());
            
            
            // read nodes
            lat  = new double[node_num];
            lon  = new double[node_num];

            for (int i = 0; i < node_num; ++i)
            {
                final String[] s = b.readLine().split(" ", 2);
                lat[i]  = Double.parseDouble(s[0]);
                lon[i]  = Double.parseDouble(s[1]);

                minLat = Math.min(minLat, lat[i]);
                maxLat = Math.max(maxLat, lat[i]);
                minLon = Math.min(minLon, lon[i]);
                maxLon = Math.max(maxLon, lon[i]);
            }
            
            
            // read edges
            source = new int[edge_num];
            target = new int[edge_num];
            offset = new int[node_num + 1];    // +1 because of getNeighbor()
            dist   = new int[edge_num];
            dist_w = new int[edge_num];
            Arrays.fill(offset, -1);
            
            for (int i = 0; i < edge_num; ++i)
            {
                final String[] s = b.readLine().split(" ");
                source[i] = Integer.parseInt(s[0]);
                target[i] = Integer.parseInt(s[1]);
                dist[i]   = Integer.parseInt(s[2]);

                final double weight  = types[Integer.parseInt(s[3])];
                dist_w[i] = (int) Math.round(dist[i]/weight);
                
                if (offset[source[i]] < 0) {
                    offset[source[i]] = i;
                }
            }
            
            // fix offset array
            int last = source.length;
            for (int i = offset.length - 1; i > 0; --i) {
                if (offset[i] < 0) {
                    offset[i] = last;
                }
                else {
                    last = offset[i];
                }
            }

            
            // read tourism nodes
            nlat = new double[tour_num];
            nlon = new double[tour_num];
            tour = new byte[tour_num];
            name = new String[tour_num];

            for (int i = 0; i < tour_num; ++i)
            {
                final String[] s = b.readLine().split(" ", 4);
                nlat[i] = Double.parseDouble(s[0]);
                nlon[i] = Double.parseDouble(s[1]);
                tour[i] = Byte.parseByte(s[2]);
                name[i] = s.length > 3 ? s[3] : null;
            }

            
        } 
        catch (NumberFormatException e) {
            throw new InvalidGraphFormatException("Invalid graph text file format");
        }
    }
    
    
    private void loadFromBinary(ObjectInputStream ois) throws IOException, InvalidGraphFormatException
    {
        try {
            System.out.println("Found serialized graph, reading...");
            
            lat    = (double[]) ois.readObject();
            lon    = (double[]) ois.readObject();

            source = (int[])    ois.readObject();
            target = (int[])    ois.readObject();
            offset = (int[])    ois.readObject();
            dist   = (int[])    ois.readObject();
            dist_w = (int[])    ois.readObject();
            
            nlat   = (double[]) ois.readObject();
            nlon   = (double[]) ois.readObject();
            tour   = (byte[])    ois.readObject();
            name   = (String[]) ois.readObject();
            
            minLat = ois.readDouble();
            maxLat = ois.readDouble();
            minLon = ois.readDouble();
            maxLon = ois.readDouble();
        }
        catch (ClassNotFoundException e) {
            throw new InvalidGraphFormatException("Invalid serialized graph format");
        }

    }
    
    
    private void load(String f) throws InvalidGraphFormatException, IOException
    {
        try {
            final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            loadFromBinary(ois);
            ois.close();
        }
        catch (StreamCorruptedException e1) {
            final BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
            loadFromText(b);
            b.close();
        }
        
        try {
            grid = new LookupGrid(lat, lon, minLat, maxLat, minLon, maxLon);
            ngrid = new LookupGrid(nlat, nlon);
        }
        catch (InvalidCoordinateArraysException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Search the ID of the node closest to the given coordinates.
     * 
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     * @return ID of the found node or {@code -1}
     */
    public int getNearestNode(double lat, double lon)
    {
        return getNearestNode(grid, lat, lon);
    }
    
    
    /**
     * Search the ID of the non-routable node closest to the given coordinates.
     * 
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     * @return ID of the found node or {@code -1}
     */
    public int getNearestNNode(double lat, double lon)
    {
        return getNearestNode(ngrid, lat, lon);
    }
    
    
    private int getNearestNode(LookupGrid lg, double lat, double lon)
    {
        if (lg != null) {
            return lg.getNearestNode(lat, lon);
        }
        System.err.println("error: no lookup grid");
        return -1;
    }
    
    
    private LinkedList<Integer> getNodesInRange(double[] la, double[] lo, double lat, double lon, int range)
    {
        final double max_lat = Distance.translateLat(lat,  range);
        final double min_lat = Distance.translateLat(lat, -range);
        final double max_lon = Distance.translateLon(lon,  range, lat);
        final double min_lon = Distance.translateLon(lon, -range, lat);
        final LinkedList<Integer> r = new LinkedList<Integer>();
        
        for (int i = 0; i < la.length; ++i)
        {
            if (la[i] > min_lat && la[i] < max_lat && lo[i] > min_lon && lo[i] < max_lon && Distance.haversine(la[i], lo[i], lat, lon) < range) {
                r.add(i);
            }
        }
        return r;
    }
    
    
    public LinkedList<Integer> getNodesInRange(double lat, double lon, int range)
    {
        return getNodesInRange(this.lat, this.lon, lat, lon, range);
    }
    
    
    public LinkedList<Integer> getNNodesInRange(double lat, double lon, int range)
    {
        return getNodesInRange(this.nlat, this.nlon, lat, lon, range);
    }
    
    
    /**
     * Draw a visual marker for every routable node on the map.
     * 
     * @param win
     */
    public void drawRoutableNodes(MainWindow win)
    {
        Color[] c = {Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.WHITE};
        OverlayAggregate oa = new OverlayAggregate();
        
        for (int i = 0; i < lat.length; ++i) {
            GeoPosition g = new GeoPosition(lat[i], lon[i]);
            oa.addPoint(new OverlayElement(g, c[i % c.length], 7));
        }
        win.addPersistentOverlayAggregate(oa);
    }
    
    
    /**
     * Draw a visual marker for every non-routable node on the map.
     * 
     * @param win
     */
    public void drawNonRoutableNodes(MainWindow win)
    {
        Color[] c = {Color.RED};
        OverlayAggregate oa = new OverlayAggregate();
        
        for (int i = 0; i < nlat.length; ++i) {
            GeoPosition g = new GeoPosition(nlat[i], nlon[i]);
            oa.addPoint(new OverlayElement(g, c[i % c.length], 7)).addLabel(new OverlayLabel(name[i], g));
        }
        
        win.addPersistentOverlayAggregate(oa);
    }
    
    
    /**
     * Return an array of {@code n}'s neighbors (outgoing edges).<br>
     * This assumes directed edges.
     * 
     * @param n node ID
     * @return array of target node IDs<br>
     *         Might be empty in case no such node exists
     * @throws RuntimeException when not {@code -1 < n < }{@link #size()}
     */
    public int[] getNeighbors(int n)
    {
        if (n < 0 || n >= lat.length) {
            throw new RuntimeException("Bad Node ID: " + n + " (offset.length = " + lat.length + ")");
        }

        int[] r = new int[offset[n+1] - offset[n]];
        for (int i = 0; i < r.length; ++i) {
            r[i] = target[offset[n] + i];
        }
        return r;
    }
    
    
    /**
     * Get the {@code i}'th neighbor of node {@code n}.
     * 
     * @param n node ID
     * @param i 
     * @return node ID of {@code i}'th neighbor or {@code -1}
     * @throws RuntimeException when not {@code -1 < n < }{@link #size()}
     */
    public int getIthNeighbor(int n, int i)
    {
        if (n < 0 || n >= lat.length) {
            throw new RuntimeException("Bad Node ID: " + n + " (offset.length = " + lat.length + ")");
        }
        
        if ((offset[n] + i) < offset[n+1])
            return target[offset[n] + i];
        return -1;
    }
    
    
    /**
     * Searches all outgoing edges of {@code from} and
     * returns the distance of the first edge found with
     * target {@code to}.
     * 
     * @param from node ID
     * @param to node ID
     * @return dist from {@code from} to {@code to} or {@code -1}
     * @throws RuntimeException when not {@code -1 < n < }{@link #size()}
     */
    public int getDist(int from, int to)
    {
        if (from < 0 || from >= lat.length) {
            throw new RuntimeException("Bad Node ID: " + from + " (offset.length = " + lat.length + ")");
        }
        
        for (int i = offset[from]; i < offset[from+1]; ++i) {
            if (target[i] == to)
                return dist[i];
        }
        return -1;
    }

    
    /**
     * @return latitude of given node ID in decimal degrees
     */
    public double getLat(int n)
    {
        return lat[n];
    }
    
    
    /**
     * @return latitude of given non-routable node ID in decimal degrees
     */
    public double getNLat(int n)
    {
        return nlat[n];
    }

    
    /**
     * @return longitude of given node ID in decimal degrees
     */
    public double getLon(int n)
    {
        return lon[n];
    }
    
    
    /**
     * @return longitude of given non-routable node ID in decimal degrees
     */
    public double getNLon(int n)
    {
        return nlon[n];
    }
    
    
    /**
     * Return the location of node {@code n} as a 
     * {@link org.jdesktop.swingx.mapviewer.GeoPosition GeoPosition} object.
     * 
     * @param n node ID
     * @return {@link org.jdesktop.swingx.mapviewer.GeoPosition GeoPosition} of {@code n}
     */
    public GeoPosition getPosition(int n)
    {
        return new GeoPosition(lat[n], lon[n]);
    }
    
    
    /**
     * Return the location of non-routable node {@code n} as a 
     * {@link org.jdesktop.swingx.mapviewer.GeoPosition GeoPosition} object.
     * 
     * @param n node ID
     * @return {@link org.jdesktop.swingx.mapviewer.GeoPosition GeoPosition} of {@code n}
     */
    public GeoPosition getNPosition(int n)
    {
        return new GeoPosition(nlat[n], nlon[n]);
    }
    
    
    /**
     * Return the name of the given non-routable node {@code n}.
     * @param n
     * @return name of node
     */
    public String getName(int n)
    {
        return name[n];
    }
    

    /**
     * Size of the loaded graph (number of nodes).
     */
    public int size()
    {
        return lat.length;
    }
    
    
    public int getIthEdgeDistFor(int n, int i)
    {
        return getIthEdgeDistFor(n, i, false);
    }
    
    
    /**
     * Get the distance of the {@code i}'th edge of node {@code n}.
     * 
     * @param n node ID
     * @param i 
     * @param weighted dist weighted by street type
     * @return distance
     */
    public int getIthEdgeDistFor(int n, int i, boolean weighted)
    {
        if (weighted)
            return dist_w[offset[n]+i];
        else
            return dist[offset[n]+i];
    }
    
    
    /**
     * Return a rectangle enclosing all routable latitude values of this 
     * graph in decimal degrees.
     * 
     * @return {@code double[]} of size 4
     */
    public double[] getBoundingRectLat()
    {
        return new double[] {maxLat, maxLat, minLat, minLat};
    }
    
    
    /**
     * Return a rectangle enclosing all routable longitude values of this 
     * graph in decimal degrees.
     * 
     * @return {@code double[]} of size 4
     */
    public double[] getBoundingRectLon()
    {
        return new double[] {maxLon, minLon, minLon, maxLon};
    }
    
    
    /**
     * Color the nodes touched when searching for a nearest node.<br>
     * Not necessarily thread safe.
     * 
     * @param t
     * @param w
     */
    public void visualizeGridLookup(boolean t, MainWindow w)
    {
        if (grid != null)
            grid.setVisualize(t, w);
    }


    /**
     * Color the nodes touched when searching for a nearest non-routable node.<br>
     * Not necessarily thread safe.
     * 
     * @param t
     * @param w
     */
    public void visualizeNGridLookup(boolean t, MainWindow w)
    {
        if (ngrid != null)
            ngrid.setVisualize(t, w);
    }

}