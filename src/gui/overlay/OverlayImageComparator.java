package gui.overlay;

import java.util.Comparator;



public class OverlayImageComparator implements Comparator<OverlayImage>
{

    @Override
    public int compare(OverlayImage o1, OverlayImage o2)
    {
        return o1.getDate().compareTo(o2.getDate());
    }

}
