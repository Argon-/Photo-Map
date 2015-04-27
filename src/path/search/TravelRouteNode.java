package path.search;

import org.jdesktop.swingx.mapviewer.GeoPosition;



/**
 * See {@link #TravelRouteNode(GeoPosition, TravelRouteNoteData)}.
 */
public class TravelRouteNode
{
    private final GeoPosition pos;
    private final TravelRouteNoteData data;
    
    
    /**
     * Encapsulate the position and the actual object for searching 
     * the shortest route.<br>
     * We only need to access the position frequently, the object
     * is merely carried along so one can get these objects in
     * shortest-route-order too ("one" = who ever requested the route).
     */
    public TravelRouteNode(GeoPosition pos, TravelRouteNoteData data)
    {
        this.pos = pos;
        this.data = data;
    }
    
    
    public GeoPosition getPos()
    {
        return pos;
    }
    
    
    public TravelRouteNoteData getData()
    {
        return data;
    }
}
