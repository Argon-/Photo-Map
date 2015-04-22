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

import org.jdesktop.swingx.mapviewer.GeoPosition;

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
     * Not-routable nodes info (tourism nodes)
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
        this.types = new double[29];
        Arrays.fill(this.types, -1);
        this.types[1]  = 1.3;   // motorway
        this.types[2]  = 1.2;   // primary
        this.types[3]  = 0.8;   // secondary
        this.types[4]  = 0.7;   // tertiary
        this.types[6]  = 1.3;   // trunk
        this.types[7]  = 0.5;   // road
        this.types[8]  = 0.45;  // residential
        this.types[9]  = 0.3;   // living_street
        this.types[10] = 0.5;   // turning_circle
        this.types[11] = 0.3;   // service
        this.types[12] = 0.5;   // unclassified
        
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
        
        oos.writeObject(this.lat);
        oos.writeObject(this.lon);

        oos.writeObject(this.source);
        oos.writeObject(this.target);
        oos.writeObject(this.offset);
        oos.writeObject(this.dist);
        oos.writeObject(this.dist_w);
        
        oos.writeObject(this.nlat);
        oos.writeObject(this.nlon);
        oos.writeObject(this.tour);
        oos.writeObject(this.name);

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
            this.lat  = new double[node_num];
            this.lon  = new double[node_num];

            for (int i = 0; i < node_num; ++i)
            {
                final String[] s = b.readLine().split(" ", 2);
                this.lat[i]  = Double.parseDouble(s[0]);
                this.lon[i]  = Double.parseDouble(s[1]);

                this.minLat = Math.min(minLat, this.lat[i]);
                this.maxLat = Math.max(maxLat, this.lat[i]);
                this.minLon = Math.min(minLon, this.lon[i]);
                this.maxLon = Math.max(maxLon, this.lon[i]);
            }
            
            
            // read edges
            this.source = new int[edge_num];
            this.target = new int[edge_num];
            this.offset = new int[node_num + 1];    // +1 because of getNeighbor()
            this.dist   = new int[edge_num];
            this.dist_w = new int[edge_num];
            Arrays.fill(this.offset, -1);
            
            for (int i = 0; i < edge_num; ++i)
            {
                final String[] s = b.readLine().split(" ");
                this.source[i] = Integer.parseInt(s[0]);
                this.target[i] = Integer.parseInt(s[1]);
                this.dist[i]   = Integer.parseInt(s[2]);

                final double weight  = this.types[Integer.parseInt(s[3])];
                this.dist_w[i] = (int) Math.round(this.dist[i]/weight);
                
                if (offset[this.source[i]] < 0) {
                    offset[this.source[i]] = i;
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
            this.nlat = new double[tour_num];
            this.nlon = new double[tour_num];
            this.tour = new byte[tour_num];
            this.name = new String[tour_num];

            for (int i = 0; i < tour_num; ++i)
            {
                final String[] s = b.readLine().split(" ", 4);
                this.nlat[i] = Double.parseDouble(s[0]);
                this.nlon[i] = Double.parseDouble(s[1]);
                this.tour[i] = Byte.parseByte(s[2]);
                this.name[i] = s.length > 3 ? s[3] : null;
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
            
            this.lat    = (double[]) ois.readObject();
            this.lon    = (double[]) ois.readObject();

            this.source = (int[])    ois.readObject();
            this.target = (int[])    ois.readObject();
            this.offset = (int[])    ois.readObject();
            this.dist   = (int[])    ois.readObject();
            this.dist_w = (int[])    ois.readObject();
            
            this.nlat   = (double[]) ois.readObject();
            this.nlon   = (double[]) ois.readObject();
            this.tour   = (byte[])    ois.readObject();
            this.name   = (String[]) ois.readObject();
            
            this.minLat = ois.readDouble();
            this.maxLat = ois.readDouble();
            this.minLon = ois.readDouble();
            this.maxLon = ois.readDouble();
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
            grid = new LookupGrid(this.lat, this.lon, this.minLat, this.maxLat, this.minLon, this.maxLon);
            ngrid = new LookupGrid(this.nlat, this.nlon);
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
        if (grid != null) {
            return grid.getNearestNode(lat, lon);
        }
        System.err.println("error: no lookup grid");
        return -1;
    }
    
    
    /**
     * Search the ID of the not-routable node closest to the given coordinates.
     * 
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     * @return ID of the found node or {@code -1}
     */
    public int getNearestNNode(double lat, double lon)
    {
        if (ngrid != null) {
            return ngrid.getNearestNode(lat, lon);
        }
        System.err.println("error: no lookup grid");
        return -1;
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
            GeoPosition g = new GeoPosition(this.lat[i], this.lon[i]);
            oa.addPoint(new OverlayElement(g, c[i % c.length], 7));
        }
        win.addPersistentOverlayAggregate(oa);
    }
    
    
    /**
     * Draw a visual marker for every not-routable node on the map.
     * 
     * @param win
     */
    public void drawNonRoutableNodes(MainWindow win)
    {
        Color[] c = {Color.RED};
        OverlayAggregate oa = new OverlayAggregate();
        
        for (int i = 0; i < nlat.length; ++i) {
            GeoPosition g = new GeoPosition(this.nlat[i], this.nlon[i]);
            oa.addPoint(new OverlayElement(g, c[i % c.length], 7)).addLabel(new OverlayLabel(this.name[i], g));
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
        if (n < 0 || n >= this.lat.length) {
            throw new RuntimeException("Bad Node ID: " + n + " (offset.length = " + this.lat.length + ")");
        }

        int[] r = new int[this.offset[n+1] - this.offset[n]];
        for (int i = 0; i < r.length; ++i) {
            r[i] = this.target[this.offset[n] + i];
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
        if (n < 0 || n >= this.lat.length) {
            throw new RuntimeException("Bad Node ID: " + n + " (offset.length = " + this.lat.length + ")");
        }
        
        if ((this.offset[n] + i) < this.offset[n+1])
            return this.target[this.offset[n] + i];
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
        if (from < 0 || from >= this.lat.length) {
            throw new RuntimeException("Bad Node ID: " + from + " (offset.length = " + this.lat.length + ")");
        }
        
        for (int i = this.offset[from]; i < this.offset[from+1]; ++i) {
            if (this.target[i] == to)
                return this.dist[i];
        }
        return -1;
    }

    
    /**
     * @return latitude of given node ID in decimal degrees
     */
    public double getLat(int n)
    {
        return this.lat[n];
    }

    
    /**
     * @return longitude of given node ID in decimal degrees
     */
    public double getLon(int n)
    {
        return this.lon[n];
    }
    
    
    /**
     * Return the location of {@code n} as a 
     * {@link org.jdesktop.swingx.mapviewer.GeoPosition GeoPosition} object.
     * 
     * @param n node ID
     * @return {@link org.jdesktop.swingx.mapviewer.GeoPosition GeoPosition} of {@code n}
     */
    public GeoPosition getPosition(int n)
    {
        return new GeoPosition(this.lat[n], this.lon[n]);
    }
    

    /**
     * Size of the loaded graph (number of nodes).
     */
    public int size()
    {
        return this.lat.length;
    }
    
    
    public int getIthEdgeDistFor(int n, int i)
    {
        return this.getIthEdgeDistFor(n, i, false);
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
            return this.dist_w[this.offset[n]+i];
        else
            return this.dist[this.offset[n]+i];
    }
    
    
    /**
     * Return a rectangle enclosing all routable latitude values of this 
     * graph in decimal degrees.
     * 
     * @return {@code double[]} of size 4
     */
    public double[] getBoundingRectLat()
    {
        return new double[] {this.maxLat, this.maxLat, this.minLat, this.minLat};
    }
    
    
    /**
     * Return a rectangle enclosing all routable longitude values of this 
     * graph in decimal degrees.
     * 
     * @return {@code double[]} of size 4
     */
    public double[] getBoundingRectLon()
    {
        return new double[] {this.maxLon, this.minLon, this.minLon, this.maxLon};
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
     * Color the nodes touched when searching for a nearest not-routable node.<br>
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