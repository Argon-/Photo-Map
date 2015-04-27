package data_structures.graph.generator;



/**
 * See {@link #TourismNode(double, double, int, String)}.
 */
public final class TourismNode extends Node
{
    private int tourism_type;
    private String name = null;
    
    
    /**
     * Extension of {@link Node}.<br>
     * Used for tourism nodes, this class provides additionally
     * a tourism type and name for the respective node.
     * 
     * @param lat
     * @param lon
     * @param tourism_type
     * @param name
     */
    public TourismNode(double lat, double lon, int tourism_type, String name)
    {
        super(lat, lon);
        this.tourism_type = tourism_type;
        this.name = name;
    }
    
    
    public void setTourismInfo(int tourism_type, String name)
    {
        this.tourism_type = tourism_type;
        this.name = name;
    }
    

    public String toString()
    {
        String s = super.toString() + "\n";
        s += "   t_type : " + tourism_type + "\n";
        s += "   name   : " + name;
        return s;
    }
    
    
    public int getTourismType() { return tourism_type; }
    public String getName()     { return name; }
    public boolean hasName()    { return name != null; }

 }
