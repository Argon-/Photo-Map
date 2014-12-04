package net.zieglejn.osm_fapra.path_calculation;

import java.util.Arrays;

import net.zieglejn.osm_fapra.graph.Graph;
import net.zieglejn.osm_fapra.heap.BinaryMinHeap_Old;
import net.zieglejn.osm_fapra.heap.HeapNode_Old;



public final class Dijkstra_Old
{
	Graph g = null;
	BinaryMinHeap_Old heap = null;
	int last_source = -1;
	
	
	public Dijkstra_Old(Graph graph)
	{
		this.g = graph;
	}
	
	
	public void buildHeap(int source)
	{
		int[] init = new int[g.size()];
		Arrays.fill(init, Integer.MAX_VALUE);
		init[source] = 0;
		this.last_source = source;
		this.heap = new BinaryMinHeap_Old(init);
		init = null;
	}
	
	
	public void pathFromToCached(int from, int to)
	{
		System.out.println("Dijkstra from " + from + " to " + to);
		if (from == to) {
			return;
		}
		
		if (this.heap == null || this.last_source != from) {
			this.buildHeap(from);
		}
				

		loop : while (!this.heap.isEmpty()) {
			int[] u = this.heap.extractMin();
			
			if (u[HeapNode_Old.DIST] == Integer.MAX_VALUE) {
				//continue;	// node is not reachable from `from`
				return;
			}
			
			int[] neighbors = this.g.getNeighbors(u[HeapNode_Old.NODE_ID]);
			
			for (int i = 0; i < neighbors.length; ++i) {
				
				int new_dist = u[HeapNode_Old.DIST] + g.getIthEdgeDistFor(u[HeapNode_Old.NODE_ID], i);
				
				if (this.heap.getNodeDist(neighbors[i]) > new_dist) {
					this.heap.decreaseKeyByNodeID(neighbors[i], new_dist);	// fails silently if node not in heap
				}
				
				if (to != -1 && to == neighbors[i]) {
					System.out.println("Dist from " + from + " to " + to + " = " + this.heap.getNodeDist(neighbors[i]));
					break loop;
				}
			}
		}
	}
		
	
	public void pathFromTo(int from, int to)
	{
		this.last_source = -1;
		this.heap = new BinaryMinHeap_Old(this.g.size());
		boolean[] known = new boolean[this.g.size()];
		
		this.g.resetPred();
		this.heap.insert(new int[] {from, 0});
		
		
		loop : while (!this.heap.isEmpty()) 
		{
			int[] u = this.heap.extractMin();

			/*if (u[HeapNode.DIST] == Integer.MAX_VALUE) {
				System.out.println("TRÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖT 1");
				return;
			}
			
			if (known[u[HeapNode.NODE_ID]]) {
				System.out.println("TRÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖT 2");
				//continue;
			}*/
			
			known[u[HeapNode_Old.NODE_ID]] = true;
			int[] neighbors = this.g.getNeighbors(u[HeapNode_Old.NODE_ID]);
		
			for (int i = 0; i < neighbors.length; ++i) 
			{
				if (known[neighbors[i]]) {
					continue;
				}
				
				int new_dist = u[HeapNode_Old.DIST] + g.getIthEdgeDistFor(u[HeapNode_Old.NODE_ID], i);
				
				if (!this.heap.contains(neighbors[i])) {
					this.heap.insert(new int[] {neighbors[i], new_dist});
					this.g.setPred(neighbors[i], u[HeapNode_Old.NODE_ID]);
					//System.out.println("Setting predecessor for " + neighbors[i] + " to " + u[HeapNode.NODE_ID] + " (insert)");
				}
				else if (this.heap.getNodeDist(neighbors[i]) > new_dist) {
					this.heap.decreaseKeyByNodeID(neighbors[i], new_dist);
					this.g.setPred(neighbors[i], u[HeapNode_Old.NODE_ID]);
					//System.out.println("Setting predecessor for " + neighbors[i] + " to " + u[HeapNode.NODE_ID]);
				}
				
				if (to != -1 && to == neighbors[i]) {
					System.out.println("Dist from " + from + " to " + to + " = " + this.heap.getNodeDist(neighbors[i]));
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
