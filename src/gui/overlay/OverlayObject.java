package gui.overlay;

import java.awt.Graphics2D;

import org.jdesktop.swingx.JXMapViewer;



public interface OverlayObject
{
    public void draw(Graphics2D g, JXMapViewer map);
}
