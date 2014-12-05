package fapra.graph;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;



public interface Graph {
	public void save(String f) throws IOException;
	public void load(String f) throws InvalidGraphFormatException, IOException;
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
