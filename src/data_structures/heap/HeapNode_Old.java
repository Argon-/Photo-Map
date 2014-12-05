package data_structures.heap;



public final class HeapNode_Old
{
	public static final int NODE_ID = 0;
	public static final int DIST = 1;
	public static final int SIZE = 2;
	
	
    public static int dist(int[] i)
    {
        return i[DIST];
    }
    
    
    public static int id(int[] i)
    {
        return i[NODE_ID];
    }
    
    
    public static void setDist(int[] i, int e)
    {
        i[DIST] = e;
    }

}
