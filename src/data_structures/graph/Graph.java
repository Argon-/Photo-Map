package data_structures.graph;

import java.io.IOException;

import org.jdesktop.swingx.mapviewer.GeoPosition;



public interface Graph {
	public void save(String f) throws IOException;
	public int[] getNeighbors(int n);
	public int size();
	public int getIthEdgeDistFor(int n, int i);
	public int getIthEdgeDistFor(int n, int i, boolean weighted);
	public int getIthNeighbor(int n, int i);
	public int getDist(int from, int to);
	public double getLat(int n);
	public double getLon(int n);
	public GeoPosition getPosition(int n);
	public int getNearestNode(double lat, double lon);
	public double[] getBoundingRectLat();
	public double[] getBoundingRectLon();
}
