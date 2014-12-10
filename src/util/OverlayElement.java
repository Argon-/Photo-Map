package util;

import java.awt.Color;

import org.jdesktop.swingx.mapviewer.GeoPosition;



public class OverlayElement
{
	private GeoPosition	source;
	private GeoPosition	target;
	private Color		color		= Color.BLACK;
	private int			strokeWidth	= 6;
	


	public OverlayElement(GeoPosition point)
	{
		this.source = point;
		this.target = point;
	}


	public OverlayElement(GeoPosition point, Color color, int strokeWidth)
	{
		this(point);
		this.color = color;
		this.strokeWidth = strokeWidth;
	}


	public OverlayElement(GeoPosition source, GeoPosition target)
	{
		this.source = source;
		this.target = target;
	}


	public OverlayElement(GeoPosition source, GeoPosition target, Color color, int strokeWidth)
	{
		this(source, target);
		this.color = color;
		this.strokeWidth = strokeWidth;
	}


	public OverlayElement(double point_lat, double point_lon)
	{
		this.source = new GeoPosition(point_lat, point_lon);
		this.target = new GeoPosition(point_lat, point_lon);
	}


	public OverlayElement(double point_lat, double point_lon, Color color, int strokeWidth)
	{
		this(point_lat, point_lon);
		this.color = color;
		this.strokeWidth = strokeWidth;
	}


	public OverlayElement(double source_lat, double source_lon, double target_lat, double target_lon)
	{
		this.source = new GeoPosition(source_lat, source_lon);
		this.target = new GeoPosition(target_lat, target_lon);
	}


	public OverlayElement(double source_lat, double source_lon, double target_lat, double target_lon, Color color,
			int strokeWidth)
	{
		this(source_lat, source_lon, target_lat, target_lon);
		this.color = color;
		this.strokeWidth = strokeWidth;
	}


	public GeoPosition getSource()
	{
		return this.source;
	}


	public GeoPosition getTarget()
	{
		return this.target;
	}


	public Color getColor()
	{
		return this.color;
	}


	public int getWidth()
	{
		return this.strokeWidth;
	}
	
	
	public static OverlayElement lineBlackThin(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.BLACK, 1);
	}
	public static OverlayElement lineBlueThin(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.BLUE, 1);
	}
	public static OverlayElement lineRedThin(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.RED, 1);
	}
	public static OverlayElement lineGreenThin(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.GREEN, 1);
	}
	public static OverlayElement lineYellowThin(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.YELLOW, 1);
	}
	
	public static OverlayElement lineBlackMedium(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.BLACK, 3);
	}
	public static OverlayElement lineBlueMedium(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.BLUE, 3);
	}
	public static OverlayElement lineRedMedium(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.RED, 3);
	}
	public static OverlayElement lineGreenMedium(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.GREEN, 3);
	}
	public static OverlayElement lineYellowMedium(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.YELLOW, 3);
	}

	public static OverlayElement lineBlackThick(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.BLACK, 5);
	}
	public static OverlayElement lineBlueThick(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.BLUE, 5);
	}
	public static OverlayElement lineRedThick(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.RED, 5);
	}
	public static OverlayElement lineGreenThick(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.GREEN, 5);
	}
	public static OverlayElement lineYellowThick(GeoPosition source, GeoPosition target)	{
		return new OverlayElement(source, target, Color.YELLOW, 5);
	}


	public static OverlayElement pointBlackSmall(GeoPosition point)	{
		return new OverlayElement(point, Color.BLACK, 3);
	}
	public static OverlayElement pointBlackMedium(GeoPosition point)	{
		return new OverlayElement(point, Color.BLACK, 6);
	}
	public static OverlayElement pointBlackBig(GeoPosition point)	{
		return new OverlayElement(point, Color.BLACK, 9);
	}
	
	public static OverlayElement pointBlueSmall(GeoPosition point)	{
		return new OverlayElement(point, Color.BLUE, 3);
	}
	public static OverlayElement pointBlueMedium(GeoPosition point)	{
		return new OverlayElement(point, Color.BLUE, 6);
	}
	public static OverlayElement pointBlueBig(GeoPosition point)	{
		return new OverlayElement(point, Color.BLUE, 9);
	}
	
	public static OverlayElement pointRedSmall(GeoPosition point)	{
		return new OverlayElement(point, Color.RED, 3);
	}
	public static OverlayElement pointRedMedium(GeoPosition point)	{
		return new OverlayElement(point, Color.RED, 6);
	}
	public static OverlayElement pointRedBig(GeoPosition point)	{
		return new OverlayElement(point, Color.RED, 9);
	}
	
	public static OverlayElement pointGreenSmall(GeoPosition point)	{
		return new OverlayElement(point, Color.GREEN, 3);
	}
	public static OverlayElement pointGreenMedium(GeoPosition point)	{
		return new OverlayElement(point, Color.GREEN, 6);
	}
	public static OverlayElement pointGreenBig(GeoPosition point)	{
		return new OverlayElement(point, Color.GREEN, 9);
	}
	
	public static OverlayElement pointYellowSmall(GeoPosition point)	{
		return new OverlayElement(point, Color.YELLOW, 3);
	}
	public static OverlayElement pointYellowMedium(GeoPosition point)	{
		return new OverlayElement(point, Color.YELLOW, 6);
	}
	public static OverlayElement pointYellowBig(GeoPosition point)	{
		return new OverlayElement(point, Color.YELLOW, 9);
	}

}
