package gui.overlay;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;



/**
 * See {@link #OverlayLabel(String, GeoPosition, int)}.
 */
public final class OverlayLabel implements OverlayObject
{
    // pixel constants for alignment and padding in draw()
    public static final int LABEL_X_PADDING = 5;
    public static final int LABEL_Y_PADDING = 2;
    public final int Y_OFFSET;

    private int cachedFontHeight = 0;
    private int cachedFontWidth  = 0;
    private int cachedFontAscent = 0;
    private int cachedZoom       = 1;
    
    private Color background = new Color(0, 0, 0, 150);
    private Color foreground = Color.WHITE;
    
    String label;
    GeoPosition pos;
    
    
    /**
     * Represents a text label drawable onto the map.<br>
     * A label consists of the text to draw, the position where to draw at
     * and an offset below the text.<br>
     * A box with transparent dark background is drawn behind the text to 
     * increase readability.
     * 
     * @param text label
     * @param g position
     * @param offset in pixel
     */
    public OverlayLabel(String text, GeoPosition g, int offset)
    {
        label = text;
        pos = g;
        Y_OFFSET = offset;
        
        if (label == null)
            label = "(no name)";
    }


    /**
     * Using {@code offset = 7}.
     * See {@link #OverlayLabel(String, GeoPosition, int)}.
     */
    public OverlayLabel(String text, GeoPosition g)
    {
        this(text, g, 7);
    }
        
    
    /**
     * Text to draw.
     */
    public OverlayLabel setLabel(String l)
    {
        label = l;
        if (label == null)
            label = "(no name)";
        return this;
    }

    
    /**
     * Where to draw the text at.
     */
    public OverlayLabel setGeoPos(GeoPosition g)
    {
        pos = g;
        return this;
    }
    
    
    /**
     * Text color.
     */
    public OverlayLabel setForeground(Color c)
    {
        foreground = c;
        return this;
    }
    
    
    /**
     * Color of the box drawn behind the text.
     */
    public OverlayLabel setBackground(Color c)
    {
        background = c;
        return this;
    }

    
    public void draw(Graphics2D g, JXMapViewer map)
    {
        if (cachedFontHeight == 0 || cachedFontWidth == 0 || cachedFontAscent == 0) {
            FontMetrics fm = g.getFontMetrics();
            cachedFontWidth = fm.stringWidth(label);
            cachedFontHeight = fm.getHeight();
            cachedFontAscent = fm.getAscent();
        }
        
        if (map.getZoom() != cachedZoom) {
            cachedZoom = map.getZoom();
        }

        Point2D p = map.getTileFactory().geoToPixel(pos, cachedZoom);
        int box_x = (int) (p.getX() - (cachedFontWidth / 2));
        int box_y = (int) (p.getY() - (cachedFontHeight + Y_OFFSET));

        g.setPaint(background);
        g.fillRoundRect(box_x - LABEL_X_PADDING, box_y - LABEL_Y_PADDING, cachedFontWidth + LABEL_X_PADDING * 2, cachedFontHeight + LABEL_Y_PADDING * 2, 10, 10);
        g.setPaint(foreground);
        g.drawString(label, box_x, box_y + cachedFontAscent);
    }
    
    
    public GeoPosition getPosition()
    {
        return pos;
    }
    
    
    public String toString() 
    {
        return label != null ? label : "(null)";
    }

}
