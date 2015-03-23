package gui;


import gui.overlay.OverlayAggregate;
import gui.overlay.OverlayElement;
import gui.overlay.OverlayImage;

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
	private static final long	serialVersionUID	= -590468540732816556L;
	
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
	
	private static final int MAX_LOG_LENGTH = 500;
	private int currLines = 0;
	private boolean imageSelectedFromList = false;
	
    private JPanel                                     contentPane;
    private JXMapKit                                   mapKit;
    private JXMapViewer                                map;
    private JScrollPane                                scrollPane_Log;
    private JTextArea                                  textArea_Log;
    private JButton                                    btn_LoadGraph;
    private JButton                                    btn_ClearLast;
    private JButton                                    btn_ClearAll;
    private JButton                                    btn_AddImages;
    private JButton                                    btn_RemoveImage;
    private JComboBox<String>                          cb_ResizeMethod;
    private JComboBox<String>                          cb_ImageSize;
    private JLabel                                     lbl_ResizeMethod;
    private JLabel                                     lbl_ImageSize;
    private JButton                                    btn_CalculateRoute;
    private JLabel                                     lbl_VisitOrder;
    private JComboBox<String>                          cb_VisitOrder;
    private JLabel                                     lbl_ImageQuality;
    private JComboBox<String>                          cb_ImageQuality;
    private JList<OverlayImage>                        list_Images;
    private JSeparator                                 separator_0;
    private JSeparator                                 separator_1;
    private Component                                  verticalStrut;
    private JScrollPane                                scrollPane_Images;


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
		
		initComponents();
		myInitComponents();
	}
	
	
	public void init() 
	{
	    if (g == null) {
    		try {
    			this.g = GraphFactory.loadArrayRepresentation("/Users/Julian/Documents/Uni/_Fapra OSM/3/file-generation/out-stg.txt");
    			this.d = new Dijkstra(this.g);
    			//g.drawCells(this);
    		}
    		catch (InvalidGraphFormatException e) {
    			System.out.println("Supplied graph has invalid format");
    		}
    		catch (IOException e) {
    			System.out.println("Error reading graph");
    		}
	    }
		
		double[] lat = this.g.getBoundingRectLat();
		double[] lon = this.g.getBoundingRectLon();
		for (int i = 0; i < lat.length; ++i) {
			GeoPosition s = new GeoPosition(lat[i], lon[i]);
			GeoPosition t = new GeoPosition(lat[(i + 1) % lat.length],   lon[(i + 1) % lat.length]);
			persistentOverlayLines.add(OverlayAggregate.line(OverlayElement.lineBlackMedium(s, t)));
		}
	}


	private void initComponents()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1300, 900);
		this.contentPane = new JPanel();
		this.contentPane.setBorder(null);
		setContentPane(this.contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 20, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		this.contentPane.setLayout(gbl_contentPane);

		this.mapKit = new JXMapKit();
		this.map = mapKit.getMainMap();
		this.mapKit.addMouseListener(new MouseAdapter() {
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
		this.contentPane.add(this.mapKit, gbc_mapKit);
		
		this.scrollPane_Images = new JScrollPane();
		GridBagConstraints gbc_scrollPane_Images = new GridBagConstraints();
		gbc_scrollPane_Images.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_Images.gridwidth = 3;
		gbc_scrollPane_Images.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_Images.gridx = 5;
		gbc_scrollPane_Images.gridy = 0;
		this.contentPane.add(this.scrollPane_Images, gbc_scrollPane_Images);
		
		this.list_Images = new JList<OverlayImage>();
		this.list_Images.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		        list_Images(e);
		    }
		});
		this.list_Images.setModel(new DefaultListModel<OverlayImage>());
		this.list_Images.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.scrollPane_Images.setViewportView(this.list_Images);
		this.list_Images.setDragEnabled(true);
		
		this.scrollPane_Log = new JScrollPane();
		this.scrollPane_Log.setMinimumSize(new Dimension(200, 100));
		GridBagConstraints gbc_scrollPane_Log = new GridBagConstraints();
		gbc_scrollPane_Log.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_Log.gridheight = 6;
		gbc_scrollPane_Log.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_Log.gridx = 0;
		gbc_scrollPane_Log.gridy = 1;
		this.contentPane.add(this.scrollPane_Log, gbc_scrollPane_Log);
		
		this.textArea_Log = new JTextArea();
		textArea_Log.setFont(new Font("Hasklig", Font.PLAIN, 11));
		this.scrollPane_Log.setViewportView(this.textArea_Log);
		
		this.btn_CalculateRoute = new JButton("Calculate route");
		this.btn_CalculateRoute.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        btn_CalculateRoute(e);
		    }
		});
		
		this.btn_ClearLast = new JButton("Clear last marker");
		this.btn_ClearLast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btn_ClearLast(e);
			}
		});
		this.btn_ClearLast.setActionCommand("Clear last");
		GridBagConstraints gbc_btn_ClearLast = new GridBagConstraints();
		gbc_btn_ClearLast.anchor = GridBagConstraints.WEST;
		gbc_btn_ClearLast.insets = new Insets(0, 0, 5, 5);
		gbc_btn_ClearLast.gridx = 1;
		gbc_btn_ClearLast.gridy = 1;
		this.contentPane.add(this.btn_ClearLast, gbc_btn_ClearLast);
		
		this.separator_0 = new JSeparator();
		this.separator_0.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_separator_0 = new GridBagConstraints();
		gbc_separator_0.fill = GridBagConstraints.VERTICAL;
		gbc_separator_0.gridheight = 6;
		gbc_separator_0.insets = new Insets(0, 0, 0, 5);
		gbc_separator_0.gridx = 2;
		gbc_separator_0.gridy = 1;
		this.contentPane.add(this.separator_0, gbc_separator_0);
		GridBagConstraints gbc_btn_CalculateRoute = new GridBagConstraints();
		gbc_btn_CalculateRoute.fill = GridBagConstraints.HORIZONTAL;
		gbc_btn_CalculateRoute.insets = new Insets(0, 0, 5, 5);
		gbc_btn_CalculateRoute.gridx = 4;
		gbc_btn_CalculateRoute.gridy = 1;
		this.contentPane.add(this.btn_CalculateRoute, gbc_btn_CalculateRoute);
		
		this.btn_RemoveImage = new JButton("Remove image");
		this.btn_RemoveImage.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        btn_RemoveImage(e);
		    }
		});
		
		this.separator_1 = new JSeparator();
		this.separator_1.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.fill = GridBagConstraints.VERTICAL;
		gbc_separator_1.gridheight = 6;
		gbc_separator_1.insets = new Insets(0, 0, 0, 5);
		gbc_separator_1.gridx = 5;
		gbc_separator_1.gridy = 1;
		this.contentPane.add(this.separator_1, gbc_separator_1);
		GridBagConstraints gbc_btn_RemoveImage = new GridBagConstraints();
		gbc_btn_RemoveImage.fill = GridBagConstraints.HORIZONTAL;
		gbc_btn_RemoveImage.insets = new Insets(0, 0, 5, 0);
		gbc_btn_RemoveImage.gridx = 7;
		gbc_btn_RemoveImage.gridy = 1;
		this.contentPane.add(this.btn_RemoveImage, gbc_btn_RemoveImage);
		
		this.btn_AddImages = new JButton("Add images");
		this.btn_AddImages.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        btn_AddImages(e);
		    }
		});
		
		this.btn_ClearAll = new JButton("Clear all markers");
		this.btn_ClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btn_ClearAll(e);
			}
		});
		GridBagConstraints gbc_btn_ClearAll = new GridBagConstraints();
		gbc_btn_ClearAll.anchor = GridBagConstraints.WEST;
		gbc_btn_ClearAll.insets = new Insets(0, 0, 5, 5);
		gbc_btn_ClearAll.gridx = 1;
		gbc_btn_ClearAll.gridy = 2;
		this.contentPane.add(this.btn_ClearAll, gbc_btn_ClearAll);
		
		this.btn_LoadGraph = new JButton("Load Graph");
		this.btn_LoadGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btn_LoadGraph(e);
			}
		} );
		
		this.verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridx = 1;
		gbc_verticalStrut.gridy = 3;
		this.contentPane.add(this.verticalStrut, gbc_verticalStrut);
		GridBagConstraints gbc_btn_LoadGraph = new GridBagConstraints();
		gbc_btn_LoadGraph.anchor = GridBagConstraints.WEST;
		gbc_btn_LoadGraph.insets = new Insets(0, 0, 0, 5);
		gbc_btn_LoadGraph.gridx = 1;
		gbc_btn_LoadGraph.gridy = 6;
		this.contentPane.add(this.btn_LoadGraph, gbc_btn_LoadGraph);
		
		this.lbl_ImageQuality = new JLabel("Image quality:");
		GridBagConstraints gbc_lbl_ImageQuality = new GridBagConstraints();
		gbc_lbl_ImageQuality.anchor = GridBagConstraints.EAST;
		gbc_lbl_ImageQuality.insets = new Insets(0, 0, 0, 5);
		gbc_lbl_ImageQuality.gridx = 6;
		gbc_lbl_ImageQuality.gridy = 6;
		this.contentPane.add(this.lbl_ImageQuality, gbc_lbl_ImageQuality);
		
		this.cb_ImageQuality = new JComboBox<String>();
		this.cb_ImageQuality.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        cb_ImageQuality(e);
		    }
		});
		this.cb_ImageQuality.setModel(new DefaultComboBoxModel<String>(new String[] {"high", "low"}));
		GridBagConstraints gbc_cb_ImageQuality = new GridBagConstraints();
		gbc_cb_ImageQuality.anchor = GridBagConstraints.WEST;
		gbc_cb_ImageQuality.gridx = 7;
		gbc_cb_ImageQuality.gridy = 6;
		this.contentPane.add(this.cb_ImageQuality, gbc_cb_ImageQuality);
		GridBagConstraints gbc_btn_AddImages = new GridBagConstraints();
		gbc_btn_AddImages.fill = GridBagConstraints.HORIZONTAL;
		gbc_btn_AddImages.insets = new Insets(0, 0, 5, 0);
		gbc_btn_AddImages.gridx = 7;
		gbc_btn_AddImages.gridy = 2;
		this.contentPane.add(this.btn_AddImages, gbc_btn_AddImages);
		
		this.lbl_VisitOrder = new JLabel("Visit in order:");
		GridBagConstraints gbc_lbl_VisitOrder = new GridBagConstraints();
		gbc_lbl_VisitOrder.anchor = GridBagConstraints.EAST;
		gbc_lbl_VisitOrder.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_VisitOrder.gridx = 3;
		gbc_lbl_VisitOrder.gridy = 4;
		this.contentPane.add(this.lbl_VisitOrder, gbc_lbl_VisitOrder);
		
		this.cb_VisitOrder = new JComboBox<String>();
		this.cb_VisitOrder.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        cb_VisitOrder(e);
		    }
		});
		this.cb_VisitOrder.setModel(new DefaultComboBoxModel<String>(new String[] {"chronological", "shortest route", "selected order"}));
		GridBagConstraints gbc_cb_VisitOrder = new GridBagConstraints();
		gbc_cb_VisitOrder.anchor = GridBagConstraints.WEST;
		gbc_cb_VisitOrder.insets = new Insets(0, 0, 5, 5);
		gbc_cb_VisitOrder.gridx = 4;
		gbc_cb_VisitOrder.gridy = 4;
		this.contentPane.add(this.cb_VisitOrder, gbc_cb_VisitOrder);
		
		this.lbl_ResizeMethod = new JLabel("Resize images:");
		GridBagConstraints gbc_lbl_ResizeMethod = new GridBagConstraints();
		gbc_lbl_ResizeMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_ResizeMethod.anchor = GridBagConstraints.EAST;
		gbc_lbl_ResizeMethod.gridx = 6;
		gbc_lbl_ResizeMethod.gridy = 4;
		this.contentPane.add(this.lbl_ResizeMethod, gbc_lbl_ResizeMethod);
		
		this.cb_ResizeMethod = new JComboBox<String>();
		this.cb_ResizeMethod.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        cb_ResizeMethod(e);
		    }
		});
		this.cb_ResizeMethod.setModel(new DefaultComboBoxModel<String>(new String[] {"when zooming", "don't resize"}));
		GridBagConstraints gbc_cb_ResizeMethod = new GridBagConstraints();
		gbc_cb_ResizeMethod.anchor = GridBagConstraints.WEST;
		gbc_cb_ResizeMethod.insets = new Insets(0, 0, 5, 0);
		gbc_cb_ResizeMethod.gridx = 7;
		gbc_cb_ResizeMethod.gridy = 4;
		this.contentPane.add(this.cb_ResizeMethod, gbc_cb_ResizeMethod);
		
		this.lbl_ImageSize = new JLabel("Limit image size:");
		GridBagConstraints gbc_lbl_ImageSize = new GridBagConstraints();
		gbc_lbl_ImageSize.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_ImageSize.anchor = GridBagConstraints.EAST;
		gbc_lbl_ImageSize.gridx = 6;
		gbc_lbl_ImageSize.gridy = 5;
		this.contentPane.add(this.lbl_ImageSize, gbc_lbl_ImageSize);
		
		this.cb_ImageSize = new JComboBox<String>();
		this.cb_ImageSize.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        cb_ImageSize(e);
		    }
		});
		this.cb_ImageSize.setModel(new DefaultComboBoxModel<String>(new String[] {"100 px", "200 px", "400 px", "600 px", "800 px", "original"}));
		this.cb_ImageSize.setSelectedIndex(2);
		GridBagConstraints gbc_cb_ImageSize = new GridBagConstraints();
		gbc_cb_ImageSize.insets = new Insets(0, 0, 5, 0);
		gbc_cb_ImageSize.anchor = GridBagConstraints.WEST;
		gbc_cb_ImageSize.gridx = 7;
		gbc_cb_ImageSize.gridy = 5;
		this.contentPane.add(this.cb_ImageSize, gbc_cb_ImageSize);
	}


	public void myInitComponents()
	{
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
		
		
		this.textArea_Log.setEditable(false);
		((DefaultCaret) this.textArea_Log.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
        this.mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
		this.mapKit.setAddressLocationShown(false); // don't show center
		//this.mapKit.setAddressLocation(new GeoPosition(48.74670985863194, 9.105284214019775)); // Uni
	    this.mapKit.setAddressLocation(new GeoPosition(48.89088888888889, 9.225294444444444)); // Home
		this.mapKit.setZoom(1);
        this.mapKit.setMiniMapVisible(false);
	    
        
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
				
                for (OverlayImage oi : overlayImages)
                {
                    oi.draw(g, map);
                }
				
                for (OverlayAggregate oa : overlayLines)
                {
                    oa.draw(g, map);
                }
				
				for (OverlayAggregate oa : persistentOverlayLines)
				{
				    oa.draw(g, map);
				}
				
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
		this.overlayLines.clear();
		this.mapKit.repaint();
		this.currSource = null;
		this.currTarget = null;
	}
		
	
    public void mapMouseClicked(MouseEvent e)
    {
        if (g == null) {
            System.out.println("Error: must load a graph first!");
            return;
        }
        
        // middle mouse button -> clear
        if (SwingUtilities.isMiddleMouseButton(e)) {
            this.clearMap();
            return;
        }
        // right mouse button -> select target, but not without a source
        else if (SwingUtilities.isRightMouseButton(e) && this.currSource == null) {
            System.out.println("Please set a source first");
            return;
        }

        // from here on it's either a left mouse button click (-> select source)
        // or a right mouse button click with a previously selected source (-> select target, calculate route)

        System.out.println();
        GeoPosition clickPos = map.convertPointToGeoPosition(e.getPoint());
        System.out.println("Clicked at: " + clickPos.getLatitude() + ", " + clickPos.getLongitude());

        StopWatch.lap();
        int n = g.getNearestNode(clickPos.getLatitude(), clickPos.getLongitude());
        StopWatch.lap();
        if (n == -1) {
            System.out.println("Found no node!");
            return;
        }
        System.out.println("Closest node at: " + g.getLat(n) + ", " + g.getLon(n) + "  (found in "
                + String.format("%.3f", StopWatch.getLastLapSec()) + " sec)");

        
        if (SwingUtilities.isLeftMouseButton(e)) 
        {
            this.currSource = this.g.getPosition(n);
            this.currTarget = null;
            this.overlayLines.add(OverlayAggregate.route_var3(clickPos, this.currSource));
            d.setSource(n);
        }
        else if (SwingUtilities.isRightMouseButton(e)) 
        {
            this.currTarget = this.g.getPosition(n);
            d.setTarget(n);
            if (this.currSource != null && this.currTarget != null) {
                StopWatch.lap();
                boolean r = d.pathFromTo();
                StopWatch.lap();
                if (r) {
                    this.overlayLines.add(OverlayAggregate.route_multi_var2(d.getRoute()));
                }
                System.out.println("Calculated route in " + String.format("%.3f", StopWatch.getLastLapSec()) + " sec");
            }
        }

        this.mapKit.repaint();
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
            if (visibleNum >= OverlayImage.MAX_CONCURRENTLY_VISIBLE_IMAGES) {
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
            
            // are we close to the oi's Waypoint ?
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
        this.clearMap();
    }
    
    
    public void btn_ClearLast(ActionEvent e)
    {
        this.overlayLines.pollLast();
        this.mapKit.repaint();
        this.currSource = null;
        this.currTarget = null;
    }
    
    
    public void btn_LoadGraph(ActionEvent e)
    {
        JFileChooser fd = new JFileChooser();
        fd.setDialogTitle("Select a graph file");
        fd.setCurrentDirectory(new File("/Users/Julian/Documents/Uni/_Fapra OSM/3/file-generation"));
        fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int c = fd.showOpenDialog(this);
        
        switch (c) {
            case JFileChooser.APPROVE_OPTION:
                File file = fd.getSelectedFile();
                try {
                    this.g = GraphFactory.loadArrayRepresentation(file.getAbsolutePath());
                    this.d = new Dijkstra(this.g);
                    this.clearMap();
                    init();
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
    }
    
	
    public void cb_VisitOrder(ActionEvent e)
    {
        System.err.println("cb_VisitOrder not yet implemented!");
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
        JFileChooser fd = new JFileChooser();
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
                System.out.println("Loading images: " + StopWatch.lapSecStr());
                
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

    
    public void list_Images(ListSelectionEvent e)
    {
        OverlayImage oi = list_Images.getSelectedValue();
        if (oi == null) {
            return;
        }
        else if (list_Images.getValueIsAdjusting()) {
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
            
            //if (ih > (3 * h / 4)) {
            //    p.setLocation(p.getX(), p.getY() - (h/2) + OverlayImage.PADDING);
            //    System.out.println("2");
            //}
            if (oi.getHeightFull() > (h / 2)) {
                p.setLocation(p.getX(), p.getY() - (h/4));
            }
            map.setCenter(p);
        }
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
            this.textArea_Log.setText("");
        }
        this.textArea_Log.append(s);
    }
    
}
