package fapra.graph;

import java.io.FileInputStream;



public interface Graph {
	public void readFromFile(FileInputStream f) throws InvalidGraphFormatException;
	public int[] getNeighbors(int n);
	public int size();
	public int getIthEdgeDistFor(int n, int i);
	public int getIthNeighbor(int n, int i);
	public int getDist(int from, int to);
	public void resetPred();
	public int getPred(int n);
	public void setPred(int n, int pred);
	public boolean hasPred(int n);
}
