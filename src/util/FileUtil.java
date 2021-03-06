package util;

import gui.overlay.OverlayImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;



public final class FileUtil
{
    /**
     * In case {@code f} points to a file: load the file as OverlayImage and add 
     * it to {@code c}.
     * <br>
     * In case {@code f} points to a directory: iterate all images in the 
     * directory (not recursive), load the files as OverlayImage and add 
     * them to {@code c}.
     * 
     * @param f path to a file or directory
     * @param c collection to add the images to
     * @param maxSize maximum image size on the lowest zoom level (resizing retains aspect ratio)
     * @param dynamicResize shrink images when zooming out
     * @param highQuality perform high(er) quality resizing
     * @return {@code true} when at least one image was loaded, {@code false} otherwise.
     */
    public static boolean loadOverlayImagesFrom(String f, Collection<OverlayImage> c, int maxSize, boolean dynamicResize, boolean highQuality)
    {
        File file = new File(f);
        int l = c.size();
        
        if (file.isDirectory())
        {
            try {
                Files.walk(file.toPath(), 1)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            c.add(new OverlayImage(path.toAbsolutePath().toString(), true)
                                    .setHighQuality(highQuality)
                                    .dynamicResize(dynamicResize)
                                    .maxSize(maxSize));
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
        
        else if (file.isFile())
        {
            try {
                c.add(new OverlayImage(f, true)
                        .setHighQuality(highQuality)
                        .dynamicResize(dynamicResize)
                        .maxSize(maxSize));                
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
     * See {@link #loadOverlayImagesFrom(String, Collection, int, boolean, boolean) loadOverlayImagesFrom}.
     * <br>
     * Overloaded convenience method using {@code maxSize = 400} and {@code dynamicResize = true}.
     * 
     * @see #loadOverlayImagesFrom
     * @param f path to a file or directory
     * @param c collection to add the images to
     */
    public static boolean loadOverlayImagesFrom(String f, Collection<OverlayImage> c) 
    {
        return loadOverlayImagesFrom(f, c, 400, true, true);
    }

}
