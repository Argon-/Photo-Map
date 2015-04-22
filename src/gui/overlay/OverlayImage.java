package gui.overlay;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;

import util.ImageUtil;
import util.StringUtil;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;


/**
 * This class encapsulates an images which is intended as overlay for
 * the JXMapViewer map.<br>
 * <br>
 * <strong>Note</strong>: for performance reasons (indirect) resizing operations
 * are usually deferred as long as possible. This would leads to several getters
 * returning potentially outdated imformation because the actual requested 
 * resize did not yet happen.
 * Therefore various variables and methods are used to cache or
 * pre-calculate values. For this reason methods like {@code getHeight} are
 * not just returning member variables, but actually involve calculations.
 * <br><br>
 * A resize is only guaranteed to happen when an image is explicitly requested
 * by the user ({@code isVisible(true)} followed by a call to {@code draw()}).
 */
public final class OverlayImage implements OverlayObject
{
    public static final int IMAGE_MIN_HEIGHT = 50;
    public static final int IMAGE_MIN_WIDTH  = 50;
    
    // positioning constants for fixed position images
    public static final int TOP_RIGHT = 0;
    public static final int TOP_LEFT  = 1;
    public static final int BOT_RIGHT = 2;
    public static final int BOT_LEFT  = 3;
    
    // pixel constants for alignment and padding in draw()
    public static final int WAYPOINT_Y_OFFSET = 35;         // waypoint height
    public static final int PADDING           = 3;          // padding between waypoint and label/image
    public static final int LABEL_X_PADDING   = 5;
    public static final int LABEL_Y_PADDING   = 2;

    private int cachedFontHeight = 0;
    private int cachedFontWidth  = 0;
    private int cachedFontAscent = 0;
    private double cachedMapZoomFactor = 1;

    // metadata
    private GeoPosition     mapPos        = null;
    private Date            date          = null;
    
    private final BufferedImage img;
    private BufferedImage   cachedImg;
    private boolean         highQuality   = true;
    private String          label         = null;
    private boolean         displayLabel  = true;
    private boolean         visible       = false;
    private boolean         dynamicResize = true;
    private boolean         fixedPosition = false;
    private boolean         forceResize   = false;
    private int             positionHint  = TOP_LEFT;
    private int             mapZoom       = 1;

    private int             targetWidth;
    private int             targetHeight;
    

    public OverlayImage(String f, boolean strict) throws IOException
    {
        File file = new File(f);
        img = ImageIO.read(file);
        if (img == null)
            throw new IOException("not a valid image");
        label = StringUtil.basename(f);
        targetWidth = img.getWidth();
        targetHeight = img.getHeight();
        
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            
            GpsDirectory gpsDir = metadata.getDirectory(GpsDirectory.class);
            GeoLocation loc = gpsDir.getGeoLocation();
            mapPos = new GeoPosition(loc.getLatitude(), loc.getLongitude());
            
            ExifSubIFDDirectory subDir = metadata.getDirectory(ExifSubIFDDirectory.class);
            date = subDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        }
        catch (ImageProcessingException e) {
        }
        catch (NullPointerException e) {
            // no or not enough metadata
        }
        
        if (strict && mapPos == null) {
            throw new RuntimeException("image contains no valid geo location (strict checking)");
        }
        
        if (date == null) {
            date = new Date(0);
        }
        
        forceResize = true;
    }
        
    
    public int getHeight()
    {
        int r = 0;
        if (dynamicResize)
            r = dynamicHeight();
        else
            r = targetHeight;
        return r + WAYPOINT_Y_OFFSET + PADDING * 2 + LABEL_Y_PADDING + cachedFontHeight;
    }
    
    
    public int getWidth()
    {
        return dynamicResize ? Math.max(dynamicWidth(), cachedFontWidth) : targetWidth;
    }
    
    
    public OverlayImage(String f) throws IOException
    {
        this(f, true);
    }
    
    
    public boolean isVisible()
    {
        return visible;
    }
    
    
    public Waypoint getWaypoint()
    {
        if (mapPos != null)
            return new Waypoint(mapPos);
        return null;
    }
    
    
    public Date getDate()
    {
        return date;
    }
    
    
    public String getLabel()
    {
        return label;
    }
    
    
    public OverlayImage setVisible(boolean v)
    {
        visible = v;
        return this;
    }
    
    
    public OverlayImage setHighQuality(boolean q)
    {
        highQuality = q;
        forceResize = true;
        return this;
    }
    
    
    public OverlayImage displayLabel(boolean sl)
    {
        displayLabel = sl;
        return this;
    }
    
    
    public OverlayImage setLabel(String l)
    {
        label = l;
        return this;
    }

    
    public OverlayImage resize(int w, int h)
    {
        targetWidth = w;
        targetHeight = h;
        resizeInternal(w, h);
        return this;
    }
    
    
    private int dynamicHeight()
    {
        return (int) ((targetHeight / cachedMapZoomFactor) - (targetHeight * 0.01 * (mapZoom-1)));
    }
    
    
    private int dynamicWidth()
    {
        return (int) ((targetWidth / cachedMapZoomFactor) - (targetWidth * 0.01 * (mapZoom-1)));
    }
    
    
    private boolean resizeInternal(int width, int height)
    {
        
        int w = dynamicResize ? dynamicWidth() : width;
        int h = dynamicResize ? dynamicHeight() : height;

        if (w < IMAGE_MIN_WIDTH || h < IMAGE_MIN_HEIGHT) {
            if (img.getWidth() < img.getHeight()) {
                w = (int) (img.getWidth() * IMAGE_MIN_WIDTH / img.getHeight());
                h = IMAGE_MIN_HEIGHT;
            }
            else {
                w = IMAGE_MIN_WIDTH;
                h = (int) (img.getHeight() * IMAGE_MIN_HEIGHT / img.getWidth());
            }
        }

        if (forceResize || ((w > -1 && w != cachedImg.getWidth()) || (h > -1 && h != cachedImg.getHeight()))) {
            cachedImg = ImageUtil.getScaledInstance(img, w, h, highQuality);
            forceResize = false;
        }
        return true;
    }

    
    public OverlayImage maxSize(int m, boolean lazy)
    {
        // special case: original size
        if (m < 0) {
            if (lazy) {
                targetWidth = img.getWidth();
                targetHeight = img.getHeight();
                forceResize = true;
            }
            else {
                resize(img.getWidth(), img.getHeight());
            }
            return this;
        }
        
        float fW = (float) m / img.getWidth();
        float fH = (float) m / img.getHeight();
        if (img.getWidth() < img.getHeight())
            fW = fH;

        if (lazy) {
            targetWidth = (int) (img.getWidth() * fW);
            targetHeight = (int) (img.getHeight() * fW);
            forceResize = true;
        }
        else {
            resize((int) (img.getWidth() * fW), (int) (img.getHeight() * fW));
        }

        return this;
    }
    
    
    public OverlayImage maxSize(int m)
    {
        return maxSize(m, false);
    }
    
    
    public OverlayImage dynamicResize(boolean dr)
    {
        dynamicResize = dr;
        forceResize = true;
        return this;
    }


    public OverlayImage useFixedPos(boolean useFixedPos)
    {
        fixedPosition = useFixedPos;
        return this;
    }
    
    
    public boolean isFixedPosition()
    {
        return fixedPosition;
    }


    public OverlayImage setFixedPos(int positionHint)
    {
        this.positionHint = positionHint;
        return this;
    }


    public OverlayImage setGeoPos(GeoPosition g)
    {
        mapPos = g;
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
        
        if (map.getZoom() != mapZoom) {
            mapZoom = map.getZoom();
            cachedMapZoomFactor = (mapZoom) / (Math.log(mapZoom) + 1);
            forceResize = true;
        }

        if (!visible) {
            return;
        }
        
        if (forceResize) {
            resizeInternal(targetWidth, targetHeight);
        }

        
        int x = 0, y = 0;
        
        if (fixedPosition)
        {
            Rectangle rect = map.getViewportBounds();
            switch (positionHint) {
            case TOP_LEFT:
                x = rect.x;
                y = rect.y;
                break;
            case TOP_RIGHT:
                x = rect.width - cachedImg.getWidth() + rect.x;
                y = rect.y;
                break;
            case BOT_LEFT:
                x = rect.x;
                y = rect.height - cachedImg.getHeight() + rect.y;
                break;
            case BOT_RIGHT:
                x = rect.width - cachedImg.getWidth() + rect.x;
                y = rect.height - cachedImg.getHeight() + rect.y;
                break;
            }
        }
        else if (mapPos != null)
        {
            Point2D p = map.getTileFactory().geoToPixel(mapPos, mapZoom);
            // reside centered above the location
            x = (int) p.getX() - (cachedImg.getWidth() / 2);
            y = (int) p.getY() - (cachedImg.getHeight() + WAYPOINT_Y_OFFSET + PADDING);
            
            if (displayLabel && label != null) {
                g.setPaint(new Color(0, 0, 0, 150));
                
                int box_x = (int) (p.getX() - (cachedFontWidth / 2));
                int box_y = (int) (p.getY() - (cachedFontHeight + WAYPOINT_Y_OFFSET + PADDING));
                y = y - cachedFontHeight - LABEL_Y_PADDING * 2;
                
                g.fillRoundRect(box_x - LABEL_X_PADDING, box_y - LABEL_Y_PADDING, cachedFontWidth + LABEL_X_PADDING * 2, cachedFontHeight + LABEL_Y_PADDING * 2, 10, 10);
                g.setPaint(Color.WHITE);
                g.drawString(label, box_x, box_y + cachedFontAscent);
            }
        }
        g.drawImage(cachedImg, x, y, null);
    }
    
    
    public GeoPosition getPosition()
    {
        return mapPos;
    }
    
    
    public String toString() 
    {
        return label != null ? label : "(null)";
    }

}
