package fapra.path_calculation;

import java.util.Arrays;

import fapra.graph.Graph;
import fapra.heap.BinaryMinHeap;



public final class Dijkstra
{
	private final int UNSETTLED = Integer.MAX_VALUE;
	private final int   SETTLED = Integer.MAX_VALUE-1;
	
	private Graph g = null;
	
	private int last_source = -1;
	private BinaryMinHeap heap = null;
	private int[] state = null;
	
	public Dijkstra(Graph graph)
	{
		this.g = graph;
		this.state = new int[this.g.size()];
	}
	
	
	public void buildNewHeap(int source, boolean cleanup)
	{
		// was wird schneller durch touched?
		// oder: was ist langsam ohne touched?
		// beim cleanup müsste man den state/settled array doch mit updaten?
		this.last_source = source;
		Arrays.fill(this.state, UNSETTLED);
		this.g.resetPred();
		
		if (!cleanup) {
			this.heap = new BinaryMinHeap(this.g.size() / 10);
		}
		else {
			this.heap.cleanup();
		}
	}
	
	
	public void pathFromTo_old(int from, int to)
	{
		if (this.last_source != from) {
			this.buildNewHeap(from, false);
		}
		else if (this.state[to] == SETTLED) {
			System.out.println("Dist from " + from + " to " + to + " = (already settled)");
			return;
		}
				
		this.heap.insert(from, 0);
		loop : while (!this.heap.isEmpty()) 
		{
			int u_id = this.heap.getMinID();
			int u_dist = this.heap.getMinValue();
			this.heap.removeMin();

			
			this.state[u_id] = SETTLED;
			int[] neighbors = this.g.getNeighbors(u_id);
		
			for (int i = 0; i < neighbors.length; ++i) 
			{
				if (this.state[neighbors[i]] == SETTLED) {
					continue;
				}
				
				int new_dist = u_dist + g.getIthEdgeDistFor(u_id, i);
				
				if (this.state[neighbors[i]] > new_dist) {
					this.heap.insert(neighbors[i], new_dist);
					this.state[neighbors[i]] = new_dist;
					this.g.setPred(neighbors[i], u_id);
				}
				
				if (to != -1 && to == neighbors[i]) {
					System.out.println("Dist from " + from + " to " + to + " = " + this.state[neighbors[i]]);
					this.state[neighbors[i]] = SETTLED;
					this.g.setPred(neighbors[i], u_id);
					break loop;
				}
			} // for
		} // while
	}
	
	
	public void pathFromTo(int from, int to)
	{
		if (this.last_source != from) {
			this.buildNewHeap(from, false);
		}
		else if (this.state[to] == SETTLED) {
			System.out.println("Dist from " + from + " to " + to + " = (already settled)");
			return;
		}
				
		this.heap.insert(from, 0);
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
					this.g.setPred(neighbor, u_id);
				}
				
				if (to != -1 && to == neighbor) {
					System.out.println("Dist from " + from + " to " + to + " = " + this.state[neighbor]);
					this.state[neighbor] = SETTLED;
					this.g.setPred(neighbor, u_id);
					break loop;
				}
			} // while
		} // while
	}
	
	
	public void printRouteStats(int from, int to)
	{
		StringBuilder sb = new StringBuilder("" + to);
		int l_n = to;
		int n = this.g.getPred(l_n);
		int hops = 0;
		int ad = 0;
		int lnc = 0;
		
		while (n != -1) {
			if (this.g.getDist(n, l_n) == -1) {
				System.out.println("NO EDGE FOUND FROM " + l_n + " TO " + n);
			}
			
			if (lnc < (sb.length()+12) / 80) {
				++lnc;
				sb.insert(0, System.getProperty("line.separator") + "            ");
			}
			sb.insert(0, n + " -> ");
			++hops;
			ad += this.g.getDist(n, l_n);
			
			if (n == from) {
				break;
			}

			l_n = n;
			n = this.g.getPred(n);
		}
		
		//System.out.println("----------------------------------------------------------------------");
		System.out.println("Shortest path from " + from + " to " + to);
		System.out.println("   >  Hops: " + hops);
		System.out.println("   >  Dist: " + ad);
		//System.out.println("   > Route: " + sb.toString());
		//System.out.println("----------------------------------------------------------------------");
	}
	
	
}
