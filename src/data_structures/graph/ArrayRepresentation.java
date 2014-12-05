package data_structures.graph;

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



final public class ArrayRepresentation implements Graph, Serializable {

	private static final long serialVersionUID = 8955873766213220121L;
	
	private double     x[] = null;
	private double     y[] = null;
	private int     pred[] = null;
	private int     elev[] = null;
	private int   source[] = null;
	private int   target[] = null;
	private int     dist[] = null;
	private int   offset[] = null;
	private int     type[] = null;
	private double types[] = null;
	
	
	public ArrayRepresentation()
	{
		this.types = new  double[] {-1.0, 1.3, 1.2, 0.8, 0.7, -1, 1.3, 0.5, 0.45, 0.3, 0.5, 0.3, 0.5};
	}
	
	
	public void save(String f) throws IOException
	{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		
		oos.writeObject(this.x);
		oos.writeObject(this.y);
		//oos.writeObject(this.pred);
		oos.writeObject(this.elev);
		oos.writeObject(this.source);
		oos.writeObject(this.target);
		oos.writeObject(this.dist);
		oos.writeObject(this.offset);
		oos.writeObject(this.type);
		oos.writeObject(this.types);
		
		oos.close();
	}
	
	
	public void load(String f) throws InvalidGraphFormatException, IOException
	{
		System.out.println("Probing file header");
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			System.out.println("Found serialized graph, reading...");
			
			this.x = (double[]) ois.readObject();
			this.y = (double[]) ois.readObject();
			//this.pred = (int[]) ois.readObject();
			this.pred = new int[this.x.length];
			this.elev = (int[]) ois.readObject();
			this.source = (int[]) ois.readObject();
			this.target = (int[]) ois.readObject();
			this.dist = (int[]) ois.readObject();
			this.offset = (int[]) ois.readObject();
			this.type = (int[]) ois.readObject();
			this.types = (double[]) ois.readObject();
			
			ois.close();
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
			
			this.x    = new double[node_num];
			this.y    = new double[node_num];
			this.elev = new int[node_num];
			this.pred = new int[node_num];
			this.resetPred();

			for (int i = 0; i < node_num; ++i)
			{
				final String[] s = b.readLine().split(" ");
				this.x[i] = Double.parseDouble(s[1]);
				this.y[i] = Double.parseDouble(s[2]);
				this.elev[i] = Integer.parseInt(s[3]);
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
		} 
		catch (NumberFormatException e) {
			e.printStackTrace();
			throw new InvalidGraphFormatException("Invalid graph text file format");
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
		if (n < 0 || n >= this.x.length) {
			throw new RuntimeException("Bad Node ID: " + n + " (offset.length = " + this.x.length + ")");
		}

		int[] r = new int[this.offset[n+1] - this.offset[n]];
		for (int i = 0; i < r.length; ++i) {
			r[i] = this.target[this.offset[n] + i];
		}
		return r;
	}
	
	
	public int getIthNeighbor(int n, int i)
	{
		if (n < 0 || n >= this.x.length) {
			throw new RuntimeException("Bad Node ID: " + n + " (offset.length = " + this.x.length + ")");
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
		if (from < 0 || from >= this.x.length) {
			throw new RuntimeException("Bad Node ID: " + from + " (offset.length = " + this.x.length + ")");
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
		
	
	public int size()
	{
		return this.x.length;
	}
	
	
	public int getOffset(int n)
	{
		return this.offset[n];
	}
	
	
	public int getIthEdgeDistFor(int n, int i)
	{
		return this.dist[this.offset[n]+i];
	}
	
	
	public void resetPred()
	{
		Arrays.fill(this.pred, -1);
	}
	
	
	public int getPred(int n)
	{
		return this.pred[n];
	}
	
	
	public void setPred(int n, int pred)
	{
		this.pred[n] = pred;
	}
	
	public boolean hasPred(int n)
	{
		return this.pred[n] != -1;
	}
}
