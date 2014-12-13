package path.search;

import java.util.Arrays;
import java.util.LinkedList;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import data_structures.graph.Graph;
import data_structures.heap.BinaryMinHeap;



public final class Dijkstra implements Runnable
{
	private final int UNSETTLED = Integer.MAX_VALUE;
	private final int   SETTLED = Integer.MAX_VALUE - 1;
	
	private final Graph g;
	private final int[] state;
	private final int[] pred;
	
	private BinaryMinHeap heap = null;
	
	private int source = -1;
	private int target = -1;
	
	
	public Dijkstra(Graph graph)
	{
		this.g = graph;
		this.state = new int[this.g.size()];
		this.pred  = new int[this.g.size()];
		Arrays.fill(this.pred, -1);
	}
	
	
	public Dijkstra(Graph graph, int from, int to)
	{
		this(graph);
		this.setSource(from);
		this.setTarget(to);
	}
	
	
	public void buildNewHeap(int source, boolean cleanup)
	{
		Arrays.fill(this.state, UNSETTLED);
		
		if (!cleanup) {
			this.heap = new BinaryMinHeap(this.g.size() / 10);
			this.heap.insert(source, 0);
		}
		else {
			// was wird schneller durch touched?
			// oder: was ist langsam ohne touched?
			// beim cleanup müsste man den state/settled array doch mit updaten?
			this.heap.cleanup();
		}
	}
	
	
	public boolean pathFromTo(int from, int to)
	{
		this.setSource(from);
		this.setTarget(to);
		return this.pathFromTo();
	}
		
	
	public boolean pathFromTo()
	{
		if (this.state[this.target] == SETTLED) {
			System.out.println("Dist from " + this.source + " to " + this.target + " = (already settled)");
			return true;
		}
		
		loop : while (!this.heap.isEmpty()) 
		{
			int u_id = this.heap.getMinID();
			int u_dist = this.heap.getMinValue();
			this.heap.removeMin();
			
			this.state[u_id] = SETTLED;
			int i = 0;
			int neighbor = -1;
			
			if (this.target == u_id) {
				System.out.println("Dist from " + this.source + " to " + this.target + " = " + u_dist + " (1)");
				break loop;
			}
			
			while ((neighbor = this.g.getIthNeighbor(u_id, i++)) != -1)
			{

				if (this.state[neighbor] == SETTLED) {
					continue;
				}
				
				final int new_dist = u_dist + g.getIthEdgeDistFor(u_id, i-1);
				
				if (this.state[neighbor] > new_dist) {
					this.heap.insert(neighbor, new_dist);
					this.state[neighbor] = new_dist;
					this.pred[neighbor] = u_id;
				}
				
				if (this.target == neighbor) {
					System.out.println("Dist from " + this.source + " to " + this.target + " = " + this.state[neighbor]);
					this.state[neighbor] = SETTLED;
					this.pred[neighbor] = u_id;
					break loop;
				}
			} // while
		} // while
		
		if (this.state[this.target] == SETTLED)
		{
			return true;
		}
		System.out.println("Found no route from " + this.source + " to " + this.target);
		return false;
	}
	
	
	public void setSource(int from)
	{
		if (this.source != from) {
			this.source = from;
			this.buildNewHeap(from, false);
		}
	}
	
	
	public void setTarget(int to)
	{
		this.target = to;
	}
	
	
	public void printRouteStats(int from, int to)
	{
		LinkedList<Integer> l = this.getRoute_NodeIDs();
		int last = l.pop();
		int curr;
		int dist = 0;
		int hops = l.size();
		int line_count = 0;

		String s = "" + last;
		while (!l.isEmpty())
		{
			curr = l.pop();
			if (this.g.getDist(last, curr) == -1) {
				System.out.println("NO EDGE FOUND FROM " + curr + " TO " + last);
			}
			else {
				if (s.length() / 80 > line_count) {
					++line_count;
					s += System.getProperty("line.separator") + "           ";
				}
				dist += this.g.getDist(last, curr);
				s += " -> " + curr;
			}
			last = curr;
		}
		
		
		//System.out.println("----------------------------------------------------------------------");
		System.out.println("Shortest path from " + this.source + " to " + this.target);
		System.out.println("   >  Hops: " + hops);
		System.out.println("   >  Dist: " + dist);
		//System.out.println("   > Route: " + s);
		//System.out.println("----------------------------------------------------------------------");
	}
	
	
	public LinkedList<GeoPosition> getRoute()
	{
		if (this.target == -1)
			throw new RuntimeException("Can't return route without target");
		
		LinkedList<GeoPosition> l = new LinkedList<GeoPosition>();
		l.addFirst(this.g.getPosition(this.target));
		
		int i = this.pred[this.target];
		while (i != -1) {
			l.addFirst(this.g.getPosition(i));
			if (i == this.source) {
				break;
			}
			i = this.pred[i];
		}
		return l;
	}
	
	
	public LinkedList<Integer> getRoute_NodeIDs()
	{
		if (this.target == -1)
			throw new RuntimeException("Can't return route without target");
		
		LinkedList<Integer> l = new LinkedList<Integer>();
		l.addFirst(this.target);
		
		int i = this.pred[this.target];
		while (i != -1) {
			l.addFirst(i);
			if (i == this.source) {
				break;
			}
			i = this.pred[i];
		}
		return l;
	}


	@Override
	public void run()
	{
		this.pathFromTo();
	}
	
	
}
