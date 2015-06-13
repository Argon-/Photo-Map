package gui.overlay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;



/**
 * See {@link #OverlayAggregate()}.
 * <br><br>
 * To make usage more convenient several static methods exist to construct
 * OverlayAggregate objects from positions or lists of positions with different
 * visual formatting.
 */
public final class OverlayAggregate implements OverlayObject
{
    private final LinkedList<OverlayElement> points;
    private final LinkedList<OverlayElement> lines;
    private final LinkedList<OverlayLabel> labels;

    
    /**
     * An OverlayAggregate aggregates multiple {@link OverlayElement} and {@link OverlayLabel} objects
     * to create more complex compound visuals and also allows for grouping.
     * <br>
     * Internally they are organized in {@code points}, {@code lines} and {@code labels}.
     */
    public OverlayAggregate()
    {
        points = new LinkedList<OverlayElement>();
        lines = new LinkedList<OverlayElement>();
        labels = new LinkedList<OverlayLabel>();
    }
    

    /**
     * @return get the internal list of OverlayElements regarded as points
     */
    public LinkedList<OverlayElement> getPoints()
    {
        return points;
    }
    
    
    /**
     * @return get the internal list of OverlayElements regarded as lines
     */
    public LinkedList<OverlayElement> getLines()
    {
        return lines;
    }
    
    
    /**
     * @return get the internal list of OverlayLabels
     */
    public LinkedList<OverlayLabel> getLabels()
    {
        return labels;
    }
    
    
    /**
     * Add OverlayElement as point.
     */
    public OverlayAggregate addPoint(OverlayElement oe)
    {
        points.add(oe);
        return this;
    }
    
    
    /**
     * Add OverlayElement as line.
     */
    public OverlayAggregate addLine(OverlayElement oe)
    {
        lines.add(oe);
        return this;
    }
    
    
    /**
     * Add OverlayLabel as label.
     */
    public OverlayAggregate addLabel(OverlayLabel ol)
    {
        labels.add(ol);
        return this;
    }
    
    
    /**
     * Add all elements and labels of the given OverlayAggregate
     * to this object.
     */
    public OverlayAggregate add(OverlayAggregate oa)
    {
        for (OverlayElement oe : oa.getPoints()) {
            points.add(oe);
        }
        for (OverlayElement oe : oa.getLines()) {
            lines.add(oe);
        }
        for (OverlayLabel ol : oa.getLabels()) {
            labels.add(ol);
        }
        return this;
    }
    
    
    public void draw(Graphics2D g, JXMapViewer map)
    {
        for (OverlayObject oo : lines)  { oo.draw(g, map); }
        for (OverlayObject oo : points) { oo.draw(g, map); }
        for (OverlayObject oo : labels) { oo.draw(g, map); }
    }
    
    
    /* ***********************************************************************
     * Factories
     ************************************************************************/

    public static OverlayAggregate point(OverlayElement oe) {
        OverlayAggregate oa = new OverlayAggregate();
        oa.addPoint(oe);
        return oa;
    }
    
    public static OverlayAggregate line(OverlayElement oe) {
        OverlayAggregate oa = new OverlayAggregate();
        oa.addLine(oe);
        return oa;
    }
    
    /**
     * Red source, medium<br>
     * Blue target, medium<br>
     */
    public static OverlayAggregate route_var1(GeoPosition source, GeoPosition target) {
        OverlayAggregate oa = new OverlayAggregate();
        oa.addPoint(OverlayElement.pointRedMedium(source));
        oa.addPoint(OverlayElement.pointBlueMedium(target));
        return oa;
    }
    
    /**
     * Black source, medium<br>
     * Black target, medium<br>
     */
    public static OverlayAggregate route_var2(GeoPosition source, GeoPosition target) {
        OverlayAggregate oa = new OverlayAggregate();
        oa.addPoint(OverlayElement.pointBlackMedium(source));
        oa.addPoint(OverlayElement.pointBlackMedium(target));
        return oa;
    }
    
    /**
     * Yellow source, medium<br>
     * Green line, thin<br>
     * Red target, medium<br>
     */
    public static OverlayAggregate route_var3(GeoPosition source, GeoPosition target) {
        OverlayAggregate oa = new OverlayAggregate();
        oa.addPoint(OverlayElement.pointYellowMedium(source));
        oa.addPoint(OverlayElement.pointRedMedium(target));
        oa.addLine(OverlayElement.lineGreenThin(source, target));
        return oa;
    }
    
    /**
     * Red source, medium<br>
     * Black line, thin<br>
     * Blue target, medium<br>
     */
    public static OverlayAggregate route_var4(GeoPosition source, GeoPosition target) {
        OverlayAggregate oa = new OverlayAggregate();
        oa.addPoint(OverlayElement.pointRedMedium(source));
        oa.addPoint(OverlayElement.pointBlueMedium(target));
        oa.addLine(OverlayElement.lineBlackThin(source, target));
        return oa;
    }

    
    /**
     * Black source, medium<br>
     * Black line, thin<br>
     * Black target, medium<br>
     */
    public static OverlayAggregate route_var5(GeoPosition source, GeoPosition target) {
        OverlayAggregate oa = new OverlayAggregate();
        oa.addPoint(OverlayElement.pointBlackMedium(source));
        oa.addPoint(OverlayElement.pointBlackMedium(target));
        oa.addLine(OverlayElement.lineBlackThin(source, target));
        return oa;
    }
    
    
    /**
     * Yellow source, medium<br>
     * Green line, thin<br>
     * Red target, big<br>
     */
    public static OverlayAggregate route_var6(GeoPosition source, GeoPosition target) {
        OverlayAggregate oa = new OverlayAggregate();
        oa.addPoint(OverlayElement.pointYellowMedium(source));
        oa.addPoint(OverlayElement.pointRedBig(target));
        oa.addLine(OverlayElement.lineGreenThin(source, target));
        return oa;
    }

    
    /**
     * Black source, medium<br>
     * Black line, medium<br>
     * Black target, medium<br>
     */
    public static OverlayAggregate route_multi_var1(LinkedList<GeoPosition> l) {
        OverlayAggregate oa = new OverlayAggregate();
        GeoPosition last = l.removeFirst();
        oa.addPoint(OverlayElement.pointBlackMedium(last));
        for (GeoPosition p : l) {
            oa.addLine(OverlayElement.lineBlackMedium(last, p));
            last = p;
        }
        oa.addPoint(OverlayElement.pointBlackMedium(last));
        return oa;
    }
    
    /**
     * Red source, medium<br>
     * Black line, medium<br>
     * Blue target, medium<br>
     */
    public static OverlayAggregate route_multi_var2(LinkedList<GeoPosition> l) {
        OverlayAggregate oa = new OverlayAggregate();
        GeoPosition last = l.removeFirst();
        oa.addPoint(OverlayElement.pointRedMedium(last));
        for (GeoPosition p : l) {
            oa.addLine(OverlayElement.lineBlackMedium(last, p));
            last = p;
        }
        oa.addPoint(OverlayElement.pointBlueMedium(last));
        return oa;
    }
    
    
    /**
     * Red source, big<br>
     * Black line, medium<br>
     * Blue target, big<br>
     */
    public static OverlayAggregate route_multi_var3(LinkedList<GeoPosition> l) {
        OverlayAggregate oa = new OverlayAggregate();
        GeoPosition last = l.removeFirst();
        oa.addPoint(OverlayElement.pointRedBig(last));
        for (GeoPosition p : l) {
            oa.addLine(OverlayElement.lineBlackMedium(last, p));
            last = p;
        }
        oa.addPoint(OverlayElement.pointBlueBig(last));
        return oa;
    }
    
    
    /**
     * Red source, big<br>
     * Black line, medium<br>
     * Blue target, big<br>
     */
    public static OverlayAggregate route_multi_multi_var1(LinkedList<LinkedList<GeoPosition> > list) {
        // vary color and width
        final Color[] c = new Color[] { //new Color(0, 0, 0), 
                                        //new Color(64, 64, 64),
                                        //Color.GRAY,
                                        new Color(0, 255, 0, 128),           // green
                                        new Color(255, 0, 255, 128),         // magenta
                                        //new Color(192, 192, 192),
                                      };
        final int[] w = new int[] { 6,
                                    //5,
                                    3,
                                  };
        
        OverlayAggregate oa = new OverlayAggregate();
        GeoPosition last = list.getFirst().removeFirst();
        oa.addPoint(OverlayElement.pointRedBig(last));
        
        int i = 0;
        for (LinkedList<GeoPosition> l : list)
        {
            for (GeoPosition p : l)
            {
                oa.addLine(new OverlayElement(last, p, c[i % c.length], w[i % w.length]));
                last = p;
            }
            ++i;
        }
        return oa;
    }

}
