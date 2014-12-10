package util;

import java.util.LinkedList;

import org.jdesktop.swingx.mapviewer.GeoPosition;



public class OverlayAggregate
{
	LinkedList<OverlayElement> points = null;
	LinkedList<OverlayElement> lines = null;

	
	public OverlayAggregate()
	{
		this.points = new LinkedList<OverlayElement>();
		this.lines = new LinkedList<OverlayElement>();
	}
	
	public LinkedList<OverlayElement> getPoints()
	{
		return this.points;
	}
	
	public LinkedList<OverlayElement> getLines()
	{
		return this.lines;
	}
	
	public void addPoint(OverlayElement oe)
	{
		this.points.add(oe);
	}
	
	public void addLine(OverlayElement oe)
	{
		this.lines.add(oe);
	}

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
	 * Red source, medium<br>
	 * Green line, thin<br>
	 * Blue target, medium<br>
	 */
	public static OverlayAggregate route_var3(GeoPosition source, GeoPosition target) {
		OverlayAggregate oa = new OverlayAggregate();
		oa.addPoint(OverlayElement.pointRedMedium(source));
		oa.addPoint(OverlayElement.pointBlueMedium(target));
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
	 * Black source, medium size<br>
	 * Black line, thin<br>
	 * Black target, medium size<br>
	 */
	public static OverlayAggregate route_var5(GeoPosition source, GeoPosition target) {
		OverlayAggregate oa = new OverlayAggregate();
		oa.addPoint(OverlayElement.pointBlackMedium(source));
		oa.addPoint(OverlayElement.pointBlackMedium(target));
		oa.addLine(OverlayElement.lineBlackThin(source, target));
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
	 * Blue source, medium<br>
	 * Black line, medium<br>
	 * Blue target, medium<br>
	 */
	public static OverlayAggregate route_multi_var2(LinkedList<GeoPosition> l) {
		OverlayAggregate oa = new OverlayAggregate();
		GeoPosition last = l.removeFirst();
		oa.addPoint(OverlayElement.pointBlueMedium(last));
		for (GeoPosition p : l) {
			oa.addLine(OverlayElement.lineBlackMedium(last, p));
			last = p;
		}
		oa.addPoint(OverlayElement.pointBlueMedium(last));
		return oa;
	}

}
