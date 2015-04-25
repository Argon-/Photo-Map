package data_structures.graph;

import gui.MainWindow;

import java.io.IOException;
import java.util.LinkedList;

import org.jdesktop.swingx.mapviewer.GeoPosition;



/**
 * Interface defining the API for a graph implementation.<br>
 * See {@link data_structures.graph.ArrayRepresentation ArrayRepresentation} for detailed comments and explanations.
 */
public interface Graph {
    
    public void save(String f) throws IOException;

    // getters using locations
    public int getNearestNode(double lat, double lon);
    public int getNearestNNode(double lat, double lon);
    public int getNearestNode(GeoPosition pos);
    public int getNearestNNode(GeoPosition pos);
    public LinkedList<Integer> getNodesInRange(double lat, double lon, int range);
    public LinkedList<Integer> getNNodesInRange(double lat, double lon, int range);

    // getters using implementation-internal IDs
    public int getIthEdgeDistFor(int n, int i);
    public int getIthEdgeDistFor(int n, int i, boolean weighted);
    public int getIthNeighbor(int n, int i);
    public int[] getNeighbors(int n);
    public int getDist(int from, int to);
    public double getLat(int n);
    public double getNLat(int n);
    public double getLon(int n);
    public double getNLon(int n);
    public GeoPosition getPosition(int n);
    public GeoPosition getNPosition(int n);
    public String getName(int n);

    // getters for graph (meta)data
    public double[] getBoundingRectLat();
    public double[] getBoundingRectLon();
    public int size();

    // visualization
    public void visualizeGridLookup(boolean t, MainWindow w);
    public void visualizeNGridLookup(boolean t, MainWindow w);
    public void drawRoutableNodes(MainWindow win);
    public void drawNonRoutableNodes(MainWindow win);

}
