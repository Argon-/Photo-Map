package data_structures.graph;

import java.io.IOException;

import util.StopWatch;


public class GraphMain
{

	@SuppressWarnings("unused")
    public static void main(String[] args) throws InvalidGraphFormatException, IOException
	{
	    System.in.read();
	    StopWatch sw = new StopWatch().lap();
	    ArrayRepresentation g = GraphFactory.loadArrayRepresentation("/Users/Julian/Documents/Uni/_Fapra OSM/3/file-generation/graph-de");
	    System.out.println(sw.lap().getLastInSecStr());
	    System.in.read();
	}

}
