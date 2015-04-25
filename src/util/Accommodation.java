package util;

import java.awt.Color;

import gui.overlay.OverlayAggregate;
import gui.overlay.OverlayElement;
import gui.overlay.OverlayLabel;

import org.jdesktop.swingx.mapviewer.GeoPosition;


/**
 * Convenience class to encapsulate relevant data for a accommodation.
 */
public class Accommodation
{
    public final static int STROKE_WIDTH = 2 * 6;
    
    private final GeoPosition gp;
    private final String name;
    private final OverlayAggregate oa;
    
    
    public Accommodation(GeoPosition gp, String name)
    {
        this.gp = gp;
        this.name = name;
        oa = new OverlayAggregate();
        oa.addPoint(new OverlayElement(gp, Color.BLUE, STROKE_WIDTH));
        oa.addLabel(new OverlayLabel(name, gp, STROKE_WIDTH - 2));
    }
    
    
    public GeoPosition getPos()
    {
        return gp;
    }
    
    
    public String getName()
    {
        return name;
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
            return gp.equals(a.getPos()) && name.equals(a.getName());
        }
        return false;
    }
}
