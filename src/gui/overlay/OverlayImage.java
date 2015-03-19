package gui.overlay;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
import com.drew.metadata.exif.GpsDirectory;



public final class OverlayImage
{
    public static final int MAX_CONCURRENTLY_VISIBLE_IMAGES = 1;
    
    public static final int TOP_RIGHT     = 0;
    public static final int TOP_LEFT      = 1;
    public static final int BOT_RIGHT     = 2;
    public static final int BOT_LEFT      = 3;
    
    public static final int WAYPOINT_Y_OFFSET = 35;
    public static final int PADDING           = 3;
    public static final int LABEL_X_PADDING   = 5;
    public static final int LABEL_Y_PADDING   = 2;

    private BufferedImage   img;
    private BufferedImage   cachedImg;
    private boolean         highQuality   = true;
    private String          label         = null;
    private boolean         displayLabel     = true;
    private boolean         visible       = false;
    private boolean         dynamicResize = true;
    private boolean         fixedPosition = false;
    private int             positionHint  = TOP_LEFT;
    private GeoPosition     mapPos        = null;
    private int             mapZoom       = -1;

    private int             targetWidth;
    private int             targetHeight;
    

    public OverlayImage(String f, boolean strict) throws IOException
    {
        File file = new File(f);
        img = ImageIO.read(file);
        cachedImg = ImageIO.read(file);
        if (img == null)
            throw new IOException("not a valid image");
        label = StringUtil.basename(f);
        targetWidth = img.getWidth();
        targetHeight = img.getHeight();
        
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            GpsDirectory directory = metadata.getDirectory(GpsDirectory.class);
            GeoLocation loc = directory.getGeoLocation();
            //System.out.println("Extracted geolocation: " + loc.getLatitude() + " " + loc.getLongitude());
            mapPos = new GeoPosition(loc.getLatitude(), loc.getLongitude());
        }
        catch (ImageProcessingException e) {
        }
        catch (NullPointerException e) {
            // no/not enough metadata
        }
        
        if (strict && mapPos == null) {
            throw new RuntimeException("image contains no valid geo location (strict checking)");
        }
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
    
    
    public OverlayImage setVisible(boolean v)
    {
        visible = v;
        return this;
    }
    
    
    public OverlayImage setHighQuality(boolean q)
    {
        highQuality = q;
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
        return resizeInternal(w, h);
    }
    
    
    public OverlayImage resizeInternal(int w, int h)
    {
        if ((w > -1 && w != img.getWidth()) || (h > -1 && h != img.getHeight())) {
            cachedImg = ImageUtil.getScaledInstance(img, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR, highQuality);
        }
        return this;
    }

    public OverlayImage maxSize(int m)
    {
        return maxSizeInternal(m, true);
    }

    private OverlayImage maxSizeInternal(int m, boolean update)
    {
        float fW = (float) m / img.getWidth();
        float fH = (float) m / img.getHeight();
        if (img.getWidth() < img.getHeight())
            fW = fH;

        if (update)
            return resize((int) (img.getWidth() * fW), (int) (img.getHeight() * fW));
        else
            return resizeInternal((int) (img.getWidth() * fW), (int) (img.getHeight() * fW));
    }
    
    
    public OverlayImage dynamicResize(boolean dr)
    {
        dynamicResize = dr;
        return this;
    }


    public OverlayImage useFixedPos(boolean useFixedPos)
    {
        fixedPosition = useFixedPos;
        return this;
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
        if (!visible)
            return;
        
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
            if (map.getZoom() != mapZoom) {
                mapZoom = map.getZoom();
                if (dynamicResize) {
                    double ff = (mapZoom) / (Math.log(mapZoom) + 1);
                    int w = (int) ((targetWidth / ff) - (targetWidth * 0.01 * (mapZoom-1)));
                    int h = (int) ((targetHeight / ff) - (targetHeight * 0.01 * (mapZoom-1)));
                    if (w > 20 && h > 20) // arbitrary minimum
                        resizeInternal(w, h);
                }
            }
            Point2D p = map.getTileFactory().geoToPixel(mapPos, mapZoom);
            // reside centered above the location
            x = (int) p.getX() - (cachedImg.getWidth() / 2);
            y = (int) p.getY() - (cachedImg.getHeight() + WAYPOINT_Y_OFFSET + PADDING);
            
            if (displayLabel && label != null) {
                FontMetrics fm = g.getFontMetrics();
                int font_w = fm.stringWidth(label);
                int font_h = fm.getHeight();
                
                g.setPaint(new Color(0, 0, 0, 150));
                
                int box_x = (int) (p.getX() - (font_w / 2));
                int box_y = (int) (p.getY() - (font_h + WAYPOINT_Y_OFFSET + PADDING));
                y = y - font_h - LABEL_Y_PADDING * 2;
                
                g.fillRoundRect(box_x - LABEL_X_PADDING, box_y - LABEL_Y_PADDING, font_w + LABEL_X_PADDING * 2, font_h + LABEL_Y_PADDING * 2, 10, 10);
                g.setPaint(Color.WHITE);
                g.drawString(label, box_x, box_y + fm.getAscent());
            }
        }
        g.drawImage(cachedImg, x, y, null);
    }
    
    
    public GeoPosition getPosition()
    {
        return mapPos;
    }

}
