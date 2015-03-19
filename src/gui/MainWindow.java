package gui;


import gui.overlay.OverlayAggregate;
import gui.overlay.OverlayElement;
import gui.overlay.OverlayImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.JButton;

import java.awt.FileDialog;
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
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;



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
	
	private static final int MAX_LOG_LENGTH = 500;
	private int currLines = 0;
	
	private JPanel		contentPane;
	private JXMapKit	mapKit;
	private JScrollPane scrollPane_Log;
	private JTextArea   textArea_Log;
	private JButton		btn_LoadGraph;
	private JButton 	btn_ClearLast;
	private JButton 	btn_ClearAll;
	private JButton btn_AddImages;
	private JButton btn_RemoveImage;
	private JComboBox<String> cb_ResizeMethod;
	private JComboBox<String> cb_ImageSize;
	private JLabel lbl_ResizeMethod;
	private JLabel lbl_ImageSize;
	private JButton btnCalculateRoute;
	private JLabel lbl_VisitOrder;
	private JComboBox<String> cb_VisitOrder;
	private JLabel lbl_ImageQuality;
	private JComboBox<String> cb_ImageQuality;
	private JList list_Images;



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
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(this.contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 0.0,
				0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		this.contentPane.setLayout(gbl_contentPane);

		this.mapKit = new JXMapKit();
		this.mapKit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mapMouseClicked(e);
			}
		});
		GridBagConstraints gbc_mapKit = new GridBagConstraints();
		gbc_mapKit.gridheight = 13;
		gbc_mapKit.gridwidth = 14;
		gbc_mapKit.insets = new Insets(0, 0, 5, 5);
		gbc_mapKit.fill = GridBagConstraints.BOTH;
		gbc_mapKit.gridx = 0;
		gbc_mapKit.gridy = 0;
		this.contentPane.add(this.mapKit, gbc_mapKit);
		
		this.list_Images = new JList();
		GridBagConstraints gbc_list_Images = new GridBagConstraints();
		gbc_list_Images.gridwidth = 3;
		gbc_list_Images.gridheight = 13;
		gbc_list_Images.insets = new Insets(0, 0, 5, 0);
		gbc_list_Images.fill = GridBagConstraints.BOTH;
		gbc_list_Images.gridx = 14;
		gbc_list_Images.gridy = 0;
		this.contentPane.add(this.list_Images, gbc_list_Images);
		
		this.scrollPane_Log = new JScrollPane();
		this.scrollPane_Log.setMinimumSize(new Dimension(200, 100));
		GridBagConstraints gbc_scrollPane_Log = new GridBagConstraints();
		gbc_scrollPane_Log.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_Log.gridheight = 6;
		gbc_scrollPane_Log.gridwidth = 8;
		gbc_scrollPane_Log.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_Log.gridx = 0;
		gbc_scrollPane_Log.gridy = 13;
		this.contentPane.add(this.scrollPane_Log, gbc_scrollPane_Log);
		
		this.textArea_Log = new JTextArea();
		textArea_Log.setFont(new Font("Hasklig", Font.PLAIN, 11));
		this.scrollPane_Log.setViewportView(this.textArea_Log);
		
		this.btn_LoadGraph = new JButton("Load Graph");
		this.btn_LoadGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btn_LoadGraph(e);
			}
		} );
		
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
		gbc_btn_ClearLast.gridx = 8;
		gbc_btn_ClearLast.gridy = 13;
		this.contentPane.add(this.btn_ClearLast, gbc_btn_ClearLast);
		
		this.btnCalculateRoute = new JButton("Calculate route");
		GridBagConstraints gbc_btnCalculateRoute = new GridBagConstraints();
		gbc_btnCalculateRoute.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCalculateRoute.insets = new Insets(0, 0, 5, 5);
		gbc_btnCalculateRoute.gridx = 12;
		gbc_btnCalculateRoute.gridy = 13;
		this.contentPane.add(this.btnCalculateRoute, gbc_btnCalculateRoute);
		
		this.btn_RemoveImage = new JButton("Remove selected image");
		GridBagConstraints gbc_btn_RemoveImage = new GridBagConstraints();
		gbc_btn_RemoveImage.fill = GridBagConstraints.HORIZONTAL;
		gbc_btn_RemoveImage.insets = new Insets(0, 0, 5, 0);
		gbc_btn_RemoveImage.gridx = 16;
		gbc_btn_RemoveImage.gridy = 13;
		this.contentPane.add(this.btn_RemoveImage, gbc_btn_RemoveImage);
		
		this.btn_ClearAll = new JButton("Clear all markers");
		this.btn_ClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btn_ClearAll(e);
			}
		});
		GridBagConstraints gbc_btn_ClearAll = new GridBagConstraints();
		gbc_btn_ClearAll.anchor = GridBagConstraints.WEST;
		gbc_btn_ClearAll.insets = new Insets(0, 0, 5, 5);
		gbc_btn_ClearAll.gridx = 8;
		gbc_btn_ClearAll.gridy = 14;
		this.contentPane.add(this.btn_ClearAll, gbc_btn_ClearAll);
		
		this.btn_AddImages = new JButton("Add images");
		GridBagConstraints gbc_btn_AddImages = new GridBagConstraints();
		gbc_btn_AddImages.fill = GridBagConstraints.HORIZONTAL;
		gbc_btn_AddImages.insets = new Insets(0, 0, 5, 0);
		gbc_btn_AddImages.gridx = 16;
		gbc_btn_AddImages.gridy = 14;
		this.contentPane.add(this.btn_AddImages, gbc_btn_AddImages);
		
		this.lbl_VisitOrder = new JLabel("Visit in order:");
		GridBagConstraints gbc_lbl_VisitOrder = new GridBagConstraints();
		gbc_lbl_VisitOrder.anchor = GridBagConstraints.EAST;
		gbc_lbl_VisitOrder.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_VisitOrder.gridx = 11;
		gbc_lbl_VisitOrder.gridy = 16;
		this.contentPane.add(this.lbl_VisitOrder, gbc_lbl_VisitOrder);
		
		this.cb_VisitOrder = new JComboBox<String>();
		this.cb_VisitOrder.setModel(new DefaultComboBoxModel<String>(new String[] {"chronological", "shortest route", "selected order"}));
		GridBagConstraints gbc_cb_VisitOrder = new GridBagConstraints();
		gbc_cb_VisitOrder.anchor = GridBagConstraints.EAST;
		gbc_cb_VisitOrder.insets = new Insets(0, 0, 5, 5);
		gbc_cb_VisitOrder.gridx = 12;
		gbc_cb_VisitOrder.gridy = 16;
		this.contentPane.add(this.cb_VisitOrder, gbc_cb_VisitOrder);
		
		this.lbl_ResizeMethod = new JLabel("Resize images:");
		GridBagConstraints gbc_lbl_ResizeMethod = new GridBagConstraints();
		gbc_lbl_ResizeMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_ResizeMethod.anchor = GridBagConstraints.EAST;
		gbc_lbl_ResizeMethod.gridx = 15;
		gbc_lbl_ResizeMethod.gridy = 16;
		this.contentPane.add(this.lbl_ResizeMethod, gbc_lbl_ResizeMethod);
		
		this.cb_ResizeMethod = new JComboBox<String>();
		this.cb_ResizeMethod.setModel(new DefaultComboBoxModel<String>(new String[] {"when zooming", "don't resize"}));
		GridBagConstraints gbc_cb_ResizeMethod = new GridBagConstraints();
		gbc_cb_ResizeMethod.anchor = GridBagConstraints.WEST;
		gbc_cb_ResizeMethod.insets = new Insets(0, 0, 5, 0);
		gbc_cb_ResizeMethod.gridx = 16;
		gbc_cb_ResizeMethod.gridy = 16;
		this.contentPane.add(this.cb_ResizeMethod, gbc_cb_ResizeMethod);
		
		this.lbl_ImageSize = new JLabel("Limit image size:");
		GridBagConstraints gbc_lbl_ImageSize = new GridBagConstraints();
		gbc_lbl_ImageSize.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_ImageSize.anchor = GridBagConstraints.EAST;
		gbc_lbl_ImageSize.gridx = 15;
		gbc_lbl_ImageSize.gridy = 17;
		this.contentPane.add(this.lbl_ImageSize, gbc_lbl_ImageSize);
		
		this.cb_ImageSize = new JComboBox<String>();
		this.cb_ImageSize.setModel(new DefaultComboBoxModel<String>(new String[] {"100 px", "200 px", "400 px", "600 px", "800 px", "original"}));
		GridBagConstraints gbc_cb_ImageSize = new GridBagConstraints();
		gbc_cb_ImageSize.insets = new Insets(0, 0, 5, 0);
		gbc_cb_ImageSize.anchor = GridBagConstraints.WEST;
		gbc_cb_ImageSize.gridx = 16;
		gbc_cb_ImageSize.gridy = 17;
		this.contentPane.add(this.cb_ImageSize, gbc_cb_ImageSize);
		GridBagConstraints gbc_btn_LoadGraph = new GridBagConstraints();
		gbc_btn_LoadGraph.anchor = GridBagConstraints.WEST;
		gbc_btn_LoadGraph.insets = new Insets(0, 0, 0, 5);
		gbc_btn_LoadGraph.gridx = 8;
		gbc_btn_LoadGraph.gridy = 18;
		this.contentPane.add(this.btn_LoadGraph, gbc_btn_LoadGraph);
		
		this.lbl_ImageQuality = new JLabel("Image quality:");
		GridBagConstraints gbc_lbl_ImageQuality = new GridBagConstraints();
		gbc_lbl_ImageQuality.anchor = GridBagConstraints.EAST;
		gbc_lbl_ImageQuality.insets = new Insets(0, 0, 0, 5);
		gbc_lbl_ImageQuality.gridx = 15;
		gbc_lbl_ImageQuality.gridy = 18;
		this.contentPane.add(this.lbl_ImageQuality, gbc_lbl_ImageQuality);
		
		this.cb_ImageQuality = new JComboBox<String>();
		this.cb_ImageQuality.setModel(new DefaultComboBoxModel<String>(new String[] {"high", "low"}));
		GridBagConstraints gbc_cb_ImageQuality = new GridBagConstraints();
		gbc_cb_ImageQuality.anchor = GridBagConstraints.WEST;
		gbc_cb_ImageQuality.gridx = 16;
		gbc_cb_ImageQuality.gridy = 18;
		this.contentPane.add(this.cb_ImageQuality, gbc_cb_ImageQuality);
	}


	public void myInitComponents()
	{
		this.mapKit.getMainMap().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	mapMouseClicked(e);
            }
        });
		
		this.mapKit.getMainMap().addMouseMotionListener(new MouseAdapter() {
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
	    
        
        StopWatch.lap();
        FileUtil.loadOverlayImagesFrom("./res", this.overlayImages, 300, false);
        System.out.println("Loading images: " + StopWatch.lapSecStr());
	    
	    
	    waypointPainter = new WaypointPainter<JXMapViewer>();
		overlayPainter = new Painter<JXMapViewer>() {
		    @Override
			public void paint(Graphics2D g, JXMapViewer map, int w, int h)
			{
				g = (Graphics2D) g.create();
				// convert from viewport to world bitmap
				Rectangle rect = mapKit.getMainMap().getViewportBounds();
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
		mapKit.getMainMap().setOverlayPainter(c);      // $hide$ (WindowBuilder doesn't like this line)
		updateWaypoints();
	}
	
	
	public void clearMap()
	{
		this.overlayLines.clear();
		this.mapKit.repaint();
		this.currSource = null;
		this.currTarget = null;
	}
	
	
	public void btn_LoadGraph(ActionEvent e)
	{
		FileDialog fd = new FileDialog(this, "Choose a graph", FileDialog.LOAD);
		fd.setDirectory("");
		fd.setVisible(true);
		File[] fs = fd.getFiles();
		
		if (fs != null && fs.length > 0) {
			try {
				this.g = GraphFactory.loadArrayRepresentation(fs[0].getAbsolutePath());
				this.d = new Dijkstra(this.g);
				this.clearMap();
		        init();
			}
			catch (InvalidGraphFormatException ex) {
				System.out.println("Supplied graph has invalid format");
			}
			catch (IOException ex) {
				System.out.println("Error reading graph");
			}
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
	
	
	public void mapMouseMoved(MouseEvent e)
	{
	    JXMapViewer map = this.mapKit.getMainMap();
	    int zoom = map.getZoom();
	    GeoPosition pos = null;
	    boolean change = false;
	    int visibleNum = 0;
	    
        for (OverlayImage oi : overlayImages) 
        {
            // there's already one visible image, imply invisibility for the others
            if (visibleNum >= OverlayImage.MAX_CONCURRENTLY_VISIBLE_IMAGES) {
                if (oi.isVisible()) {
                    oi.setVisible(false);
                    change = true;
                }
                continue;
            }
            
            pos = oi.getPosition();
            if (pos == null) {
                //System.out.println("null");
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
                    change = true;
                }
                ++visibleNum;
            }
            else {
                // this image is not in range but visible
                if (oi.isVisible()) {
                    oi.setVisible(false);
                    change = true;
                }
            }
        }

        if (change) {
            this.repaint();
        }
	}
	
	
    public void mapMouseClicked(MouseEvent e)
    {
        if (g == null)
            return;
        
        // middle mouse button -> clear
        if (SwingUtilities.isMiddleMouseButton(e)) {
            this.clearMap();
            return;
        }
        // right mouse button -> select target, but not without a source
        else if (SwingUtilities.isRightMouseButton(e) && this.currSource == null) {
            System.out.println("Plese set a source first");
            return;
        }

        // from here on it's either a left mouse button click (-> select source)
        // or a right mouse button click with a previously selected source (-> select target, calculate route)
        
        GeoPosition clickPos = mapKit.getMainMap().convertPointToGeoPosition(e.getPoint());
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

        System.out.println();
        this.mapKit.repaint();
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
