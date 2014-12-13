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
	
	private final double GRID_FACTOR = 0.000005;
	private int GRID_LAT_CELLS;
	private int GRID_LON_CELLS;
	
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
		} 
		catch (NumberFormatException e) {
			e.printStackTrace();
			throw new InvalidGraphFormatException("Invalid graph text file format");
		}
	}
	
	
	private void loadFromBinary(ObjectInputStream ois) throws IOException, InvalidGraphFormatException
	{
		try {
			System.out.println("Found serialized graph, reading...");
			
			this.lat    = (double[]) ois.readObject();
			this.lon    = (double[]) ois.readObject();
			this.elev   = (int[])    ois.readObject();
			this.source = (int[])    ois.readObject();
			this.target = (int[])    ois.readObject();
			this.dist   = (int[])    ois.readObject();
			this.offset = (int[])    ois.readObject();
			this.type   = (int[])    ois.readObject();
			this.types  = (double[]) ois.readObject();
			
			this.minLat = ois.readDouble();
			this.maxLat = ois.readDouble();
			this.minLon = ois.readDouble();
			this.maxLon = ois.readDouble();
		}
		catch (ClassNotFoundException e) {
			throw new InvalidGraphFormatException("Invalid serialized graph format");
		}

	}
	
	
	public void load(String f) throws InvalidGraphFormatException, IOException
	{
		System.out.println("Probing file header");
		
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
		
		final int m = (int) Math.ceil(this.lat.length * GRID_FACTOR);
		GRID_LAT_CELLS =  m < 1 ? 1 : (int) Math.ceil(m * (this.maxLon - this.minLon) / (this.maxLat - this.minLat));
		GRID_LON_CELLS =  m < 1 ? 1 : m;
		
		LAT_CELL_SIZE = (this.maxLat - this.minLat) / GRID_LAT_CELLS;
		LON_CELL_SIZE = (this.maxLon - this.minLon) / GRID_LON_CELLS;
		
		System.out.println("GRID_LAT_CELLS = " + GRID_LAT_CELLS);
		System.out.println("GRID_LON_CELLS = " + GRID_LON_CELLS);
			
		this.buildGrid();
	}
	
	
	private void buildGrid()
	{
		long t = System.nanoTime();
				
		this.grid = new int[this.lon.length];
		this.grid_offset = new int[GRID_LAT_CELLS * GRID_LON_CELLS + 1];
		int[] count = new int[GRID_LAT_CELLS * GRID_LON_CELLS];
		
		// first pass
		for (int i = 0; i < this.lon.length; ++i)
		{
			int lat_cell = this.lat[i] == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((this.lat[i] - this.minLat) / LAT_CELL_SIZE);
			int lon_cell = this.lon[i] == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((this.lon[i] - this.minLon) / LON_CELL_SIZE);
			count[lat_cell * GRID_LON_CELLS + lon_cell] += 1;
		}
		
		// build offset array
		for (int i = 0; i < count.length; ++i)
		{
			grid_offset[i+1] = grid_offset[i] + count[i];
		}
		
		// second pass
		for (int i = 0; i < this.lon.length; ++i)
		{
			int lat_cell = this.lat[i] == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((this.lat[i] - this.minLat) / LAT_CELL_SIZE);
			int lon_cell = this.lon[i] == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((this.lon[i] - this.minLon) / LON_CELL_SIZE);
			int pos = grid_offset[lat_cell * GRID_LON_CELLS + lon_cell] + (count[lat_cell * GRID_LON_CELLS + lon_cell]-- - 1);
			grid[pos] = i;
		}
		
		System.out.println(String.format("Building grid took %.6f seconds", (System.nanoTime() - t) / 1000000000.0));
	}
	
	
	//int yyyy = 0;
	private int searchMinInCell(double lat, double lon, int lat_cell, int lon_cell, int last_min_id)
	{
		if (lat_cell < 0 || lon_cell < 0 || lat_cell >= GRID_LAT_CELLS || lon_cell >= GRID_LON_CELLS) {
			//System.out.println("Searching cell (" + lat_cell + "," + lon_cell + ")  (skipping)");
			return last_min_id;
		}
		//System.out.println("Searching cell (" + lat_cell + "," + lon_cell + ")");
		//OverlayAggregate oa = new OverlayAggregate();

		int min_id = last_min_id < 0 ? -1 : last_min_id;
		double min_dist = last_min_id > -1 ? Distance.haversine(lat, lon, this.lat[last_min_id], this.lon[last_min_id]) : Double.MAX_VALUE;
		
		int index = lat_cell * GRID_LON_CELLS + lon_cell;
		for (int i = 0; i < grid_offset[index + 1] - grid_offset[index]; ++i)
		{
			final int pos = this.grid[grid_offset[index] + i];
			final double dist = Distance.haversine(lat, lon, this.lat[pos], this.lon[pos]);
			
			if (dist < min_dist) {
				min_dist = dist;
				min_id = pos;
			}
			//oa.addPoint(new OverlayElement(this.lat[pos], this.lon[pos], yyyy % 2 == 0 ? Color.MAGENTA : new Color(255, 0, 144), 3));
		}

		//MainWindow.overlayLines.add(oa);
		return min_id;
	}
	
	
	public int getNearestNode(double lat, double lon)
	{
		if (lat < this.minLat || lat > this.maxLat || lon < this.minLon || lon > this.maxLon) {
			return -1;
		}
		
		final int lat_center = lat == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((lat - this.minLat) / LAT_CELL_SIZE);
		final int lon_center = lon == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((lon - this.minLon) / LON_CELL_SIZE);
		
		/*
		 * search in expanding rings, originating from (lat_center, lon_center)
		 * the ring expands by one per iteration, starting with 0 ( = the cell containing the clicked position)
		 * after a node was found (mid_id != -1) we expand the ring one more time
		 * —————————————————————————————
		 * | 3 | 3 | 3 | 3 | 3 | 3 | 3 |
		 * | 3 | 2 | 2 | 2 | 2 | 2 | 3 |
		 * | 3 | 2 | 1 | 1 | 1 | 2 | 3 |
		 * | 3 | 2 | 1 | 0 | 1 | 2 | 3 | 
		 * | 3 | 2 | 1 | 1 | 1 | 2 | 3 |
		 * | 3 | 2 | 2 | 2 | 2 | 2 | 3 |
		 * | 3 | 3 | 3 | 3 | 3 | 3 | 3 |
		 * —————————————————————————————
		 */
		
		int min_id = -1;
		int ring = 0;
		boolean expandAgain = true;
		
		do
		{
			if (min_id > -1 || (ring > GRID_LAT_CELLS && ring > GRID_LON_CELLS)) {
				expandAgain = false;
			}
			
			// seek upwards to this ring's starting position
			int lat_curr = lat_center + ring;
			int lon_curr = lon_center;
			
			min_id = searchMinInCell(lat, lon, lat_curr, lon_curr, min_id);
			
			// iterate right
			for (int i = 1; i < ring; ++i) {
				min_id = searchMinInCell(lat, lon, lat_curr, lon_curr + i, min_id);
			}
			lon_curr = lon_curr + ring;
			
			// iterate downwards
			for (int i = 0; i < ring * 2; ++i) {
				min_id = searchMinInCell(lat, lon, lat_curr - i, lon_curr, min_id);
			}
			lat_curr = lat_curr - (ring * 2);
			
			// iterate left
			for (int i = 0; i < ring * 2; ++i) {
				min_id = searchMinInCell(lat, lon, lat_curr, lon_curr - i, min_id);
			}
			lon_curr = lon_curr - (ring * 2);
			
			// iterate upwards
			for (int i = 0; i < ring * 2; ++i) {
				min_id = searchMinInCell(lat, lon, lat_curr + i, lon_curr, min_id);
			}
			lat_curr = lat_curr + (ring * 2);
			
			// iterate right
			for (int i = 0; i < ring; ++i) {
				min_id = searchMinInCell(lat, lon, lat_curr, lon_curr + i, min_id);
			}
			lon_curr = lon_curr + ring;
			
			++ring; //++yyyy;
		} while (expandAgain);
		System.out.println("Searched " + (ring-1) + " rings");
		
		return min_id;
	}
	
	
	public void drawCells()
	{
		Color[] c = {Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY};
		OverlayAggregate oa = new OverlayAggregate();
		
		/*
		int curr_c = 0;
		for (int i = 0; i < this.lat.length; ++i) {
			int lat_cell = this.lat[i] == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((this.lat[i] - this.minLat) / LAT_CELL_SIZE);
			int lon_cell = this.lon[i] == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((this.lon[i] - this.minLon) / LON_CELL_SIZE);
			GeoPosition g = new GeoPosition(this.lat[i], this.lon[i]);
			MainWindow.persistentOverlayLines.add(new OverlayLine(g, g, c[(lat_cell + lon_cell) % c.length], 2));
		}
		*/
		
		for (int base = 0; base < this.grid_offset.length-1; ++base)
		{
			for (int i = this.grid_offset[base]; i < this.grid_offset[base+1]; ++i)
			{
				int pos = this.grid[i];
				//int lat_cell = this.lat[pos] == this.maxLat ? GRID_LAT_CELLS - 1 : (int) ((this.lat[pos] - this.minLat) / LAT_CELL_SIZE);
				//int lon_cell = this.lon[pos] == this.maxLon ? GRID_LON_CELLS - 1 : (int) ((this.lon[pos] - this.minLon) / LON_CELL_SIZE);
				//System.out.println("Searching node in cell " + lat_cell + "," + lon_cell);
				GeoPosition g = new GeoPosition(this.lat[pos], this.lon[pos]);
				oa.addPoint(new OverlayElement(g, c[base % c.length], 3));
			}
		}
		MainWindow.persistentOverlayLines.add(oa);
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