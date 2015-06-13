package gui;


import gui.overlay.Accommodation;
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
import path.search.TravelRoute;
import path.search.TravelRouteNode;
import path.search.TravelRouteNoteData;
import util.FileUtil;
import util.StopWatch;
import data_structures.graph.Graph;
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
    private static final int LOG_BUFFER_LENGTH = 10;

    private Graph graph = null;
    private Dijkstra d = null;
    
    // LinkedBlockingDeque in case we want to thread this stuff
    private final LinkedList<OverlayAggregate> overlay = new LinkedList<OverlayAggregate>();
    private final LinkedList<OverlayAggregate> persistentOverlay = new LinkedList<OverlayAggregate>();
    private final LinkedList<OverlayImage> overlayImages = new LinkedList<OverlayImage>();
    private final LinkedList<OverlayAggregate> overlayTour = new LinkedList<OverlayAggregate>();
    private final LinkedList<Accommodation> accommodations = new LinkedList<Accommodation>();
    private final LinkedList<String> logBuffer = new LinkedList<String>();
    
    // specifies if a click in mapMouseClicked() is supposed to target an Overlay element
    // these values are written in mapMouseMoved() and used in mapMouseClicked() & handleTargetedClick()
    private boolean targetedClick = false;
    private Accommodation clickTarget = null;
    
    private WaypointPainter<JXMapViewer> waypointPainter;
    private Painter<JXMapViewer> overlayPainter;

    private GeoPosition currSource = null;
    private GeoPosition currTarget = null;
    
    private boolean imagesHighQuality = true;
    private boolean imagesDynamicResize = true;
    private int     imagesSize = 400;
    
    private int logLines = 0;
    private boolean imageSelectedFromList = false;
    
    private JPanel              contentPane;
    private JXMapKit            mapKit;
    private JXMapViewer         map;
    private JList<OverlayImage> list_Images;
    private JScrollPane         scrollPane_Log;
    private JScrollPane         scrollPane_Images;
    private JTextArea           textArea_Log;
    private JTextField          textField_MaxAccommodationDist;
    private JButton             btn_LoadGraph;
    private JButton             btn_ClearMarkers;
    private JButton             btn_ClearAccommodations;
    private JButton             btn_AddImages;
    private JButton             btn_RemoveImage;
    private JButton             btn_CalculateRoute;
    private JButton             btn_SaveOptGraph;
    private JButton             btn_ShowAccommodations;
    private JButton             btn_RemoveAccommodation;
    private JComboBox<String>   cb_ResizeMethod;
    private JComboBox<String>   cb_ImageSize;
    private JComboBox<String>   cb_VisitOrder;
    private JComboBox<String>   cb_ImageQuality;
    private JComboBox<String>   cb_StartingPosition;
    private JLabel              lbl_ResizeMethod;
    private JLabel              lbl_ImageSize;
    private JLabel              lbl_VisitOrder;
    private JLabel              lbl_ImageQuality;
    private JLabel              lbl_MaxAccommodationDist;
    private JLabel              lbl_StartingPosition;
    private JSeparator          separator_1;
    private JSeparator          separator_2;
    private Component           verticalStrut;
    private JFileChooser        fd;


    /**
     * Create the main window.<br>
     * The UI is essentially single threaded, i.e. handlers (button clicks, etc.) are
     * not threaded and they will block the UI. This spares us the need for locking.
     * While time consuming actions are in progress the UI is not really useful anyway.
     * 
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
        
        log("Welcome! Load a graph file and/or photos to get started." + System.getProperty("line.separator"));
        log("Use your mouse to place markers." + System.getProperty("line.separator"));
        log("Left click marks a starting position, right click a destination."
                + System.getProperty("line.separator") + System.getProperty("line.separator"));
    }
    
    
    /**
     * Draw a rectangle enclosing every node of the loaded {@link Graph}.
     */
    public void drawGraphRect()
    {
        if (!graphLoaded())
            return;
        
        persistentOverlay.clear();
        overlay.clear();
        
        double[] lat = graph.getBoundingRectLat();
        double[] lon = graph.getBoundingRectLon();
        for (int i = 0; i < lat.length; ++i) {
            GeoPosition s = new GeoPosition(lat[i], lon[i]);
            GeoPosition t = new GeoPosition(lat[(i + 1) % lat.length],   lon[(i + 1) % lat.length]);
            persistentOverlay.add(OverlayAggregate.line(OverlayElement.lineBlackMedium(s, t)));
        }
    }


    private void initGUIComponents()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1400, 900);
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
        textArea_Log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        scrollPane_Log.setViewportView(textArea_Log);
        
        btn_CalculateRoute = new JButton("Calculate tour");
        btn_CalculateRoute.setToolTipText("Calculate a tour visiting all currently existing waypoints on the map.");
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
        
        btn_ShowAccommodations = new JButton("Show accomms.");
        btn_ShowAccommodations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_ShowAccommodations(e);
            }
        });
        btn_ShowAccommodations.setToolTipText("Show possible accommodations near the selected photo.");
        GridBagConstraints gbc_btn_ShowAccommodations = new GridBagConstraints();
        gbc_btn_ShowAccommodations.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_ShowAccommodations.insets = new Insets(0, 0, 5, 5);
        gbc_btn_ShowAccommodations.gridx = 6;
        gbc_btn_ShowAccommodations.gridy = 3;
        contentPane.add(btn_ShowAccommodations, gbc_btn_ShowAccommodations);
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
        
        btn_RemoveAccommodation = new JButton("Remove accomm.");
        btn_RemoveAccommodation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_RemoveAccommodation(e);
            }
        });
        btn_RemoveAccommodation.setToolTipText("Remove the selected photo's association with an accommodation." + 
                                                    " This will not remove the accommodation from the map.");
        GridBagConstraints gbc_btn_RemoveAccommodation = new GridBagConstraints();
        gbc_btn_RemoveAccommodation.fill = GridBagConstraints.HORIZONTAL;
        gbc_btn_RemoveAccommodation.insets = new Insets(0, 0, 5, 5);
        gbc_btn_RemoveAccommodation.gridx = 6;
        gbc_btn_RemoveAccommodation.gridy = 4;
        contentPane.add(btn_RemoveAccommodation, gbc_btn_RemoveAccommodation);
        
        verticalStrut = Box.createVerticalStrut(20);
        GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
        gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
        gbc_verticalStrut.gridx = 1;
        gbc_verticalStrut.gridy = 5;
        contentPane.add(verticalStrut, gbc_verticalStrut);
        
        separator_2 = new JSeparator();
        separator_2.setOrientation(SwingConstants.VERTICAL);
        GridBagConstraints gbc_separator_2 = new GridBagConstraints();
        gbc_separator_2.gridheight = 6;
        gbc_separator_2.fill = GridBagConstraints.VERTICAL;
        gbc_separator_2.insets = new Insets(0, 0, 0, 5);
        gbc_separator_2.gridx = 7;
        gbc_separator_2.gridy = 3;
        contentPane.add(separator_2, gbc_separator_2);
        
        lbl_MaxAccommodationDist = new JLabel("Range:");
        lbl_MaxAccommodationDist.setToolTipText("Maximum distance in meters from the selected photo");
        GridBagConstraints gbc_lbl_MaxAccommodationDist = new GridBagConstraints();
        gbc_lbl_MaxAccommodationDist.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_MaxAccommodationDist.anchor = GridBagConstraints.EAST;
        gbc_lbl_MaxAccommodationDist.gridx = 5;
        gbc_lbl_MaxAccommodationDist.gridy = 6;
        contentPane.add(lbl_MaxAccommodationDist, gbc_lbl_MaxAccommodationDist);
        
        textField_MaxAccommodationDist = new JTextField();
        textField_MaxAccommodationDist.setToolTipText("Maximum distance in meters from the selected photo");
        textField_MaxAccommodationDist.setText("1000");
        GridBagConstraints gbc_textField_MaxAccommodationDist = new GridBagConstraints();
        gbc_textField_MaxAccommodationDist.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_MaxAccommodationDist.insets = new Insets(0, 0, 5, 5);
        gbc_textField_MaxAccommodationDist.gridx = 6;
        gbc_textField_MaxAccommodationDist.gridy = 6;
        contentPane.add(textField_MaxAccommodationDist, gbc_textField_MaxAccommodationDist);
        textField_MaxAccommodationDist.setColumns(3);
        
        lbl_StartingPosition = new JLabel("Start:");
        lbl_StartingPosition.setToolTipText("Where to start (and end) the route from: the last placed starting marker or the currently selected photo.");
        GridBagConstraints gbc_lbl_StartingPosition = new GridBagConstraints();
        gbc_lbl_StartingPosition.anchor = GridBagConstraints.EAST;
        gbc_lbl_StartingPosition.insets = new Insets(0, 0, 5, 5);
        gbc_lbl_StartingPosition.gridx = 2;
        gbc_lbl_StartingPosition.gridy = 7;
        contentPane.add(lbl_StartingPosition, gbc_lbl_StartingPosition);
        
        cb_StartingPosition = new JComboBox<String>();
        cb_StartingPosition.setToolTipText("Where to start (and end) the route from: the last placed starting marker or the currently selected photo.");
        cb_StartingPosition.setModel(new DefaultComboBoxModel<String>(new String[] {"first photo", "last start mark"}));
        GridBagConstraints gbc_cb_StartingPosition = new GridBagConstraints();
        gbc_cb_StartingPosition.anchor = GridBagConstraints.WEST;
        gbc_cb_StartingPosition.insets = new Insets(0, 0, 5, 5);
        gbc_cb_StartingPosition.gridx = 3;
        gbc_cb_StartingPosition.gridy = 7;
        contentPane.add(cb_StartingPosition, gbc_cb_StartingPosition);
        
        lbl_ImageQuality = new JLabel("Photo quality:");
        lbl_ImageQuality.setToolTipText("Quality for photo resizing. This greatly affects performance.");
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
        lbl_VisitOrder.setToolTipText("The order to use for visiting the waypoints on the map.");
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
        cb_VisitOrder.setModel(new DefaultComboBoxModel<String>(new String[] {"selected order", "chronological", "shortest (greedy)"}));
        GridBagConstraints gbc_cb_VisitOrder = new GridBagConstraints();
        gbc_cb_VisitOrder.anchor = GridBagConstraints.WEST;
        gbc_cb_VisitOrder.insets = new Insets(0, 0, 5, 5);
        gbc_cb_VisitOrder.gridx = 3;
        gbc_cb_VisitOrder.gridy = 6;
        contentPane.add(cb_VisitOrder, gbc_cb_VisitOrder);
        
        lbl_ResizeMethod = new JLabel("Resize photos:");
        lbl_ResizeMethod.setToolTipText("Select whether to automatically adjust the size of photos when zooming the map.");
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
        lbl_ImageSize.setToolTipText("Maximum size for photos (on the lowest zoom level).");
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


    /**
     * Set-up for the map and the overlay rendering.
     */
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

                for (OverlayObject o : persistentOverlay) { o.draw(g, map); }
                for (OverlayObject o : overlayTour)       { o.draw(g, map); }
                for (OverlayObject o : overlay)           { o.draw(g, map); }
                for (Accommodation a : accommodations)    { a.getOverlay().draw(g, map); }
                for (OverlayObject o : overlayImages)     { o.draw(g, map); }
                
                g.dispose();
            }
        };
        
        final CompoundPainter<Painter<JXMapViewer> > c = new CompoundPainter<Painter<JXMapViewer> >(waypointPainter, overlayPainter);
        c.setCacheable(false);
        map.setOverlayPainter(c);      // $hide$ (WindowBuilder doesn't like this line)
        updateWaypoints();
    }
        
    
    /**
     * Mouse click listener.<br>
     * Dispatches the click depending on whether this click is considered "targeted".
     */
    private void mapMouseClicked(MouseEvent e)
    {
        // middle mouse button -> clear
        if (SwingUtilities.isMiddleMouseButton(e)) {
            clearMap();
            return;
        }
        
        // we are now left with either right or left click
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
        System.out.println("Associated photo with \"" + clickTarget.getLabel() + "\"");
    }
    
    
    private void handleRegularMapClick(MouseEvent e)
    {
        // right mouse button -> select target, but not without a source
        if (SwingUtilities.isRightMouseButton(e) && currSource == null) {
            System.out.println("Please set a source first");
            return;
        }

        // from here on it's either a left mouse button click (-> select source)
        // or a right mouse button click with a previously selected source 
        // (-> select target, calculate route)

        System.out.println();
        GeoPosition clickPos = map.convertPointToGeoPosition(e.getPoint());
        System.out.println("Clicked at  : " + String.format("%.4f", clickPos.getLatitude()) + ", "
                + String.format("%.4f", clickPos.getLongitude()));

        // we can only proceed with a graph
        if (!graphLoaded()) {
            return;
        }
        
        final StopWatch sw = new StopWatch();
        sw.lap();
        int n = graph.getNearestNode(clickPos);
        sw.lap();
        if (n == -1) {
            System.out.println("Found no node!");
            return;
        }
        System.out.println("Closest node: " + String.format("%.4f", graph.getLat(n)) + ", "
                + String.format("%.4f", graph.getLon(n)) + "  (found in " + sw.getLastInSecStr() + " sec)");

        if (SwingUtilities.isLeftMouseButton(e)) {
            currSource = graph.getPosition(n);
            currTarget = null;
            overlay.add(OverlayAggregate.route_var6(clickPos, currSource));
            d.setSource(n);
        }
        else if (SwingUtilities.isRightMouseButton(e)) {
            currTarget = graph.getPosition(n);
            d.setTarget(n);
            if (currSource != null && currTarget != null) {
                sw.lap();
                boolean r = d.pathFromTo();
                sw.lap();
                if (r) {
                    overlay.add(OverlayAggregate.route_multi_var3(d.getPath()));
                    System.out.println("Shortest route found in " + sw.getLastInSecStr(7) + " sec");
                }
                else {
                    System.out.println("Found no route to specified destination!");
                }
            }
        }
        mapKit.repaint();
    }
    
    
    /**
     * Check if the mouse is close to some relevant overlay objects:
     * <ul>
     * <li>Accommodation: consider this a "targeted" click
     * <li>Waypoint: display the corresponding image
     * </ul>
     * Both can happen at the same time.
     */
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
            
            pos = oi.getPos();
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
    
    
    /**
     * Load a new {@link Graph} and initialize a new {@link Dijkstra} object for the new graph.
     */
    private void btn_LoadGraph(ActionEvent e)
    {
        fd.setDialogTitle("Select a graph file");
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
                    graph = null;
                    d = null;
                    persistentOverlay.clear();
                    clearMap();
                    System.gc();
                    
                    // load new graph
                    final StopWatch sw = new StopWatch();
                    sw.lap();
                    graph = GraphFactory.load(file.getAbsolutePath());
                    sw.lap();
                    d = new Dijkstra(graph);
                    
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
    
    
    /**
     * Use {@link TravelRoute} to calculate a route with the given images as points
     * and other parameters.
     */
    private void btn_CalculateRoute(ActionEvent e)
    {
        if (!graphLoaded()) {
            return;
        }
        
        DefaultListModel<OverlayImage> dm = ((DefaultListModel<OverlayImage>) list_Images.getModel());
        if (dm.size() < 2) {
            System.out.println("Not enough photos loaded!");
            return;
        }
        
        final boolean startWithMarker = cb_StartingPosition.getSelectedIndex() == 1;        // TODO: this is bad
        final int order = cb_VisitOrder.getSelectedIndex();                                 // TODO: this is bad

        StopWatch sw = new StopWatch();
        TravelRoute tr = null;
        try {
            tr = new TravelRoute(graph, Collections.list(dm.elements()), order, startWithMarker && currSource != null ? currSource : null);
            sw.lap();
            tr.calculate();
        }
        catch (Exception e1) {
            System.out.println(e1.getMessage());
            return;
        }

        
        if (!tr.getRoute().isEmpty()) {
            System.out.println(System.getProperty("line.separator") + "Calculated tour:    (in " + sw.lap().getLastInSecStr() + " sec)");
            for (TravelRouteNode trn : tr.getNodes()) {
                TravelRouteNoteData data = trn.getData();
                System.out.println("   -> " + (data != null ? data.getLabel() : "(start marker)"));
            }
            
            overlayTour.clear();
            overlayTour.add(OverlayAggregate.route_multi_multi_var1(tr.getRoute()));
            map.repaint();
        }
        else {
            System.out.println("Found no route!");
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
    
    
    /**
     * Use a JFileChooser to load images.
     */
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
    
    
    /**
     * Select all accommodations around the currently selected image
     * and visualize them.
     */
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
        final LinkedList<Integer> l = graph.getNNodesInRange(oi.getPos().getLatitude(), 
                                                         oi.getPos().getLongitude(), radius);
        sw.lap();
        if (l != null && l.size() < 1) {
            System.out.println("No accommodation found! Try again with higher range");
            return;
        }
        System.out.println("Found " + l.size() + " nearby accommodation(s) in " + sw.getLastInSecStr() + " sec");
        System.out.println("Click one to associate it with the currently selected photo");
        
        // visualize tourism nodes
        accommodations.clear();
        for (int i : l) {
            accommodations.add(new Accommodation(graph.getNPosition(i), graph.getName(i)));
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
         * The following is not a real fix and does not help in a case where image A 
         * is associated with an accommodation which was just found with image B.
         * However, a proper fix would require more effort (computationally) just for
         * the sake of a minor visual thingy... (visible only when transparency is used)
         */
        if (oi.getAccommodation() != null)
            accommodations.remove(oi.getAccommodation());
        
        mapKit.repaint();
    }
    
    
    /**
     * Unlink the currently selected image with its accommodation.
     */
    private void btn_RemoveAccommodation(ActionEvent e)
    {
        OverlayImage oi = getSelectedImage();
        if (oi == null || oi.getAccommodation() == null)
            return;
        
        System.out.println("Removed photo's association with \"" + oi.getAccommodation().getLabel() + "\"");
        accommodations.add(oi.getAccommodation());
        oi.setAccommodation(null);
        map.repaint();
    }
    
    
    /**
     * Write out the currently loaded graph.
     */
    private void btn_SaveOptGraph(ActionEvent e)
    {
        if (!graphLoaded()) {
            return;
        }
        
        fd.setDialogTitle("Save as");
        fd.setSelectedFile(new File("graph"));
        fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fd.setMultiSelectionEnabled(false);
        int c = fd.showSaveDialog(this);
        
        switch (c) {
            case JFileChooser.APPROVE_OPTION:
                File file = fd.getSelectedFile();
                try {
                    graph.save(file.getAbsolutePath());
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

    
    /**
     * Focus the waypoint of a image selected in the GUI list.<br>
     * This used to also show the selected image. Necessary parts of the
     * code are still there.
     */
    private void list_Images(ListSelectionEvent e)
    {
        OverlayImage oi = list_Images.getSelectedValue();
        if (oi == null || list_Images.getValueIsAdjusting() || oi.isFixedPosition()) {
            return;
        }

        /* See commit 9f31026c5d83da909b4fbe89b66de5bef072fd84 for the reason
         * of this being commented  out
        for (OverlayImage ois : overlayImages)
            ois.setVisible(false);
        oi.setVisible(true);
        imageSelectedFromList = true;
        repaint();
        */
        
        if (oi.getPos() != null) {
            final int h = map.getHeight();
            mapKit.setAddressLocation(oi.getPos());
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
        // not needed, the value is read in btn_CalculateRoute()
    }
    
    
    private void cb_ResizeMethod(ActionEvent e)
    {
        imagesDynamicResize = cb_ResizeMethod.getSelectedIndex() == 0;     // TODO: this is bad
        for (OverlayImage oi : overlayImages) {
            oi.dynamicResize(imagesDynamicResize);
        }
        repaint();
    }
    
    
    private void cb_ImageSize(ActionEvent e)
    {
        String value = (String) cb_ImageSize.getSelectedItem();
        imagesSize = -1;
        
        if (value.matches("[0-9]{2,3}0 px")) {                             // TODO: this is bad
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
        imagesHighQuality = cb_ImageQuality.getSelectedIndex() == 0;       // TODO: this is bad
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
    
    
    /**
     * Clear some (not all!) overlay lists and reset
     * the currently selected source and target values.
     */
    private void clearMap()
    {
        overlay.clear();
        overlayTour.clear();
        mapKit.repaint();
        currSource = null;
        currTarget = null;
    }
    
    
    private boolean graphLoaded()
    {
        if (graph != null) {
            return true;
        }
        System.out.println("No graph loaded!");
        return false;
    }
    
    
    /**
     * @return the currently selected OverlayImage or {@code null}
     */
    private OverlayImage getSelectedImage()
    {
        if (list_Images.getModel().getSize() < 1) {
            System.out.println("No photos loaded!");
            return null;
        }

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
    
    
    /**
     * Log text to a GUI text area.<br>
     * <b>Note</b>: textArea_Log.append is not thread-safe
     */
    synchronized public void log(String s)
    {
        ++logLines;
        logBuffer.addLast(s);
        if (logBuffer.size() > LOG_BUFFER_LENGTH)
            logBuffer.removeFirst();
        
        if (logLines >= MAX_LOG_LENGTH) {
            logLines = logBuffer.size();
            textArea_Log.setText(String.join("", logBuffer));
        }
        else {
            textArea_Log.append(s);
        }
        // caret-position auto-update stops working after some text within the text area
        // was selected by the user, so we better scroll down manually
        textArea_Log.setCaretPosition(textArea_Log.getDocument().getLength());
    }
    
    
    public void addOverlay(OverlayAggregate oa)
    {
        overlay.add(oa);
    }
    
    
    public void addPersistentOverlay(OverlayAggregate oa)
    {
        persistentOverlay.add(oa);
    }

}
