package gui.overlay;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;

import com.drew.imaging.ImageMetadataReader;

import util.ImageUtil;



public final class OverlayImage
{
    public static final int TOP_RIGHT = 0;
    public static final int TOP_LEFT  = 1;
    public static final int BOT_RIGHT = 2;
    public static final int BOT_LEFT  = 3;
    
    private BufferedImage   img;
    private boolean         fixedPosition = true;
    private int             positionHint  = TOP_LEFT;
    private GeoPosition     mapPos;


    public OverlayImage(String file) throws IOException
    {
        img = ImageIO.read(new File(file));
        ImageMetadataReader r;
    }


    public OverlayImage resize(int w, int h)
    {
        img = ImageUtil.toBufferedImage(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        return this;
    }


    public OverlayImage resizeW(int w)
    {
        return resize(w, -1);
    }


    public OverlayImage resizeH(int h)
    {
        return resize(-1, h);
    }


    public OverlayImage fixedPos(boolean useFixedPos)
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
                x = rect.width - img.getWidth() + rect.x;
                y = rect.y;
                break;
            case BOT_LEFT:
                x = rect.x;
                y = rect.height - img.getHeight() + rect.y;
                break;
            case BOT_RIGHT:
                x = rect.width - img.getWidth() + rect.x;
                y = rect.height - img.getHeight() + rect.y;
                break;
            }
        }
        else 
        {
            Point2D p = map.getTileFactory().geoToPixel(mapPos, map.getZoom());
            x = (int) p.getX();
            y = (int) p.getY();
        }

        g.drawImage(img, x, y, null);
    }

}
