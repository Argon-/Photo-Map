package old;

import path.search.Dijkstra;
import data_structures.graph.Graph;
import data_structures.graph.GraphFactory;
import util.StopWatch;



public class DijkstraMain {
	
	public static void main(String[] args) throws Exception {
		//String f = "./150K.txt";
		//String f = "./15K.txt";
		//String f = "/Users/Julian/Documents/Dropbox/_Semester 9/Fapra OSM/1/15000.txt";
		String f = "./15000K.bin";
		
		StopWatch sw = new StopWatch();
		sw.lap();
		Graph g = GraphFactory.load(f);
		//g.save("./15000K.bin");
		System.out.println(sw.getLastInSecStrLong() + " sec");
		System.out.println("");
		
		
		Dijkstra_Old d_old = new Dijkstra_Old(g);
		Dijkstra_PrioQ d_pq = new Dijkstra_PrioQ(g);
		Dijkstra d_2h = new Dijkstra(g, false);
		//exec(g, d_old, d_pq, d_2h, 0, 333333);
		exec(g, d_old, d_pq, d_2h, 0, 666666);
		//exec(g, d_old, d_pq, d_2h, 0, 999999);
		//exec(g, d_old, d_pq, d_2h, 15, 31);
		//exec(g, d_old, d_pq, d_2h, 15, 32);
		//exec(g, d_old, d_pq, d_2h, 1339670, 9686873);
		//exec(g, d_old, d_pq, d_2h, 1339670, 10180216);
		//exec(g, d_old, d_pq, d_2h, 0, 2109);
	}

	
	public static void exec(Graph g, Dijkstra_Old d_old, Dijkstra_PrioQ d_pq, Dijkstra d_2h, int from, int to) throws InterruptedException
	{
		System.out.println("================================================================================");
		System.out.println("Dijkstra_Old");
		System.out.println("================================================================================");
		
	    StopWatch sw = new StopWatch();
		sw.lap();
		d_old.pathFromTo(from, to);
		System.out.println(sw.getLastInSecStrLong() + " sec");
		System.out.println("");
		
		sw.lap();
		d_old.printRouteStats(from, to);
		//System.out.println(StopWatch.lapSecStr() + " sec");
		System.out.println("");

		
		System.out.println("================================================================================");
		System.out.println("Dijkstra Priority Queue");
		System.out.println("================================================================================");
		
		sw.lap();
		d_pq.pathFromTo(from, to);
		System.out.println(sw.getLastInSecStrLong() + " sec");
		System.out.println("");
		
		sw.lap();
		d_pq.printRouteStats(from, to);
		//System.out.println(StopWatch.lapSecStr() + " sec");
		System.out.println("");

		
		System.out.println("================================================================================");
		System.out.println("Dijkstra BinHeap");
		System.out.println("================================================================================");
		
		sw.lap();
		d_2h.pathFromTo(from, to);
		System.out.println(sw.getLastInSecStrLong() + " sec");
		System.out.println("");
		
		sw.lap();
		d_2h.printRouteStats(from, to);
		//System.out.println(StopWatch.lapSecStr() + " sec");
		System.out.println("");
				
		
		System.out.println("");
		System.out.println("");
	}

}