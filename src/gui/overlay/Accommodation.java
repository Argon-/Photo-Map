package gui.overlay;

import java.awt.Color;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import path.search.TravelRouteNoteData;



/**
 * See {@link #Accommodation(GeoPosition, String)}.
 */
public class Accommodation implements TravelRouteNoteData
{
    public final static int STROKE_WIDTH = 2 * 6;
    
    private final GeoPosition gp;
    private final String label;
    private final OverlayAggregate oa;
    
    
    /**
     * Convenience class to encapsulate relevant data for a accommodation.
     */
    public Accommodation(GeoPosition gp, String name)
    {
        this.gp = gp;
        this.label = name;
        oa = new OverlayAggregate();
        oa.addPoint(new OverlayElement(gp, Color.BLUE, STROKE_WIDTH));
        oa.addLabel(new OverlayLabel(name, gp, STROKE_WIDTH - 2));
    }
    
    
    public GeoPosition getPos()
    {
        return gp;
    }
    
    
    public String getLabel()
    {
        return label;
    }
    
    
    public OverlayAggregate getOverlay()
    {
        return oa;
    }


    /**
     * This assumes there's only one accommodation with name {@code name}
     * at position {@code gp}.
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof Accommodation) {
            Accommodation a = (Accommodation) obj;
            return gp.equals(a.getPos()) && label.equals(a.getLabel());
        }
        return false;
    }
}
