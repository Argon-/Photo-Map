package gui;


import gui.overlay.OverlayAggregate;
import gui.overlay.OverlayElement;
import gui.overlay.OverlayImage;
import gui.overlay.OverlayImageComparator;
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
import util.Accommodation;
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
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JTextField;



public class MainWindow extends JFrame implements Serializable
{
    private static final long serialVersionUID = -590468540732816556L;
    
    private static final int MAX_CONCURRENTLY_VISIBLE_IMAGES = 1;
    private static final int MAX_LOG_LENGTH = 200;

    private ArrayRepresentation g = null;
    private Dijkstra d = null;
    
    // LinkedBlockingDeque
    private final LinkedList<OverlayAggregate> overlay = new LinkedList<OverlayAggregate>();
    private final LinkedList<OverlayAggregate> persistentOverlay = new LinkedList<OverlayAggregate>();
    private final LinkedList<OverlayImage> overlayImages = new LinkedList<OverlayImage>();
    private final LinkedList<Accommodation> accommodations = new LinkedList<Accommodation>();
    
    // specifies if a click in mapMouseClicked() is supposed to target an Overlay element
    // this is determined in mapMouseMoved()
    private boolean targetedClick = false;
    private Accommodation clickTarget = null;
    
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
    private JButton               btn_ClearMarkers;
    private JButton               btn_ClearAccommodations;
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
    private JSeparator            separator_1;
    private Component             verticalStrut;
    private JFileChooser          fd;
    private JButton btn_ShowAccommodations;
    private JSeparator separator_2;
    private JButton btn_RemoveAccommodation;
    private JTextField textField_MaxAccommodationDist;
    private JLabel lbl_MaxAccommodationDist;


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
        
        ToolTipManager.sharedInstance().setDismissDelay(6000);
        
        initGUIComponents();
        configureMap();
        
        log("Welcome! Load a graph file and/or images to get started." + System.getProperty("line.separator"));
        log("Left mouse click places a starting position, right mouse click a destination."
                + System.getProperty("line.separator") + System.getProperty("line.separator"));
    }
    
    
    public void testInit()
    {
        if (!graphLoaded()) {
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
        if (!graphLoaded())
            return;
        
        persistentOverlay.clear();
        overlay.clear();
        
        double[] lat = g.getBoundingRectLat();
        double[] lon = g.getBoundingRectLon();
        for (int i = 0; i < lat.length; ++i) {
            GeoPosition s = new GeoPosition(lat[i], lon[i]);
            GeoPosition t = new GeoPosition(lat[(i + 1) % lat.length],   lon[(i + 1) % lat.length]);
            persistentOverlay.add(OverlayAggregate.line(OverlayElement.lineBlackMedium(s, t)));
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
        gbl_contentPane.columnWidths = new int[] { 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 20, 0, 0, 0, 0, 0, 0 };
        gbl_contentPane.columnWeights = new double[] { 1.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 1.0,
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
        gbc_mapKit.gridheight = 3;
        gbc_mapKit.gridwidth = 7;
        gbc_mapKit.insets = new Insets(0, 0, 5, 5);
        gbc_mapKit.fill = GridBagConstraints.BOTH;
        gbc_mapKit.gridx = 0;
        gbc_mapKit.gridy = 0;
        contentPane.add(mapKit, gbc_mapKit);
        
        btn_ClearMarkers = new JButton("Clear markers");
        btn_ClearMarkers.setToolTipText("Remove all set position markers from the map.");
        btn_ClearMarkers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_ClearMarkers(e);
            }
        });
        GridBagConstraints gbc_btn_ClearMarkers = new GridBagConstraints();
        gbc_btn_ClearMarkers.gridwidth = 2;
        gbc_btn_ClearMarkers.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_ClearMarkers.insets = new Insets(0, 0, 5, 5);
        gbc_btn_ClearMarkers.gridx = 7;
        gbc_btn_ClearMarkers.gridy = 0;
        contentPane.add(btn_ClearMarkers, gbc_btn_ClearMarkers);
        
        btn_ClearAccommodations = new JButton("Clear accomm.");
        btn_ClearAccommodations.setToolTipText("Remove all unassociated accommodations from the map.");
        btn_ClearAccommodations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_ClearAccommodations(e);
            }
        });
        
        btn_LoadGraph = new JButton("Load graph");
        btn_LoadGraph.setToolTipText("Load a graph from either an optimized (binary) or plain text file.");
        btn_LoadGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_LoadGraph(e);
            }
        } );
        GridBagConstraints gbc_btn_LoadGraph = new GridBagConstraints();
        gbc_btn_LoadGraph.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_LoadGraph.insets = new Insets(0, 0, 5, 0);
        gbc_btn_LoadGraph.gridx = 9;
        gbc_btn_LoadGraph.gridy = 0;
        contentPane.add(btn_LoadGraph, gbc_btn_LoadGraph);
        GridBagConstraints gbc_btn_ClearAccommodations = new GridBagConstraints();
        gbc_btn_ClearAccommodations.gridwidth = 2;
        gbc_btn_ClearAccommodations.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_ClearAccommodations.insets = new Insets(0, 0, 5, 5);
        gbc_btn_ClearAccommodations.gridx = 7;
        gbc_btn_ClearAccommodations.gridy = 1;
        contentPane.add(btn_ClearAccommodations, gbc_btn_ClearAccommodations);
        
        btn_SaveOptGraph = new JButton("Save graph");
        btn_SaveOptGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_SaveOptGraph(e);
            }
        });
        btn_SaveOptGraph.setToolTipText("Save the currently loaded graph as optimized (binary) file for faster loading.");
        GridBagConstraints gbc_btn_SaveOptGraph = new GridBagConstraints();
        gbc_btn_SaveOptGraph.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_SaveOptGraph.insets = new Insets(0, 0, 5, 0);
        gbc_btn_SaveOptGraph.gridx = 9;
        gbc_btn_SaveOptGraph.gridy = 1;
        contentPane.add(btn_SaveOptGraph, gbc_btn_SaveOptGraph);
        
        scrollPane_Images = new JScrollPane();
        GridBagConstraints gbc_scrollPane_Images = new GridBagConstraints();
        gbc_scrollPane_Images.gridwidth = 3;
        gbc_scrollPane_Images.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_Images.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane_Images.gridx = 7;
        gbc_scrollPane_Images.gridy = 2;
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
        gbc_scrollPane_Log.gridy = 3;
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
        GridBagConstraints gbc_btn_CalculateRoute = new GridBagConstraints();
        gbc_btn_CalculateRoute.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_CalculateRoute.insets = new Insets(0, 0, 5, 5);
        gbc_btn_CalculateRoute.gridx = 3;
        gbc_btn_CalculateRoute.gridy = 3;
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
        gbc_separator_1.gridx = 4;
        gbc_separator_1.gridy = 3;
        contentPane.add(separator_1, gbc_separator_1);
        
        this.btn_ShowAccommodations = new JButton("Show accommodations");
        this.btn_ShowAccommodations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_ShowAccommodations(e);
            }
        });
        this.btn_ShowAccommodations.setToolTipText("Show possible accommodations near the selected photo.");
        GridBagConstraints gbc_btn_ShowAccommodations = new GridBagConstraints();
        gbc_btn_ShowAccommodations.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_ShowAccommodations.insets = new Insets(0, 0, 5, 5);
        gbc_btn_ShowAccommodations.gridx = 6;
        gbc_btn_ShowAccommodations.gridy = 3;
        this.contentPane.add(this.btn_ShowAccommodations, gbc_btn_ShowAccommodations);
        GridBagConstraints gbc_btn_RemoveImage = new GridBagConstraints();
        gbc_btn_RemoveImage.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_RemoveImage.insets = new Insets(0, 0, 5, 0);
        gbc_btn_RemoveImage.gridx = 9;
        gbc_btn_RemoveImage.gridy = 4;
        contentPane.add(btn_RemoveImage, gbc_btn_RemoveImage);
        
        btn_AddImages = new JButton("Add photos");
        btn_AddImages.setToolTipText("Add either a single photo or multiple photos from a directory.");
        btn_AddImages.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_AddImages(e);
            }
        });
        
        this.btn_RemoveAccommodation = new JButton("Remove accomm.");
        this.btn_RemoveAccommodation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_RemoveAccommodation(e);
            }
        });
        this.btn_RemoveAccommodation.setToolTipText("Remove the selected photo's association with an accommodation." + 
                                                    " This will not remove the accommodation from the map.");
        GridBagConstraints gbc_btn_RemoveAccommodation = new GridBagConstraints();
        gbc_btn_RemoveAccommodation.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_RemoveAccommodation.insets = new Insets(0, 0, 5, 5);
        gbc_btn_RemoveAccommodation.gridx = 6;
        gbc_btn_RemoveAccommodation.gridy = 4;
        this.contentPane.add(this.btn_RemoveAccommodation, gbc_btn_RemoveAccommodation);
        
        verticalStrut = Box.createVerticalStrut(20);
        GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
        gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
        gbc_verticalStrut.gridx = 1;
        gbc_verticalStrut.gridy = 5;
        contentPane.add(verticalStrut, gbc_verticalStrut);
        
        this.separator_2 = new JSeparator();
        this.separator_2.setOrientation(SwingConstants.VERTICAL);
        GridBagConstraints gbc_separator_2 = new GridBagConstraints();
        gbc_separator_2.gridheight = 6;
        gbc_separator_2.fill = GridBagConstraints.VERTICAL;
        gbc_separator_2.insets = new Insets(0, 0, 0, 5);
        gbc_separator_2.gridx = 7;
        gbc_separator_2.gridy = 3;
        this.contentPane.add(this.separator_2, gbc_separator_2);
        
        this.lbl_MaxAccommodationDist = new JLabel("Range:");
        GridBagConstraints gbc_lbl_MaxAccommodationDist = new GridBagConstraints();
        gbc_lbl_MaxAccommodationDist.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_MaxAccommodationDist.anchor = GridBagConstraints.EAST;
        gbc_lbl_MaxAccommodationDist.gridx = 5;
        gbc_lbl_MaxAccommodationDist.gridy = 6;
        this.contentPane.add(this.lbl_MaxAccommodationDist, gbc_lbl_MaxAccommodationDist);
        
        this.textField_MaxAccommodationDist = new JTextField();
        this.textField_MaxAccommodationDist.setToolTipText("Maximum distance in meters from the selected photo");
        this.textField_MaxAccommodationDist.setText("1000");
        GridBagConstraints gbc_textField_MaxAccommodationDist = new GridBagConstraints();
        gbc_textField_MaxAccommodationDist.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_MaxAccommodationDist.insets = new Insets(0, 0, 5, 5);
        gbc_textField_MaxAccommodationDist.gridx = 6;
        gbc_textField_MaxAccommodationDist.gridy = 6;
        this.contentPane.add(this.textField_MaxAccommodationDist, gbc_textField_MaxAccommodationDist);
        this.textField_MaxAccommodationDist.setColumns(3);
        
        lbl_ImageQuality = new JLabel("Photo quality:");
        GridBagConstraints gbc_lbl_ImageQuality = new GridBagConstraints();
        gbc_lbl_ImageQuality.anchor = GridBagConstraints.EAST;
        gbc_lbl_ImageQuality.insets = new Insets(0, 0, 0, 5);
        gbc_lbl_ImageQuality.gridx = 8;
        gbc_lbl_ImageQuality.gridy = 8;
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
        gbc_cb_ImageQuality.gridx = 9;
        gbc_cb_ImageQuality.gridy = 8;
        contentPane.add(cb_ImageQuality, gbc_cb_ImageQuality);
        GridBagConstraints gbc_btn_AddImages = new GridBagConstraints();
        gbc_btn_AddImages.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_AddImages.insets = new Insets(0, 0, 5, 0);
        gbc_btn_AddImages.gridx = 9;
        gbc_btn_AddImages.gridy = 3;
        contentPane.add(btn_AddImages, gbc_btn_AddImages);
        
        lbl_VisitOrder = new JLabel("Visit order:");
        GridBagConstraints gbc_lbl_VisitOrder = new GridBagConstraints();
        gbc_lbl_VisitOrder.anchor = GridBagConstraints.EAST;
        gbc_lbl_VisitOrder.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_VisitOrder.gridx = 2;
        gbc_lbl_VisitOrder.gridy = 6;
        contentPane.add(lbl_VisitOrder, gbc_lbl_VisitOrder);
        
        cb_VisitOrder = new JComboBox<String>();
        cb_VisitOrder.setToolTipText("The order to use for visiting the waypoints on the map.");
        cb_VisitOrder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cb_VisitOrder(e);
            }
        });
        cb_VisitOrder.setModel(new DefaultComboBoxModel<String>(new String[] {"chronological", "selected order", "shortest route"}));
        GridBagConstraints gbc_cb_VisitOrder = new GridBagConstraints();
        gbc_cb_VisitOrder.anchor = GridBagConstraints.WEST;
        gbc_cb_VisitOrder.insets = new Insets(0, 0, 5, 5);
        gbc_cb_VisitOrder.gridx = 3;
        gbc_cb_VisitOrder.gridy = 6;
        contentPane.add(cb_VisitOrder, gbc_cb_VisitOrder);
        
        lbl_ResizeMethod = new JLabel("Resize photos:");
        GridBagConstraints gbc_lbl_ResizeMethod = new GridBagConstraints();
        gbc_lbl_ResizeMethod.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_ResizeMethod.anchor = GridBagConstraints.EAST;
        gbc_lbl_ResizeMethod.gridx = 8;
        gbc_lbl_ResizeMethod.gridy = 6;
        contentPane.add(lbl_ResizeMethod, gbc_lbl_ResizeMethod);
        
        cb_ResizeMethod = new JComboBox<String>();
        cb_ResizeMethod.setToolTipText("Select whether to automatically adjust the size of photos when zooming the map.");
        cb_ResizeMethod.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cb_ResizeMethod(e);
            }
        });
        cb_ResizeMethod.setModel(new DefaultComboBoxModel<String>(new String[] {"to zoom", "don't resize"}));
        GridBagConstraints gbc_cb_ResizeMethod = new GridBagConstraints();
        gbc_cb_ResizeMethod.anchor = GridBagConstraints.WEST;
        gbc_cb_ResizeMethod.insets = new Insets(0, 0, 5, 0);
        gbc_cb_ResizeMethod.gridx = 9;
        gbc_cb_ResizeMethod.gridy = 6;
        contentPane.add(cb_ResizeMethod, gbc_cb_ResizeMethod);
        
        lbl_ImageSize = new JLabel("Limit size:");
        GridBagConstraints gbc_lbl_ImageSize = new GridBagConstraints();
        gbc_lbl_ImageSize.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_ImageSize.anchor = GridBagConstraints.EAST;
        gbc_lbl_ImageSize.gridx = 8;
        gbc_lbl_ImageSize.gridy = 7;
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
        gbc_cb_ImageSize.gridx = 9;
        gbc_cb_ImageSize.gridy = 7;
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


    private void configureMap()
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
                
                for (OverlayObject o : overlay)           { o.draw(g, map); }
                for (OverlayObject o : persistentOverlay) { o.draw(g, map); }
                for (Accommodation a : accommodations)    { a.getOverlay().draw(g, map); }
                for (OverlayObject o : overlayImages)     { o.draw(g, map); }
                
                g.dispose();
            }
        };
        
        final CompoundPainter<Painter<JXMapViewer>> c = new CompoundPainter<Painter<JXMapViewer>>(waypointPainter, overlayPainter);
        c.setCacheable(false);
        map.setOverlayPainter(c);      // $hide$ (WindowBuilder doesn't like this line)
        updateWaypoints();
    }
        
    
    private void mapMouseClicked(MouseEvent e)
    {
        // targeted click -> get the Overlay element he clicked
        // not?           -> assume the user wanted to place a marker for standard route calculation
        if (targetedClick)
        {
            // add accommodation to image
            handleTargetedClick(e);
        }
        else
        {
            // place source/target markers and calculate shortest route
            handleRegularMapClick(e);
        }
    }
    
    
    private void handleTargetedClick(MouseEvent e)
    {
        // swing dispatches events one at a time and callbacks block the dispatching
        // so this shouldn't be necessary (if it were we'd need proper locking anyway)
        if (clickTarget == null || !targetedClick) {
            System.out.println("clickTarget == null || !targetedClick, this should not have happened");
            return;
        }

        OverlayImage oi = getSelectedImage();
        if (oi == null)
            return;
        
        // no change
        if (oi.getAccommodation() == clickTarget) {
            return;
        }
        
        // there is already an accommodation associated with oi
        // we are going to replace it, therefore move it back into the accomm list
        if (oi.getAccommodation() != null) {
            accommodations.add(oi.getAccommodation());
        }
        
        oi.setAccommodation(clickTarget);
        accommodations.remove(clickTarget);
        map.repaint();
        System.out.println("Associated photo with \"" + clickTarget.getName() + "\"");
    }
    
    
    private void handleRegularMapClick(MouseEvent e)
    {
        // middle mouse button -> clear
        if (SwingUtilities.isMiddleMouseButton(e)) {
            clearMap();
            return;
        }
        // we can only proceed with a graph
        if (!graphLoaded()) {
            return;
        }
        // right mouse button -> select target, but not without a source
        if (SwingUtilities.isRightMouseButton(e) && currSource == null) {
            System.out.println("Please set a source first");
            return;
        }

        // from here on it's either a left mouse button click (-> select source)
        // or a right mouse button click with a previously selected source (->
        // select target, calculate route)

        System.out.println();
        GeoPosition clickPos = map.convertPointToGeoPosition(e.getPoint());
        System.out.println("Clicked at  : " + String.format("%.4f", clickPos.getLatitude()) + ", "
                + String.format("%.4f", clickPos.getLongitude()));

        final StopWatch sw = new StopWatch();
        sw.lap();
        int n = g.getNearestNode(clickPos.getLatitude(), clickPos.getLongitude());
        sw.lap();
        if (n == -1) {
            System.out.println("Found no node!");
            return;
        }
        System.out.println("Closest node: " + String.format("%.4f", g.getLat(n)) + ", "
                + String.format("%.4f", g.getLon(n)) + "  (found in " + sw.getLastInSecStr() + " sec)");

        if (SwingUtilities.isLeftMouseButton(e)) {
            currSource = g.getPosition(n);
            currTarget = null;
            overlay.add(OverlayAggregate.route_var3(clickPos, currSource));
            d.setSource(n);
        }
        else if (SwingUtilities.isRightMouseButton(e)) {
            currTarget = g.getPosition(n);
            d.setTarget(n);
            if (currSource != null && currTarget != null) {
                sw.lap();
                boolean r = d.pathFromTo();
                sw.lap();
                if (r) {
                    overlay.add(OverlayAggregate.route_multi_var2(d.getRoute()));
                    System.out.println("Calculated route in " + sw.getLastInSecStr() + " sec");
                }
            }
        }
        mapKit.repaint();
    }
    
    
    private void mapMouseMoved(MouseEvent e)
    {        
        final int zoom = map.getZoom();
        final Rectangle rect = map.getViewportBounds();
        GeoPosition pos = null;
        boolean change = false;
        int visibleNum = 0;


        // when the mouse is above an accommodation the next click is handled 
        // as "targeted" click in mapMouseClicked
        targetedClick = false;
        clickTarget = null;
        for (Accommodation a : accommodations) 
        {
            final Point2D p = map.getTileFactory().geoToPixel(a.getPos(), zoom);
            p.setLocation(p.getX() - rect.x, p.getY() - rect.y);
            if (p.distance(e.getPoint()) <= (Accommodation.STROKE_WIDTH / 2)) 
            {
                targetedClick = true;
                clickTarget = a;
            }
        }

        
        for (OverlayImage oi : overlayImages) 
        {
            // there are already enough visible images, imply invisibility for the others
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
            
            final Point2D p = map.getTileFactory().geoToPixel(pos, zoom);
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
    
    
    private void btn_ClearAccommodations(ActionEvent e)
    {
        targetedClick = false;
        clickTarget = null;
        accommodations.clear();
        mapKit.repaint();
    }
    
    
    private void btn_ClearMarkers(ActionEvent e)
    {
        clearMap();
    }
    
    
    private void btn_LoadGraph(ActionEvent e)
    {
        fd.setDialogTitle("Select a graph file");
        fd.setCurrentDirectory(new File("/Users/Julian/Documents/Uni/_Fapra OSM/3/file-generation"));
        fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int c = fd.showOpenDialog(this);
        
        switch (c) {
            case JFileChooser.APPROVE_OPTION:
                File file = fd.getSelectedFile();
                try {
                    // reset everything old
                    accommodations.clear();
                    for (OverlayImage oi : overlayImages) { 
                        oi.setAccommodation(null);
                    }
                    g = null;
                    d = null;
                    persistentOverlay.clear();
                    clearMap();
                    System.gc();
                    
                    // load new graph
                    final StopWatch sw = new StopWatch();
                    sw.lap();
                    g = GraphFactory.loadArrayRepresentation(file.getAbsolutePath());
                    sw.lap();
                    d = new Dijkstra(g);
                    
                    drawGraphRect();
                    clearMap();
                    System.out.println("Graph loaded in " + sw.getLastInSecStr() + " sec");
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
    
    
    private void btn_CalculateRoute(ActionEvent e)
    {
        // TODO: better don't load a new graph or add/remove photos during this
        
        if (list_Images.getModel().getSize() < 1) {
            System.out.println("No photos loaded!");
            return;
        }
        
        DefaultListModel<OverlayImage> dm = ((DefaultListModel<OverlayImage>) list_Images.getModel());
        List<OverlayImage> chron = Collections.list(dm.elements());
        chron.sort(new OverlayImageComparator());
        
        System.out.println("Chronsort:");
        for (OverlayImage oi : chron) {
            System.out.println(oi.getDate() + " -> " + oi.getLabel());
        }
    }
    
    
    private void btn_RemoveImage(ActionEvent e)
    {
        OverlayImage oi = getSelectedImage();
        if (oi == null)
            return;
        
        int i = list_Images.getSelectedIndex();
        DefaultListModel<OverlayImage> model = (DefaultListModel<OverlayImage>) list_Images.getModel();
        model.remove(i);
        overlayImages.remove(oi);
        imageSelectedFromList = false;
        updateWaypoints();
        repaint();
    }
    
    
    private void btn_AddImages(ActionEvent e)
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
                final StopWatch sw = new StopWatch();
                sw.lap();
                for (File file : files) {
                    FileUtil.loadOverlayImagesFrom(file.getAbsolutePath(), list, imagesSize, imagesDynamicResize, imagesHighQuality);
                }
                System.out.println("Processed images in: " + sw.lap().getLastInSecStr() + " sec");
                
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
    
    
    private void btn_ShowAccommodations(ActionEvent e)
    {
        if (!graphLoaded())
            return;
        
        OverlayImage oi = getSelectedImage();
        if (oi == null)
            return;

        // parse radius from text field
        int radius = 0;
        try {
            radius = Integer.parseInt(textField_MaxAccommodationDist.getText());
        }
        catch (NumberFormatException ex) {
            System.out.println("Specified range is invalid!");
            return;
        }

        // request tourism nodes in range
        final StopWatch sw = new StopWatch().lap();
        final LinkedList<Integer> l = g.getNNodesInRange(oi.getPosition().getLatitude(), 
                                                         oi.getPosition().getLongitude(), radius);
        sw.lap();
        if (l != null && l.size() < 1) {
            System.out.println("No accommodation found! Try increasing the range");
            return;
        }
        System.out.println("Found " + l.size() + " nearby accommodation(s) in " + sw.getLastInSecStr() + " sec");
        System.out.println("Click one to associate it with the currently selected photo");
        
        // visualize tourism nodes
        accommodations.clear();
        for (int i : l) {
            accommodations.add(new Accommodation(g.getNPosition(i), g.getName(i)));
        }
        
        /*
         * There's one small problem resulting in a visual deviation.
         * Accommodations associated to an image are owned and drawn by this image,
         * while all the unassociated ones are organized in `accommodations` and
         * drawn with the general painter.
         * Therefore we can add (and draw) accommodations here which are already
         * drawn by an image. In fact, in this case there exist multiple instances
         * for the same accommodation.
         * However, this has no actual negative affect on the logic/program itself,
         * merely the visual aspects of two identical objects being drawn at the same 
         * position.
         * The following not a real fix and does not help in a case where image A 
         * is associated with an accommodation found with image B. But this would
         * require more effort (computationally) just to fix a visual thingy...
         */
        if (oi.getAccommodation() != null)
            accommodations.remove(oi.getAccommodation());
        
        mapKit.repaint();
    }
    
    
    private void btn_RemoveAccommodation(ActionEvent e)
    {
        OverlayImage oi = getSelectedImage();
        if (oi == null || oi.getAccommodation() == null)
            return;
        
        System.out.println("Removed photo's association with \"" + oi.getAccommodation().getName() + "\"");
        accommodations.add(oi.getAccommodation());
        oi.setAccommodation(null);
        map.repaint();
    }
    
    
    private void btn_SaveOptGraph(ActionEvent e)
    {
        if (!graphLoaded()) {
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

    
    private void list_Images(ListSelectionEvent e)
    {
        OverlayImage oi = list_Images.getSelectedValue();
        if (oi == null || list_Images.getValueIsAdjusting() || oi.isFixedPosition()) {
            return;
        }

        /*
        for (OverlayImage ois : overlayImages)
            ois.setVisible(false);
        oi.setVisible(true);
        imageSelectedFromList = true;
        repaint();
        */
        
        if (oi.getPosition() != null) {
            final int h = map.getHeight();
            mapKit.setAddressLocation(oi.getPosition());
            final Point2D p = map.getCenter();
            
            /*
            if (oi.getHeight() > (3 * h / 4)) {
                p.setLocation(p.getX(), p.getY() - (h/2) + OverlayImage.PADDING);
            }
            else if (oi.getHeight() > (h / 3)) {
                p.setLocation(p.getX(), p.getY() - (h/4));
            }
            */
            p.setLocation(p.getX(), p.getY() - (h/4));
            map.setCenter(p);
        }
    }
    
    
    private void cb_VisitOrder(ActionEvent e)
    {
        //System.err.println("cb_VisitOrder not yet implemented!");
    }
    
    
    private void cb_ResizeMethod(ActionEvent e)
    {
        if (!(e.getSource() instanceof JComboBox<?>)) {
            System.err.println("cb_ResizeMethod: e.getSource() is no instance of JComboBox");
            return;
        }
        
        @SuppressWarnings("unchecked")
        JComboBox<String> cb = (JComboBox<String>) e.getSource();
        imagesDynamicResize = cb.getSelectedIndex() == 0;       // TODO: this is bad
        
        for (OverlayImage oi : overlayImages) {
            oi.dynamicResize(imagesDynamicResize);
        }
        repaint();
    }
    
    
    private void cb_ImageSize(ActionEvent e)
    {
        if (!(e.getSource() instanceof JComboBox<?>)) {
            System.err.println("cb_ImageSize: e.getSource() is no instance of JComboBox");
            return;
        }
        
        @SuppressWarnings("unchecked")
        JComboBox<String> cb = (JComboBox<String>)e.getSource();
        String value = (String) cb.getSelectedItem();
        imagesSize = -1;
        
        if (value.matches("[0-9]{2,3}0 px")) {                  // TODO: this is bad
            value = value.substring(0, value.length() - 3);
            imagesSize = Integer.parseInt(value);
        }
        
        for (OverlayImage oi : overlayImages) {
            oi.maxSize(imagesSize, true);
        }
        repaint();
    }
    
    
    private void cb_ImageQuality(ActionEvent e)
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
        

    private void updateWaypoints()
    {
        HashSet<Waypoint> waypointSet = new HashSet<Waypoint>();
        for (OverlayImage oi : overlayImages) {
            Waypoint wp = oi.getWaypoint();
            if (wp != null)
                waypointSet.add(wp);
        }
        waypointPainter.setWaypoints(waypointSet);
    }
    
    
    private void clearMap()
    {
        overlay.clear();
        mapKit.repaint();
        currSource = null;
        currTarget = null;
    }
    
    
    private boolean graphLoaded()
    {
        if (g != null) {
            return true;
        }
        System.out.println("Error: must load a graph first!");
        return false;
    }
    
    
    private OverlayImage getSelectedImage()
    {
        OverlayImage oi = list_Images.getSelectedValue();
        if (oi == null) {
            System.out.println("No photo selected!");
            return null;
        }
        else if (list_Images.getValueIsAdjusting()) {
            return null;
        }
        return oi;
    }
    
    
    public void log(String s)
    {
        if (currLines++ > MAX_LOG_LENGTH) {
            currLines = 1;
            textArea_Log.setText("");
        }
        textArea_Log.append(s);
    }
    
    public void addOverlayAggregate(OverlayAggregate oa)
    {
        overlay.add(oa);
    }
    
    
    public void addPersistentOverlayAggregate(OverlayAggregate oa)
    {
        persistentOverlay.add(oa);
    }

}
