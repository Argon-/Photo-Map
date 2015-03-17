package gui.overlay;

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

import util.ImageUtil;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;



public final class OverlayImage
{
    public static final int TOP_RIGHT = 0;
    public static final int TOP_LEFT  = 1;
    public static final int BOT_RIGHT = 2;
    public static final int BOT_LEFT  = 3;
    
    private BufferedImage   img;
    private BufferedImage   cachedImg;
    private boolean dynamicResize = true;
    private int mapZoom = -1;
    
    private int targetWidth;
    private int targetHeight;

    private boolean         fixedPosition = true;
    private int             positionHint  = TOP_LEFT;
    private GeoPosition     mapPos = null;
    

    public OverlayImage(String file) throws IOException
    {
        File f = new File(file);
        img = ImageIO.read(f);
        cachedImg = ImageIO.read(f);
        targetWidth = img.getWidth();
        targetHeight = img.getHeight();
        
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(f);
            GpsDirectory directory = metadata.getDirectory(GpsDirectory.class);
            GeoLocation loc = directory.getGeoLocation();
            System.out.println("Extracted geolocation: " + loc.getLatitude() + " " + loc.getLongitude());
            mapPos = new GeoPosition(loc.getLatitude(), loc.getLongitude());
        }
        catch (ImageProcessingException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
        }
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
            cachedImg = ImageUtil.getScaledInstance(img, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
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
        if (img.getWidth() < img.getHeight()) {
            fW = fH;
        }

        if (update) {
            return resize((int) (img.getWidth() * fW), (int) (img.getHeight() * fW));
        }
        else {
            return resizeInternal((int) (img.getWidth() * fW), (int) (img.getHeight() * fW));
        }
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
                    int h = (int) ((targetHeight / ff) - (targetWidth * 0.01 * (mapZoom-1)));
                    System.out.println("Current zoom: " + mapZoom);
                    System.out.println("   new width: " + w + " ");
                    System.out.println("   new height: " + h);
                    if (w > 20 && h > 20) // arbitrary minimum
                        resizeInternal(w, h);
                }
            }
            Point2D p = map.getTileFactory().geoToPixel(mapPos, mapZoom);
            x = (int) p.getX() - (cachedImg.getWidth() / 2);
            y = (int) p.getY() - (cachedImg.getHeight() / 2);
        }
        
        g.drawImage(cachedImg, x, y, null);
    }

}
