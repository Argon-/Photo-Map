package gui;


import gui.overlay.OverlayAggregate;
import gui.overlay.OverlayElement;
import gui.overlay.OverlayImage;
import gui.overlay.OverlayObject;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;
import javax.swing.JButton;

import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

import path.search.Dijkstra;
import util.FileUtil;
import util.StopWatch;
import data_structures.graph.ArrayRepresentation;
import data_structures.graph.GraphFactory;
import data_structures.graph.InvalidGraphFormatException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JScrollPane;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JTextArea;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;



public class MainWindow extends JFrame
{
    private static final long serialVersionUID = -590468540732816556L;
    
    private static final int MAX_CONCURRENTLY_VISIBLE_IMAGES = 1;
    private static final int MAX_LOG_LENGTH = 200;

    private ArrayRepresentation g = null;
    private Dijkstra d = null;
    
    public final LinkedBlockingDeque<OverlayAggregate> overlayLines = new LinkedBlockingDeque<OverlayAggregate>();
    public final LinkedBlockingDeque<OverlayAggregate> persistentOverlayLines = new LinkedBlockingDeque<OverlayAggregate>();
    public final LinkedBlockingDeque<OverlayImage> overlayImages = new LinkedBlockingDeque<OverlayImage>();
    
    private WaypointPainter<JXMapViewer> waypointPainter;
    private Painter<JXMapViewer> overlayPainter;

    private GeoPosition currSource = null;
    private GeoPosition currTarget = null;
    
    private boolean imagesHighQuality = true;
    private boolean imagesDynamicResize = true;
    private int     imagesSize = 400;
    
    private int currLines = 0;
    private boolean imageSelectedFromList = false;
    
    private JPanel                contentPane;
    private JXMapKit              mapKit;
    private JXMapViewer           map;
    private JList<OverlayImage>   list_Images;
    private JScrollPane           scrollPane_Log;
    private JScrollPane           scrollPane_Images;
    private JTextArea             textArea_Log;
    private JButton               btn_LoadGraph;
    private JButton               btn_ClearLast;
    private JButton               btn_ClearAll;
    private JButton               btn_AddImages;
    private JButton               btn_RemoveImage;
    private JButton               btn_CalculateRoute;
    private JButton               btn_SaveOptGraph;
    private JComboBox<String>     cb_ResizeMethod;
    private JComboBox<String>     cb_ImageSize;
    private JComboBox<String>     cb_VisitOrder;
    private JComboBox<String>     cb_ImageQuality;
    private JLabel                lbl_ResizeMethod;
    private JLabel                lbl_ImageSize;
    private JLabel                lbl_VisitOrder;
    private JLabel                lbl_ImageQuality;
    private JSeparator            separator_0;
    private JSeparator            separator_1;
    private Component             verticalStrut;
    private JFileChooser          fd;


    /**
     * Create the frame.
     * @throws IOException 
     */
    public MainWindow()
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            System.out.println("Unable to set native look & feel");
            e1.printStackTrace();
        }
        
        initGUIComponents();
        configureMap();
        
        log("Welcome! Load a graph file and/or images to get started." + System.getProperty("line.separator"));
        log("Left mouse click places a starting position, right mouse click a destination." + System.getProperty("line.separator") + System.getProperty("line.separator"));
    }
    
    
    public void testInit() 
    {
        if (g == null) {
            try {
                g = GraphFactory.loadArrayRepresentation("/Users/Julian/Documents/Uni/_Fapra OSM/3/file-generation/out-stg.txt");
                d = new Dijkstra(g);
            }
            catch (InvalidGraphFormatException e) {
                System.out.println("Supplied graph has invalid format");
            }
            catch (IOException e) {
                System.out.println("Error reading graph");
            }
        }
        drawGraphRect();
    }
    
    
    public void drawGraphRect()
    {
        if (g == null)
            return;
        
        persistentOverlayLines.clear();
        overlayLines.clear();
        
        double[] lat = g.getBoundingRectLat();
        double[] lon = g.getBoundingRectLon();
        for (int i = 0; i < lat.length; ++i) {
            GeoPosition s = new GeoPosition(lat[i], lon[i]);
            GeoPosition t = new GeoPosition(lat[(i + 1) % lat.length],   lon[(i + 1) % lat.length]);
            persistentOverlayLines.add(OverlayAggregate.line(OverlayElement.lineBlackMedium(s, t)));
        }
    }


    private void initGUIComponents()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1300, 900);
        contentPane = new JPanel();
        contentPane.setBorder(null);
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl_contentPane.rowHeights = new int[] { 0, 20, 0, 0, 0, 0, 0, 0 };
        gbl_contentPane.columnWeights = new double[] { 1.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        gbl_contentPane.rowWeights = new double[] { 1.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        contentPane.setLayout(gbl_contentPane);

        mapKit = new JXMapKit();
        map = mapKit.getMainMap();
        mapKit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mapMouseClicked(e);
            }
        });
        GridBagConstraints gbc_mapKit = new GridBagConstraints();
        gbc_mapKit.gridwidth = 5;
        gbc_mapKit.insets = new Insets(0, 0, 5, 5);
        gbc_mapKit.fill = GridBagConstraints.BOTH;
        gbc_mapKit.gridx = 0;
        gbc_mapKit.gridy = 0;
        contentPane.add(mapKit, gbc_mapKit);
        
        scrollPane_Images = new JScrollPane();
        GridBagConstraints gbc_scrollPane_Images = new GridBagConstraints();
        gbc_scrollPane_Images.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_Images.gridwidth = 3;
        gbc_scrollPane_Images.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane_Images.gridx = 5;
        gbc_scrollPane_Images.gridy = 0;
        contentPane.add(scrollPane_Images, gbc_scrollPane_Images);
        
        list_Images = new JList<OverlayImage>();
        list_Images.setModel(new DefaultListModel<OverlayImage>());
        MouseAdapter lma = new JListDragNDropReorder<OverlayImage>(list_Images);
        list_Images.addMouseListener(lma);
        list_Images.addMouseMotionListener(lma);
        list_Images.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                list_Images(e);
            }
        });
        list_Images.setToolTipText("Select a photo to jump to its location. Use drag and drop to reorder photos.");
        list_Images.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //list_Images.setDragEnabled(true);
        scrollPane_Images.setViewportView(list_Images);
        
        scrollPane_Log = new JScrollPane();
        scrollPane_Log.setMinimumSize(new Dimension(200, 100));
        GridBagConstraints gbc_scrollPane_Log = new GridBagConstraints();
        gbc_scrollPane_Log.insets = new Insets(0, 0, 0, 5);
        gbc_scrollPane_Log.gridheight = 6;
        gbc_scrollPane_Log.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_Log.gridx = 0;
        gbc_scrollPane_Log.gridy = 1;
        contentPane.add(scrollPane_Log, gbc_scrollPane_Log);
        
        textArea_Log = new JTextArea();
        textArea_Log.setFont(new Font("Hasklig", Font.PLAIN, 11));
        scrollPane_Log.setViewportView(textArea_Log);
        
        btn_CalculateRoute = new JButton("Calculate route");
        btn_CalculateRoute.setToolTipText("Calculate a route visiting all currently existing waypoints on the map.");
        btn_CalculateRoute.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_CalculateRoute(e);
            }
        });
        
        btn_ClearLast = new JButton("Clear last marker");
        btn_ClearLast.setToolTipText("Remove the last set position marker from the map.");
        btn_ClearLast.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_ClearLast(e);
            }
        });
        btn_ClearLast.setActionCommand("Clear last");
        GridBagConstraints gbc_btn_ClearLast = new GridBagConstraints();
        gbc_btn_ClearLast.anchor = GridBagConstraints.WEST;
        gbc_btn_ClearLast.insets = new Insets(0, 0, 5, 5);
        gbc_btn_ClearLast.gridx = 1;
        gbc_btn_ClearLast.gridy = 1;
        contentPane.add(btn_ClearLast, gbc_btn_ClearLast);
        
        separator_0 = new JSeparator();
        separator_0.setOrientation(SwingConstants.VERTICAL);
        GridBagConstraints gbc_separator_0 = new GridBagConstraints();
        gbc_separator_0.fill = GridBagConstraints.VERTICAL;
        gbc_separator_0.gridheight = 6;
        gbc_separator_0.insets = new Insets(0, 0, 0, 5);
        gbc_separator_0.gridx = 2;
        gbc_separator_0.gridy = 1;
        contentPane.add(separator_0, gbc_separator_0);
        GridBagConstraints gbc_btn_CalculateRoute = new GridBagConstraints();
        gbc_btn_CalculateRoute.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_CalculateRoute.insets = new Insets(0, 0, 5, 5);
        gbc_btn_CalculateRoute.gridx = 4;
        gbc_btn_CalculateRoute.gridy = 1;
        contentPane.add(btn_CalculateRoute, gbc_btn_CalculateRoute);
        
        btn_RemoveImage = new JButton("Remove photo");
        btn_RemoveImage.setToolTipText("Remove the currently selected photo from the list.");
        btn_RemoveImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_RemoveImage(e);
            }
        });
        
        separator_1 = new JSeparator();
        separator_1.setOrientation(SwingConstants.VERTICAL);
        GridBagConstraints gbc_separator_1 = new GridBagConstraints();
        gbc_separator_1.fill = GridBagConstraints.VERTICAL;
        gbc_separator_1.gridheight = 6;
        gbc_separator_1.insets = new Insets(0, 0, 0, 5);
        gbc_separator_1.gridx = 5;
        gbc_separator_1.gridy = 1;
        contentPane.add(separator_1, gbc_separator_1);
        GridBagConstraints gbc_btn_RemoveImage = new GridBagConstraints();
        gbc_btn_RemoveImage.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_RemoveImage.insets = new Insets(0, 0, 5, 0);
        gbc_btn_RemoveImage.gridx = 7;
        gbc_btn_RemoveImage.gridy = 1;
        contentPane.add(btn_RemoveImage, gbc_btn_RemoveImage);
        
        btn_AddImages = new JButton("Add photos");
        btn_AddImages.setToolTipText("Add either a single photo or multiple photos from a directory.");
        btn_AddImages.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_AddImages(e);
            }
        });
        
        btn_ClearAll = new JButton("Clear all markers");
        btn_ClearAll.setToolTipText("Remove all position markers from the map. Shortcut: middle mouse button");
        btn_ClearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_ClearAll(e);
            }
        });
        GridBagConstraints gbc_btn_ClearAll = new GridBagConstraints();
        gbc_btn_ClearAll.anchor = GridBagConstraints.WEST;
        gbc_btn_ClearAll.insets = new Insets(0, 0, 5, 5);
        gbc_btn_ClearAll.gridx = 1;
        gbc_btn_ClearAll.gridy = 2;
        contentPane.add(btn_ClearAll, gbc_btn_ClearAll);
        
        verticalStrut = Box.createVerticalStrut(20);
        GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
        gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
        gbc_verticalStrut.gridx = 1;
        gbc_verticalStrut.gridy = 3;
        contentPane.add(verticalStrut, gbc_verticalStrut);
        
        btn_LoadGraph = new JButton("Load graph");
        btn_LoadGraph.setToolTipText("Load a graph from either an optimized (binary) or plain text file.");
        btn_LoadGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_LoadGraph(e);
            }
        } );
        GridBagConstraints gbc_btn_LoadGraph = new GridBagConstraints();
        gbc_btn_LoadGraph.anchor = GridBagConstraints.WEST;
        gbc_btn_LoadGraph.insets = new Insets(0, 0, 5, 5);
        gbc_btn_LoadGraph.gridx = 1;
        gbc_btn_LoadGraph.gridy = 5;
        contentPane.add(btn_LoadGraph, gbc_btn_LoadGraph);
        
        btn_SaveOptGraph = new JButton("Save opt graph");
        btn_SaveOptGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_SaveOptGraph(e);
            }
        });
        btn_SaveOptGraph.setToolTipText("Save the currently loaded graph as optimized (binary) file for faster loading.");
        GridBagConstraints gbc_btn_SaveOptGraph = new GridBagConstraints();
        gbc_btn_SaveOptGraph.anchor = GridBagConstraints.WEST;
        gbc_btn_SaveOptGraph.insets = new Insets(0, 0, 0, 5);
        gbc_btn_SaveOptGraph.gridx = 1;
        gbc_btn_SaveOptGraph.gridy = 6;
        contentPane.add(btn_SaveOptGraph, gbc_btn_SaveOptGraph);
        
        lbl_ImageQuality = new JLabel("Photo quality:");
        GridBagConstraints gbc_lbl_ImageQuality = new GridBagConstraints();
        gbc_lbl_ImageQuality.anchor = GridBagConstraints.EAST;
        gbc_lbl_ImageQuality.insets = new Insets(0, 0, 0, 5);
        gbc_lbl_ImageQuality.gridx = 6;
        gbc_lbl_ImageQuality.gridy = 6;
        contentPane.add(lbl_ImageQuality, gbc_lbl_ImageQuality);
        
        cb_ImageQuality = new JComboBox<String>();
        cb_ImageQuality.setToolTipText("Quality for photo resizing. This greatly affects performance.");
        cb_ImageQuality.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cb_ImageQuality(e);
            }
        });
        cb_ImageQuality.setModel(new DefaultComboBoxModel<String>(new String[] {"high", "low"}));
        GridBagConstraints gbc_cb_ImageQuality = new GridBagConstraints();
        gbc_cb_ImageQuality.anchor = GridBagConstraints.WEST;
        gbc_cb_ImageQuality.gridx = 7;
        gbc_cb_ImageQuality.gridy = 6;
        contentPane.add(cb_ImageQuality, gbc_cb_ImageQuality);
        GridBagConstraints gbc_btn_AddImages = new GridBagConstraints();
        gbc_btn_AddImages.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_AddImages.insets = new Insets(0, 0, 5, 0);
        gbc_btn_AddImages.gridx = 7;
        gbc_btn_AddImages.gridy = 2;
        contentPane.add(btn_AddImages, gbc_btn_AddImages);
        
        lbl_VisitOrder = new JLabel("Visit in order:");
        GridBagConstraints gbc_lbl_VisitOrder = new GridBagConstraints();
        gbc_lbl_VisitOrder.anchor = GridBagConstraints.EAST;
        gbc_lbl_VisitOrder.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_VisitOrder.gridx = 3;
        gbc_lbl_VisitOrder.gridy = 4;
        contentPane.add(lbl_VisitOrder, gbc_lbl_VisitOrder);
        
        cb_VisitOrder = new JComboBox<String>();
        cb_VisitOrder.setToolTipText("The order to use for visiting the waypoints on the map.");
        cb_VisitOrder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cb_VisitOrder(e);
            }
        });
        cb_VisitOrder.setModel(new DefaultComboBoxModel<String>(new String[] {"chronological", "shortest route", "selected order"}));
        GridBagConstraints gbc_cb_VisitOrder = new GridBagConstraints();
        gbc_cb_VisitOrder.anchor = GridBagConstraints.WEST;
        gbc_cb_VisitOrder.insets = new Insets(0, 0, 5, 5);
        gbc_cb_VisitOrder.gridx = 4;
        gbc_cb_VisitOrder.gridy = 4;
        contentPane.add(cb_VisitOrder, gbc_cb_VisitOrder);
        
        lbl_ResizeMethod = new JLabel("Resize photos:");
        GridBagConstraints gbc_lbl_ResizeMethod = new GridBagConstraints();
        gbc_lbl_ResizeMethod.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_ResizeMethod.anchor = GridBagConstraints.EAST;
        gbc_lbl_ResizeMethod.gridx = 6;
        gbc_lbl_ResizeMethod.gridy = 4;
        contentPane.add(lbl_ResizeMethod, gbc_lbl_ResizeMethod);
        
        cb_ResizeMethod = new JComboBox<String>();
        cb_ResizeMethod.setToolTipText("Select whether to automatically adjust the size of photos when zooming the map.");
        cb_ResizeMethod.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cb_ResizeMethod(e);
            }
        });
        cb_ResizeMethod.setModel(new DefaultComboBoxModel<String>(new String[] {"when zooming", "don't resize"}));
        GridBagConstraints gbc_cb_ResizeMethod = new GridBagConstraints();
        gbc_cb_ResizeMethod.anchor = GridBagConstraints.WEST;
        gbc_cb_ResizeMethod.insets = new Insets(0, 0, 5, 0);
        gbc_cb_ResizeMethod.gridx = 7;
        gbc_cb_ResizeMethod.gridy = 4;
        contentPane.add(cb_ResizeMethod, gbc_cb_ResizeMethod);
        
        lbl_ImageSize = new JLabel("Limit photo size:");
        GridBagConstraints gbc_lbl_ImageSize = new GridBagConstraints();
        gbc_lbl_ImageSize.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_ImageSize.anchor = GridBagConstraints.EAST;
        gbc_lbl_ImageSize.gridx = 6;
        gbc_lbl_ImageSize.gridy = 5;
        contentPane.add(lbl_ImageSize, gbc_lbl_ImageSize);
        
        cb_ImageSize = new JComboBox<String>();
        cb_ImageSize.setToolTipText("Maximum size for photos (on the lowest zoom level).");
        cb_ImageSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cb_ImageSize(e);
            }
        });
        cb_ImageSize.setModel(new DefaultComboBoxModel<String>(new String[] {"100 px", "200 px", "400 px", "600 px", "800 px", "original"}));
        cb_ImageSize.setSelectedIndex(2);
        GridBagConstraints gbc_cb_ImageSize = new GridBagConstraints();
        gbc_cb_ImageSize.insets = new Insets(0, 0, 5, 0);
        gbc_cb_ImageSize.anchor = GridBagConstraints.WEST;
        gbc_cb_ImageSize.gridx = 7;
        gbc_cb_ImageSize.gridy = 5;
        contentPane.add(cb_ImageSize, gbc_cb_ImageSize);
        
        map.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                mapMouseClicked(e);
            }
        });
        
        map.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                mapMouseMoved(e);
            }
        });
        
        fd = new JFileChooser();
        textArea_Log.setEditable(false);
        ((DefaultCaret) textArea_Log.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }


    public void configureMap()
    {
        
        mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
        mapKit.setAddressLocationShown(false); // don't show center
        //mapKit.setAddressLocation(new GeoPosition(48.74670985863194, 9.105284214019775)); // Uni
        mapKit.setAddressLocation(new GeoPosition(48.89088888888889, 9.225294444444444)); // Home
        mapKit.setZoom(1);
        mapKit.setMiniMapVisible(false);
        
        
        waypointPainter = new WaypointPainter<JXMapViewer>();
        overlayPainter = new Painter<JXMapViewer>() {
            @Override
            public void paint(Graphics2D g, JXMapViewer map, int w, int h)
            {
                g = (Graphics2D) g.create();
                // convert from viewport to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x, -rect.y);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                for (OverlayObject o : overlayImages)          { o.draw(g, map); }
                for (OverlayObject o : overlayLines)           { o.draw(g, map); }
                for (OverlayObject o : persistentOverlayLines) { o.draw(g, map); }
                
                g.dispose();
            }
        };
        
        final CompoundPainter<Painter<JXMapViewer>> c = new CompoundPainter<Painter<JXMapViewer>>(waypointPainter, overlayPainter);
        c.setCacheable(false);
        map.setOverlayPainter(c);      // $hide$ (WindowBuilder doesn't like this line)
        updateWaypoints();
    }
    
    
    public void clearMap()
    {
        overlayLines.clear();
        mapKit.repaint();
        currSource = null;
        currTarget = null;
    }
        
    
    public void mapMouseClicked(MouseEvent e)
    {
        if (g == null) {
            System.out.println("Error: must load a graph first!");
            return;
        }
        
        // middle mouse button -> clear
        if (SwingUtilities.isMiddleMouseButton(e)) {
            clearMap();
            return;
        }
        // right mouse button -> select target, but not without a source
        else if (SwingUtilities.isRightMouseButton(e) && currSource == null) {
            System.out.println("Please set a source first");
            return;
        }

        // from here on it's either a left mouse button click (-> select source)
        // or a right mouse button click with a previously selected source (-> select target, calculate route)

        System.out.println();
        GeoPosition clickPos = map.convertPointToGeoPosition(e.getPoint());
        System.out.println("Clicked at  : " + String.format("%.4f", clickPos.getLatitude()) + ", " + String.format("%.4f", clickPos.getLongitude()));

        StopWatch.lap();
        int n = g.getNearestNode(clickPos.getLatitude(), clickPos.getLongitude());
        StopWatch.lap();
        if (n == -1) {
            System.out.println("Found no node!");
            return;
        }
        System.out.println("Closest node: " + String.format("%.4f", g.getLat(n)) + ", " + String.format("%.4f", g.getLon(n)) + "  (found in "
                + String.format("%.3f", StopWatch.getLastLapSec()) + " sec)");

        
        if (SwingUtilities.isLeftMouseButton(e)) 
        {
            currSource = g.getPosition(n);
            currTarget = null;
            overlayLines.add(OverlayAggregate.route_var3(clickPos, currSource));
            d.setSource(n);
        }
        else if (SwingUtilities.isRightMouseButton(e)) 
        {
            currTarget = g.getPosition(n);
            d.setTarget(n);
            if (currSource != null && currTarget != null) {
                StopWatch.lap();
                boolean r = d.pathFromTo();
                StopWatch.lap();
                if (r) {
                    overlayLines.add(OverlayAggregate.route_multi_var2(d.getRoute()));
                }
                System.out.println("Calculated route in " + String.format("%.3f", StopWatch.getLastLapSec()) + " sec");
            }
        }

        mapKit.repaint();
    }
    
    
    public void mapMouseMoved(MouseEvent e)
    {
        int zoom = map.getZoom();
        GeoPosition pos = null;
        boolean change = false;
        int visibleNum = 0;
        
        for (OverlayImage oi : overlayImages) 
        {
            // there's already one visible image, imply invisibility for the others
            if (visibleNum >= MAX_CONCURRENTLY_VISIBLE_IMAGES) {
                if (oi.isVisible() && !imageSelectedFromList) {
                    oi.setVisible(false);
                    change = true;
                }
                continue;
            }
            
            pos = oi.getPosition();
            if (pos == null) {
                continue;
            }
            
            Point2D p = map.getTileFactory().geoToPixel(pos, zoom);
            Rectangle rect = map.getViewportBounds();
            // move the point "into" the center of the waypoint image
            p.setLocation(p.getX() - rect.x, p.getY() - rect.y - (OverlayImage.WAYPOINT_Y_OFFSET / 2));
            
            // are we close to oi's Waypoint ?
            if (p.distance(e.getPoint()) < (OverlayImage.WAYPOINT_Y_OFFSET / 2)) {
                // we are close enough, but oi was not visible until now
                if (!oi.isVisible()) {
                    oi.setVisible(true);
                    imageSelectedFromList = false;
                    change = true;
                }
                ++visibleNum;
            }
            else {
                // this image is not in range but visible
                if (oi.isVisible() && !imageSelectedFromList) {
                    oi.setVisible(false);
                    change = true;
                }
            }
        }

        if (change) {
            repaint();
        }
    }
    
    
    public void btn_ClearAll(ActionEvent e)
    {
        clearMap();
    }
    
    
    public void btn_ClearLast(ActionEvent e)
    {
        overlayLines.pollLast();
        mapKit.repaint();
        currSource = null;
        currTarget = null;
    }
    
    
    public void btn_LoadGraph(ActionEvent e)
    {
        fd.setDialogTitle("Select a graph file");
        fd.setCurrentDirectory(new File("/Users/Julian/Documents/Uni/_Fapra OSM/3/file-generation"));
        fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int c = fd.showOpenDialog(this);
        
        switch (c) {
            case JFileChooser.APPROVE_OPTION:
                File file = fd.getSelectedFile();
                try {
                    StopWatch.lap();
                    g = GraphFactory.loadArrayRepresentation(file.getAbsolutePath());
                    StopWatch.lap();
                    d = new Dijkstra(g);
                    clearMap();
                    drawGraphRect();
                    System.out.println("Graph loaded in " + String.format("%.3f", StopWatch.getLastLapSec()) + " sec");
                    g.drawNonRoutableNodes(this);
                }
                catch (InvalidGraphFormatException ex) {
                    System.out.println("Error: supplied graph has invalid format");
                }
                catch (IOException ex) {
                    System.out.println("Error: unable to read graph");
                }
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }
    }
    
    
    public void btn_CalculateRoute(ActionEvent e)
    {
        System.err.println("btn_CalculateRoute not yet implemented!");
        if (list_Images.getModel().getSize() < 1) {
            System.out.println("No photos loaded!");
            return;
        }
    }
    
    
    public void btn_RemoveImage(ActionEvent e)
    {
        OverlayImage oi = list_Images.getSelectedValue();
        if (oi == null) {
            return;
        }
        else if (list_Images.getValueIsAdjusting()) {
            return;
        }
        
        int i = list_Images.getSelectedIndex();
        DefaultListModel<OverlayImage> model = (DefaultListModel<OverlayImage>) list_Images.getModel();
        model.remove(i);
        overlayImages.remove(oi);
        imageSelectedFromList = false;
        updateWaypoints();
        repaint();
    }
    
    
    public void btn_AddImages(ActionEvent e)
    {
        fd.setDialogTitle("Select image(s) or a directory containing images");
        fd.setCurrentDirectory(new File("./"));
        fd.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fd.setMultiSelectionEnabled(true);
        int c = fd.showOpenDialog(this);
        
        switch (c) {
            case JFileChooser.APPROVE_OPTION:
                File[] files = fd.getSelectedFiles();
                LinkedList<OverlayImage> list = new LinkedList<OverlayImage>();
                StopWatch.lap();
                for (File file : files) {
                    FileUtil.loadOverlayImagesFrom(file.getAbsolutePath(), list, imagesSize, imagesDynamicResize, imagesHighQuality);
                }
                System.out.println("Processed images in: " + StopWatch.lapSecStr() + " sec");
                
                DefaultListModel<OverlayImage> model = (DefaultListModel<OverlayImage>) list_Images.getModel();
                for (OverlayImage oi : list) {
                    model.addElement(oi);
                    overlayImages.add(oi);
                }
                
                updateWaypoints();
                repaint();
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }       
    }
    
    
    public void btn_SaveOptGraph(ActionEvent e)
    {
        if (g == null) {
            System.out.println("Error: must load a graph first!");
            return;
        }
        
        fd.setDialogTitle("Save as");
        fd.setCurrentDirectory(new File("/Users/Julian/Documents/Uni/_Fapra OSM/3/file-generation"));
        fd.setSelectedFile(new File("graph"));
        fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fd.setMultiSelectionEnabled(false);
        int c = fd.showSaveDialog(this);
        
        switch (c) {
            case JFileChooser.APPROVE_OPTION:
                File file = fd.getSelectedFile();
                try {
                    g.save(file.getAbsolutePath());
                    System.out.println("Successfully saved optimized graph");
                }
                catch (IOException e1) {
                    System.out.println("Failed to save optimized graph!");
                }
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }       
    }

    
    public void list_Images(ListSelectionEvent e)
    {
        OverlayImage oi = list_Images.getSelectedValue();
        if (oi == null || list_Images.getValueIsAdjusting() || oi.isFixedPosition()) {
            return;
        }

        for (OverlayImage ois : overlayImages)
            ois.setVisible(false);
        oi.setVisible(true);
        imageSelectedFromList = true;
        repaint();

        if (oi.getPosition() != null) {
            int h = map.getHeight();
            mapKit.setAddressLocation(oi.getPosition());
            Point2D p = map.getCenter();
            
            if (oi.getHeight() > (3 * h / 4)) {
                p.setLocation(p.getX(), p.getY() - (h/2) + OverlayImage.PADDING);
            }
            else if (oi.getHeight() > (h / 3)) {
                p.setLocation(p.getX(), p.getY() - (h/4));
            }
            map.setCenter(p);
        }
    }
    
    
    public void cb_VisitOrder(ActionEvent e)
    {
        System.err.println("cb_VisitOrder not yet implemented!");
    }
    
    
    public void cb_ResizeMethod(ActionEvent e)
    {
        if (!(e.getSource() instanceof JComboBox<?>)) {
            System.err.println("cb_ResizeMethod: e.getSource() is no instance of JComboBox");
            return;
        }
        
        @SuppressWarnings("unchecked")
        JComboBox<String> cb = (JComboBox<String>)e.getSource();
        String value = (String) cb.getSelectedItem();
        imagesDynamicResize = value == "when zooming";
        
        for (OverlayImage oi : overlayImages) {
            oi.dynamicResize(imagesDynamicResize);
        }
        repaint();
    }
    
    
    public void cb_ImageSize(ActionEvent e)
    {
        if (!(e.getSource() instanceof JComboBox<?>)) {
            System.err.println("cb_ImageSize: e.getSource() is no instance of JComboBox");
            return;
        }
        
        @SuppressWarnings("unchecked")
        JComboBox<String> cb = (JComboBox<String>)e.getSource();
        String value = (String) cb.getSelectedItem();
        imagesSize = -1;
        
        if (value.matches("[0-9]{2,3}0 px")) {
            value = value.substring(0, value.length() - 3);
            imagesSize = Integer.parseInt(value);
        }
        
        for (OverlayImage oi : overlayImages) {
            oi.maxSize(imagesSize, true);
        }
        repaint();
    }
    
    
    public void cb_ImageQuality(ActionEvent e)
    {
        if (!(e.getSource() instanceof JComboBox<?>)) {
            System.err.println("cb_ImageQuality: e.getSource() is no instance of JComboBox");
            return;
        }
        
        @SuppressWarnings("unchecked")
        JComboBox<String> cb = (JComboBox<String>)e.getSource();
        String value = (String) cb.getSelectedItem();
        imagesHighQuality = value == "high";
        
        for (OverlayImage oi : overlayImages) {
            oi.setHighQuality(imagesHighQuality);
        }
        repaint();
    }
        

    public void updateWaypoints()
    {
        HashSet<Waypoint> waypointSet = new HashSet<Waypoint>();
        for (OverlayImage oi : overlayImages) {
            Waypoint wp = oi.getWaypoint();
            if (wp != null)
                waypointSet.add(wp);
        }
        waypointPainter.setWaypoints(waypointSet);
    }
    
    
    public void log(String s)
    {
        if (currLines++ > MAX_LOG_LENGTH) {
            currLines = 1;
            textArea_Log.setText("");
        }
        textArea_Log.append(s);
    }
    
}
