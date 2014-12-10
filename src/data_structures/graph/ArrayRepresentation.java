package data_structures.graph;

import gui.main_window.MainWindow;

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

import util.Distance;
import util.OverlayAggregate;
import util.OverlayElement;



final public class ArrayRepresentation implements Graph, Serializable {

	private static final long serialVersionUID = 8955873766213220121L;
	
	private double   lat[] = null;
	private double   lon[] = null;
	private int     elev[] = null;
	private int   source[] = null;
	private int   target[] = null;
	private int     dist[] = null;
	private int   offset[] = null;
	private int     type[] = null;
	private double types[] = null;

	private int GRID_LAT_CELLS = 10;	// TODO: dynamisch?
	private int GRID_LON_CELLS = 13;
	
	private double LAT_CELL_SIZE;
	private double LON_CELL_SIZE;
	
	private int[] grid        = null;
	private int[] grid_offset = null;
	
	private double minLat =  Double.MAX_VALUE;
	private double maxLat = -Double.MAX_VALUE;
	private double minLon =  Double.MAX_VALUE; 
	private double maxLon = -Double.MAX_VALUE;

	
	
	public ArrayRepresentation()
	{
		this.types = new  double[] {-1.0, 1.3, 1.2, 0.8, 0.7, -1, 1.3, 0.5, 0.45, 0.3, 0.5, 0.3, 0.5};
	}
	
	
	public void save(String f) throws IOException
	{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		
		oos.writeObject(this.lat);
		oos.writeObject(this.lon);
		oos.writeObject(this.elev);
		oos.writeObject(this.source);
		oos.writeObject(this.target);
		oos.writeObject(this.dist);
		oos.writeObject(this.offset);
		oos.writeObject(this.type);
		oos.writeObject(this.types);
		
		oos.writeObject(this.grid);
		oos.writeObject(this.grid_offset);
		
		oos.writeDouble(minLat);
		oos.writeDouble(maxLat);
		oos.writeDouble(minLon);
		oos.writeDouble(maxLon);
		
		oos.writeInt(GRID_LAT_CELLS);
		oos.writeInt(GRID_LON_CELLS);
		
		oos.close();
	}
	
	
	public void load(String f) throws InvalidGraphFormatException, IOException
	{
		System.out.println("Probing file header");
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			System.out.println("Found serialized graph, reading...");
			
			this.lat = (double[]) ois.readObject();
			this.lon = (double[]) ois.readObject();
			this.elev = (int[]) ois.readObject();
			this.source = (int[]) ois.readObject();
			this.target = (int[]) ois.readObject();
			this.dist = (int[]) ois.readObject();
			this.offset = (int[]) ois.readObject();
			this.type = (int[]) ois.readObject();
			this.types = (double[]) ois.readObject();
			
			this.grid = (int[]) ois.readObject();
			this.grid_offset = (int[]) ois.readObject();
			
			this.minLat = ois.readDouble();
			this.maxLat = ois.readDouble();
			this.minLon = ois.readDouble();
			this.maxLon = ois.readDouble();

			this.GRID_LAT_CELLS = ois.readInt();
			this.GRID_LON_CELLS = ois.readInt();
			
			ois.close();
			
			LAT_CELL_SIZE = (this.maxLat - this.minLat) / GRID_LAT_CELLS;
			LON_CELL_SIZE = (this.maxLon - this.minLon) / GRID_LON_CELLS;
			
			return;
		}
		catch (StreamCorruptedException e1) {
			// do nothing
		}
		catch (ClassNotFoundException e) {
			throw new InvalidGraphFormatException("Invalid serialized graph format");
		}
		
		
		
		try {
			final BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
			System.out.println("Found graph text file, parsing...");
			
			final int node_num = Integer.parseInt(b.readLine());
			final int edge_num = Integer.parseInt(b.readLine());
			
			this.lat  = new double[node_num];
			this.lon  = new double[node_num];
			this.elev = new int[node_num];

			for (int i = 0; i < node_num; ++i)
			{
				final String[] s = b.readLine().split(" ");
				this.lat[i]  = Double.parseDouble(s[1]);
				this.lon[i]  = Double.parseDouble(s[2]);
				this.elev[i] = Integer.parseInt(s[3]);
				
				this.minLat = Math.min(minLat, this.lat[i]);
				this.maxLat = Math.max(maxLat, this.lat[i]);
				this.minLon = Math.min(minLon, this.lon[i]);
				this.maxLon = Math.max(maxLon, this.lon[i]);
			}
			
			this.source = new int[edge_num];
			this.target = new int[edge_num];
			this.dist   = new int[edge_num];
			this.offset = new int[node_num + 1];	// +1 because of getNeighbor()
			this.type   = new int[/*edge_num*/1];	// dummy
			Arrays.fill(this.offset, -1);
			
			for (int i = 0; i < edge_num; ++i)
			{
				final String[] s = b.readLine().split(" ");
				this.source[i] = Integer.parseInt(s[0]);
				this.target[i] = Integer.parseInt(s[1]);
				this.dist[i]   = Integer.parseInt(s[2]);
				//int type = Integer.parseInt(s[3]);
				
				if (offset[this.source[i]] < 0) {
					offset[this.source[i]] = i;
				}
			}
			
			int last = source.length;
			for (int i = offset.length - 1; i > 0; --i)
			{
				if (offset[i] < 0) {
					offset[i] = last;
				}
				else {
					last = offset[i];
				}
			}

			b.close();
			this.buildGrid();
		} 
		catch (NumberFormatException e) {
			e.printStackTrace();
			throw new InvalidGraphFormatException("Invalid graph text file format");
		}
	}
	
	
	public void buildGrid()
	{
		LAT_CELL_SIZE = (this.maxLat - this.minLat) / GRID_LAT_CELLS;
		LON_CELL_SIZE = (this.maxLon - this.minLon) / GRID_LON_CELLS;

		grid = new int[this.lon.length];
		grid_offset = new int[GRID_LAT_CELLS * GRID_LON_CELLS + 1];
		int[] count = new int[GRID_LAT_CELLS * GRID_LON_CELLS];
		Arrays.fill(grid, -1);
		
		// first pass
		for (int i = 0; i < this.lon.length; ++i)
		{
			int lat_cell = this.lat[i] == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((this.lat[i] - this.minLat) / LAT_CELL_SIZE);
			int lon_cell = this.lon[i] == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((this.lon[i] - this.minLon) / LON_CELL_SIZE);
			count[lat_cell * GRID_LON_CELLS + lon_cell] += 1;
		}
		
		
		for (int i = 0; i < count.length; ++i)
		{
			int lat = i / (GRID_LAT_CELLS-1);
			int lon = i % GRID_LON_CELLS;
			grid_offset[i+1] = grid_offset[i] + count[i];
			//System.out.println("Offsets: cell " + lat + "," + lon + " from " + grid_offset[i] + " to " + grid_offset[i+1] + " containing " + count[i] + " nodes");
			//System.out.println(lat + ", " + lon + " = " + count[i] + " \tnodes" + " \toffset = " + grid_offset[i]);
		}
		
		// second pass
		for (int i = 0; i < this.lon.length; ++i)
		{
			int lat_cell = this.lat[i] == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((this.lat[i] - this.minLat) / LAT_CELL_SIZE);
			int lon_cell = this.lon[i] == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((this.lon[i] - this.minLon) / LON_CELL_SIZE);
			int pos = grid_offset[lat_cell * GRID_LON_CELLS + lon_cell] + (count[lat_cell * GRID_LON_CELLS + lon_cell]-- - 1);
			//System.out.println("Node in cell " + lat_cell + "," + lon_cell + " has position " + pos);
			grid[pos] = i;
		}
		
		for (int i = 0; i < count.length; ++i)
		{
			//System.out.println("Remaining count for " + i + " = " + count[i]);
		}
		
		for (int i = 0; i < this.grid.length; ++i)
		{
			if (this.grid[i] == -1)
				System.out.println("Unset positions");
		}

	}
		
	
	public int getNearestNode(double lat, double lon)
	{
		if (lat < this.minLat || lat > this.maxLat || lon < this.minLon || lon > this.maxLon) {
			return -1;
		}
		
		int lat_cell = lat == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((lat - this.minLat) / LAT_CELL_SIZE);
		int lon_cell = lon == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((lon - this.minLon) / LON_CELL_SIZE);
		int index = lat_cell * GRID_LON_CELLS + lon_cell;
		
		int min_id = -1;
		double min_dist = Double.MAX_VALUE;
		OverlayAggregate oa = new OverlayAggregate();
		
		// suche in "start zelle"
		//System.out.println("Searching cell " + lat_cell + "," + lon_cell + " with " + (grid_offset[index + 1] - grid_offset[index]) + " nodes");
		
		for (int i = 0; i < grid_offset[index + 1] - grid_offset[index]; ++i)
		{
			int pos = this.grid[grid_offset[index] + i];
			double dist = Distance.haversine(lat, lon, this.lat[pos], this.lon[pos]);
			
			if (dist < min_dist) {
				min_dist = dist;
				min_id = pos;
			}
			oa.addPoint(new OverlayElement(this.lat[pos], this.lon[pos], Color.MAGENTA, 3));
		}
		
		
		// ring-suche
		// mindestens 1 level, neben der suche in der start zelle
		// abbruch-kriterium: nachdem etwas gefunden wurde (im ring, start zelle zählt nicht) noch 1 ring
		int ring = 1; // 4**ring kästchen, pro ring werden es auf jeder kante 2 kästchen mehr
		boolean expandAgain = true; 
		
		
		
		MainWindow.overlayLines.add(oa);
		return min_id;
	}
	
	
	public void drawCells()
	{
		Color[] c = {Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY};
		int curr_c = 0;
		
		
		/*for (int i = 0; i < this.lat.length; ++i) {
			int lat_cell = this.lat[i] == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((this.lat[i] - this.minLat) / LAT_CELL_SIZE);
			int lon_cell = this.lon[i] == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((this.lon[i] - this.minLon) / LON_CELL_SIZE);
			GeoPosition g = new GeoPosition(this.lat[i], this.lon[i]);
			MainWindow.persistentOverlayLines.add(new OverlayLine(g, g, c[(lat_cell + lon_cell) % c.length], 2));
		}*/
		
		for (int base = 0; base < this.grid_offset.length-1; ++base)
		{
			for (int i = this.grid_offset[base]; i < this.grid_offset[base+1]; ++i)
			{
				int pos = this.grid[i];
				int lat_cell = this.lat[pos] == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((this.lat[pos] - this.minLat) / LAT_CELL_SIZE);
				int lon_cell = this.lon[pos] == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((this.lon[pos] - this.minLon) / LON_CELL_SIZE);
				//System.out.println("Searching node in cell " + lat_cell + "," + lon_cell);
				GeoPosition g = new GeoPosition(this.lat[pos], this.lon[pos]);
				MainWindow.persistentOverlayLines.add(new OverlayElement(g, c[base % c.length], 3));
			}
		}
	}
	
	
	/**
	 * Return a set of <code>n</code>'s neighbors.
	 * <br>
	 * This assumes directed edges.
	 * 
	 * @param n
	 * @return Array of target node IDs. <br>
	 *         Might be empty in case no such node exists.
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
	 * Searches all outgoing edges of <code>from</code> and
	 * returns the distance of the first edge found with
	 * target <code>to</code>, else <code>-1</code>.
	 * 
	 * @param from
	 * @param to
	 * @return dist from <code>from</code> to <code>to</code>
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
	 * This returns no clone but the internally used array reference.
	 * 
	 * @return dist array
	 */
	public int[] getDistArray()
	{
		return this.dist;
	}
	
	
	/**
	 * This returns no clone but the internally used array reference.
	 * 
	 * @return latitude array
	 */
	public double[] getLatArray()
	{
		return this.lat;
	}
	
	
	public double getLat(int n)
	{
		return this.lat[n];
	}

	
	/**
	 * This returns no clone but the internally used array reference.
	 * 
	 * @return longitude array
	 */
	public double[] getLonArray()
	{
		return this.lon;
	}
	
	
	public double getLon(int n)
	{
		return this.lon[n];
	}
	
	
	public GeoPosition getPosition(int n)
	{
		return new GeoPosition(this.lat[n], this.lon[n]);
	}
	

	public int size()
	{
		return this.lat.length;
	}
	
	
	public int getOffset(int n)
	{
		return this.offset[n];
	}
	
	
	public int getIthEdgeDistFor(int n, int i)
	{
		return this.dist[this.offset[n]+i];
	}
}