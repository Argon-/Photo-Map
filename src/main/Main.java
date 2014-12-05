package main;

import data_structures.graph.Graph;
import data_structures.graph.GraphFactory;
import path.search.Dijkstra;
import path.search.Dijkstra_Old;
import path.search.Dijkstra_PrioQ;
import util.StopWatch;



public class Main {
	
	public static void main(String[] args) throws Exception {
		//String f = "./150K.txt";
		//String f = "./15K.txt";
		String f = "./15000K.bin";

		StopWatch.lap();
		Graph g = GraphFactory.load(f);
		System.out.println(StopWatch.lapSec() + " sec");
		System.out.println("");
		
		
		Dijkstra_Old d_old = new Dijkstra_Old(g);
		Dijkstra_PrioQ d_pq = new Dijkstra_PrioQ(g);
		Dijkstra d_2h = new Dijkstra(g);
		exec(g, d_old, d_pq, d_2h, 0, 333333);
		exec(g, d_old, d_pq, d_2h, 0, 666666);
		exec(g, d_old, d_pq, d_2h, 0, 999999);
		//exec(g, d_old, d_pq, d_2h, 15, 31);
		//exec(g, d_old, d_pq, d_2h, 15, 32);
	}

	
	public static void exec(Graph g, Dijkstra_Old d_old, Dijkstra_PrioQ d_pq, Dijkstra d_2h, int from, int to) throws InterruptedException
	{
		System.out.println("================================================================================");
		System.out.println("Dijkstra BinHeap");
		System.out.println("================================================================================");
		
		StopWatch.lap();
		d_2h.pathFromTo(from, to);
		System.out.println(StopWatch.lapSecStr() + " sec");
		System.out.println("");
		
		StopWatch.lap();
		d_2h.printRouteStats(from, to);
		//System.out.println(StopWatch.lapSecStr() + " sec");
		System.out.println("");

		
		g.resetPred();
		
		System.out.println("================================================================================");
		System.out.println("Dijkstra_Old");
		System.out.println("================================================================================");
		
		StopWatch.lap();
		d_old.pathFromTo(from, to);
		System.out.println(StopWatch.lapSecStr() + " sec");
		System.out.println("");
		
		StopWatch.lap();
		d_old.printRouteStats(from, to);
		//System.out.println(StopWatch.lapSecStr() + " sec");
		System.out.println("");
		
		
		System.out.println("================================================================================");
		System.out.println("Dijkstra Priority Queue");
		System.out.println("================================================================================");
		
		StopWatch.lap();
		d_pq.pathFromTo(from, to);
		System.out.println(StopWatch.lapSecStr() + " sec");
		System.out.println("");
		
		StopWatch.lap();
		d_pq.printRouteStats(from, to);
		//System.out.println(StopWatch.lapSecStr() + " sec");
		System.out.println("");
		
		
		System.out.println("");
		System.out.println("");
	}

}