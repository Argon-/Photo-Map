package util;

import gui.overlay.OverlayImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;



public class FileUtil
{

    /**
     * Case {@code f} points to a file: load the file as OverlayImage and add 
     * it to {@code c}.
     * <br>
     * Case {@code f} points to a directory: iterate all images in the 
     * directory (not recursive), load the files as OverlayImage and add 
     * them to {@code c}.
     * 
     * @param f path to a file or directory
     * @param c collection to add the images
     * @param maxSize maximum size on the lowest zoom level (resizing retains aspect ratio)
     * @param dynamicResize shrink images when zooming out
     * @return {@code true} when at least one image was loaded, {@code false} otherwise.
     */
    public static boolean loadOverlayImagesFrom(String f, Collection<OverlayImage> c, int maxSize, boolean dynamicResize)
    {
        File file = new File(f);
        int l = c.size();
        
        // f is a directory
        if (file.isDirectory())
        {
            try {
                Files.walk(file.toPath())
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            c.add(new OverlayImage(path.toAbsolutePath().toString())
                                    .maxSize(maxSize)
                                    .dynamicResize(dynamicResize));
                        }
                        catch (IOException e) {
                            System.out.println("Error loading: " + path.toAbsolutePath().toString() + " (0)");
                        }
                        catch (RuntimeException e) {
                            System.out.println("Ignoring image with insufficient tags: " + path.toAbsolutePath().toString() + " (1)");
                        }
                });
            }
            catch (IOException e) {
                System.out.println("Error loading from: " + f + " (2)");
            }
        }
        
        // f is a single file
        else if (file.isFile())
        {
            try {
                c.add(new OverlayImage(f)
                        .maxSize(maxSize)
                        .dynamicResize(dynamicResize));                
            }
            catch (IOException e) {
                System.out.println("Error loading: " + f + " (3)");
            }
            catch (RuntimeException e) {
                System.out.println("Ignoring image with insufficient tags: " + file.getAbsolutePath() + " (1)");
            }
        }
        
        return c.size() > l;
    }
    
    
    /**
     * Overloaded method using {@code maxSize = 400} and {@code dynamicResize = true}.
     * 
     * @see #loadOverlayImagesFrom
     * @param f path to a file or directory
     * @param c collection to add the images
     */
    public static boolean loadOverlayImagesFrom(String f, Collection<OverlayImage> c) 
    {
        return loadOverlayImagesFrom(f, c, 400, true);
    }

}
