package data_structures.graph;

import java.io.IOException;



public interface Graph {
	public void save(String f) throws IOException;
	public void load(String f) throws InvalidGraphFormatException, IOException;
	public int[] getNeighbors(int n);
	public int size();
	public int getIthEdgeDistFor(int n, int i);
	public int getIthNeighbor(int n, int i);
	public int getDist(int from, int to);
}
