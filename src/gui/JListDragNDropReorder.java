package gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;



/**
 * See {@link #JListDragNDropReorder(JList)}.
 */
public class JListDragNDropReorder<T> extends MouseAdapter
{

    private JList<T> list;
    private int start, stop;


    /**
     * This enables drag&drop for a {@link JList} using a {@link DefaultListModel}.
     * 
     * @param l
     */
    public JListDragNDropReorder(JList<T> l)
    {
        start = 0;
        stop = 0;
        list = l;
    }


    @Override
    public void mousePressed(MouseEvent e)
    {
        start = list.locationToIndex(e.getPoint());
    }


    @Override
    public void mouseReleased(MouseEvent e)
    {
        stop = list.locationToIndex(e.getPoint());
        if (stop >= 0 && stop != start)
            swapElements();
    }


    @Override
    public void mouseDragged(MouseEvent e)
    {
        mouseReleased(e);
        start = stop;
    }


    private void swapElements()
    {
        DefaultListModel<T> model = (DefaultListModel<T>) list.getModel();
        T e = model.elementAt(start);
        model.removeElementAt(start);
        model.insertElementAt(e, stop);
    }
}