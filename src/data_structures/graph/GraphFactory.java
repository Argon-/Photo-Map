package data_structures.graph;

import java.io.IOException;



public final class GraphFactory
{
    private GraphFactory() {};
    

    public static Graph load(String s) throws InvalidGraphFormatException, IOException
    {
        return new ArrayRepresentation(s);
    }
    
    
    public static ArrayRepresentation loadArrayRepresentation(String s) throws InvalidGraphFormatException, IOException
    {
        return new ArrayRepresentation(s);
    }

}
