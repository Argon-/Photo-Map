package data_structures.graph;

import java.io.IOException;

public final class GraphFactory
{
	private GraphFactory() {};
	
	public static Graph load(String s) throws InvalidGraphFormatException, IOException
	{
		Graph g = new ArrayRepresentation();
		g.load(s);
		return g;
	}
}
