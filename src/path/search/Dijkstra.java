package path.search;

import java.util.Arrays;
import java.util.LinkedList;

import data_structures.graph.Graph;
import data_structures.heap.BinaryMinHeap;



public final class Dijkstra implements Runnable
{
	private final int UNSETTLED = Integer.MAX_VALUE;
	private final int   SETTLED = Integer.MAX_VALUE - 1;
	
	private Graph g = null;
	
	private BinaryMinHeap heap = null;
	private int[] state = null;
	private int[] pred  = null;
	
	private int source = 0;
	private int last_source = -1;
	private int target = -1;		// magic number: explore whole graph
	
	
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
		this.last_source = source;
		Arrays.fill(this.state, UNSETTLED);
		//Arrays.fill(this.pred, -1);
		
		if (!cleanup) {
			this.heap = new BinaryMinHeap(this.g.size() / 10);
		}
		else {
			// was wird schneller durch touched?
			// oder: was ist langsam ohne touched?
			// beim cleanup müsste man den state/settled array doch mit updaten?
			this.heap.cleanup();
		}
	}
	
	
	public void pathFromTo(int from, int to)
	{
		this.setSource(from);
		this.setTarget(to);
		this.pathFromTo();
	}
		
	
	public void pathFromTo()
	{
		if (this.state[this.target] == SETTLED) {
			System.out.println("Dist from " + this.source + " to " + this.target + " = (already settled)");
			return;
		}
				
		this.heap.insert(this.source, 0);
		loop : while (!this.heap.isEmpty()) 
		{
			int u_id = this.heap.getMinID();
			int u_dist = this.heap.getMinValue();
			this.heap.removeMin();
			
			this.state[u_id] = SETTLED;
			int i = 0;
			int neighbor = -1;
			
			while ((neighbor = this.g.getIthNeighbor(u_id, i++)) != -1)
			{
				if (this.state[neighbor] == SETTLED) {
					continue;
				}
				
				int new_dist = u_dist + g.getIthEdgeDistFor(u_id, i-1);
				
				if (this.state[neighbor] > new_dist) {
					this.heap.insert(neighbor, new_dist);
					this.state[neighbor] = new_dist;
					this.pred[neighbor] = u_id;
				}
				
				if (this.target != -1 && this.target == neighbor) {
					System.out.println("Dist from " + this.source + " to " + this.target + " = " + this.state[neighbor]);
					this.state[neighbor] = SETTLED;
					this.pred[neighbor] = u_id;
					break loop;
				}
			} // while
		} // while
	}
	
	
	public void setSource(int from)
	{
		if (this.last_source != from) {
			this.buildNewHeap(from, false);
		}
		this.last_source = this.source;
		this.source = from;
	}
	
	
	public void setTarget(int to)
	{
		this.target = to;
	}
	
	
	public void printRouteStats(int from, int to)
	{
		LinkedList<Integer> l = this.getRoute();
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
	
	
	public LinkedList<Integer> getRoute()
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
