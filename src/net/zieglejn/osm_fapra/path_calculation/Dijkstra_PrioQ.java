package net.zieglejn.osm_fapra.path_calculation;

import java.util.Arrays;
import java.util.PriorityQueue;

import net.zieglejn.osm_fapra.graph.Graph;
import net.zieglejn.osm_fapra.heap.Node;
import net.zieglejn.osm_fapra.heap.NodeComparator;



public final class Dijkstra_PrioQ
{
	private final int UNSETTLED = Integer.MAX_VALUE;
	private final int   SETTLED = -1;
	
	private Graph g = null;
	private final NodeComparator comp = new NodeComparator();
	
	private int last_source = -1;
	private PriorityQueue<Node> heap = null;
	private int[] state = null;
	
	public Dijkstra_PrioQ(Graph graph)
	{
		this.g = graph;
	}
	
	
	public void buildNewHeap(int source)
	{
		this.last_source = source;
		this.heap = new PriorityQueue<Node>(this.g.size()/2, this.comp);
		this.state = new int[this.g.size()];
		Arrays.fill(this.state, UNSETTLED);
		this.g.resetPred();
	}
	
	
	public void pathFromTo(int from, int to)
	{
		if (this.last_source != from) {
			this.buildNewHeap(from);
		}
		else if (this.state[to] == SETTLED) {
			System.out.println("Dist from " + from + " to " + to + " = (already settled)");
			return;
		}
		
		this.heap.add(new Node(from, 0));
		
		loop : while (!this.heap.isEmpty()) 
		{
			Node u = this.heap.poll();

			
			this.state[u.id] = SETTLED;
			int[] neighbors = this.g.getNeighbors(u.id);
		
			for (int i = 0; i < neighbors.length; ++i) 
			{
				if (this.state[neighbors[i]] == SETTLED) {
					continue;
				}
				
				int new_dist = u.dist + g.getIthEdgeDistFor(u.id, i);
				
				if (this.state[neighbors[i]] > new_dist) {
					this.heap.add(new Node(neighbors[i], new_dist));
					this.state[neighbors[i]] = new_dist;
					this.g.setPred(neighbors[i], u.id);
				}
				
				if (to != -1 && to == neighbors[i]) {
					System.out.println("Dist from " + from + " to " + to + " = " + this.state[neighbors[i]]);
					this.state[neighbors[i]] = SETTLED;
					break loop;
				}
			} // for
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
