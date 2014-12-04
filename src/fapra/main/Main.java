package fapra.main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import fapra.graph.ArrayRepresentation;
import fapra.graph.Graph;
import fapra.misc.StopWatch;
import fapra.path_calculation.Dijkstra;
import fapra.path_calculation.Dijkstra_Old;
import fapra.path_calculation.Dijkstra_PrioQ;



public class Main {
	
	public static void main(String[] args) throws Exception {
		//String f = "./150K.txt";
		//String f = "./15K.txt";
		String f = "./15000K.bin";
		Graph g = loadGraph(f);
		
		
		Dijkstra_Old d_old = new Dijkstra_Old(g);
		Dijkstra_PrioQ d_pq = new Dijkstra_PrioQ(g);
		Dijkstra d_2h = new Dijkstra(g);
		exec(d_old, d_pq, d_2h, 0, 333333);
		exec(d_old, d_pq, d_2h, 0, 666666);
		exec(d_old, d_pq, d_2h, 0, 999999);
		//exec(d_old, d_pq, d_2h, 15, 31);
		//exec(d_old, d_pq, d_2h, 15, 32);
	}
	
	
	public static Graph loadGraph(String location) throws Exception
	{
		Graph g;
		
		System.out.println("Probing header for file: " + location);
		FileInputStream fos = new FileInputStream(location);
		ObjectInputStream oos = null;
		
		try
		{
			oos = new ObjectInputStream(fos);
			System.out.println("Found serialized graph, reading...");
			StopWatch.lap();
			g = (Graph) oos.readObject();
		}
		catch (StreamCorruptedException e)
		{ 
			System.out.println("Found graph text file, parsing...");
			g = new ArrayRepresentation();
			StopWatch.lap();
			g.readFromFile(new FileInputStream(location));
		}
		finally
		{
			if (oos != null)
				oos.close();
			fos.close();
		}
				
		System.out.println(StopWatch.lapSec() + " sec");
		System.out.println("");
		return g;
	}
	
	
	public static void saveGraph(Graph g, String location) throws Exception
	{
	    FileOutputStream fos = new FileOutputStream(location);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(g);
		oos.close();
		fos.close();
	}

	
	public static void exec(Dijkstra_Old d_old, Dijkstra_PrioQ d_pq, Dijkstra d_2h, int from, int to)
	{
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

		
		System.out.println("");
		System.out.println("");
	}

}