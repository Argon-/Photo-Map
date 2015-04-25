package gui.overlay;

import java.awt.Graphics2D;
import java.util.LinkedList;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;



public final class OverlayAggregate implements OverlayObject
{
	private final LinkedList<OverlayElement> points;
	private final LinkedList<OverlayElement> lines;
    private final LinkedList<OverlayLabel> labels;

	
	public OverlayAggregate()
	{
		points = new LinkedList<OverlayElement>();
		lines = new LinkedList<OverlayElement>();
	    labels = new LinkedList<OverlayLabel>();
	}
	
	
	public LinkedList<OverlayElement> getPoints()
	{
		return points;
	}
	
	
	public LinkedList<OverlayElement> getLines()
	{
		return lines;
	}
	
	
	public OverlayAggregate addPoint(OverlayElement oe)
	{
		points.add(oe);
		return this;
	}
	
	
	public OverlayAggregate addLine(OverlayElement oe)
	{
		lines.add(oe);
		return this;
	}
	
	
	public OverlayAggregate addLabel(OverlayLabel ol)
    {
        labels.add(ol);
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

}
