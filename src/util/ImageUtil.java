package util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;



public class ImageUtil
{
    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
            return (BufferedImage) img;
        
        BufferedImage bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimg.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return bimg;
    }
}
