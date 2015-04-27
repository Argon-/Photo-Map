package path.search;

import org.jdesktop.swingx.mapviewer.GeoPosition;



public class TravelRouteNode
{
    private final GeoPosition pos;
    private final TravelRouteNoteData data;
    
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
