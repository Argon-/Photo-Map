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
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingDeque;



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
	
	private final int maxLines = 1000;
	private int currLines = 0;
	
	private JPanel		contentPane;
	private JXMapKit	mapKit;
	private JScrollPane scrollPane;
	private JTextArea   textArea;
	private JButton		btnNewButton;
	private JButton 	btnClearLast;
	private JButton 	btnClearAll;



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
		setBounds(100, 100, 1000, 900);
		this.contentPane = new JPanel();
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(this.contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 9.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
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
		gbc_mapKit.gridwidth = 20;
		gbc_mapKit.insets = new Insets(0, 0, 5, 0);
		gbc_mapKit.fill = GridBagConstraints.BOTH;
		gbc_mapKit.gridx = 0;
		gbc_mapKit.gridy = 0;
		this.contentPane.add(this.mapKit, gbc_mapKit);
		
		this.btnClearLast = new JButton("Clear last");
		this.btnClearLast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnClearLast(e);
			}
		});
		this.btnClearLast.setActionCommand("Clear last");
		GridBagConstraints gbc_btnClearLast = new GridBagConstraints();
		gbc_btnClearLast.anchor = GridBagConstraints.WEST;
		gbc_btnClearLast.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearLast.gridx = 0;
		gbc_btnClearLast.gridy = 13;
		this.contentPane.add(this.btnClearLast, gbc_btnClearLast);
		
		this.scrollPane = new JScrollPane();
		this.scrollPane.setMinimumSize(new Dimension(200, 100));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridheight = 5;
		gbc_scrollPane.gridwidth = 19;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 13;
		this.contentPane.add(this.scrollPane, gbc_scrollPane);
		
		this.textArea = new JTextArea();
		textArea.setFont(new Font("Hasklig", Font.PLAIN, 11));
		this.scrollPane.setViewportView(this.textArea);
		
		this.btnClearAll = new JButton("Clear all");
		this.btnClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnClearAll(e);
			}
		});
		GridBagConstraints gbc_btnClearAll = new GridBagConstraints();
		gbc_btnClearAll.anchor = GridBagConstraints.WEST;
		gbc_btnClearAll.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearAll.gridx = 0;
		gbc_btnClearAll.gridy = 14;
		this.contentPane.add(this.btnClearAll, gbc_btnClearAll);
		
		this.btnNewButton = new JButton("Load Graph");
		this.btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnLoadGraph(e);
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.WEST;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 17;
		this.contentPane.add(this.btnNewButton, gbc_btnNewButton);
	}


	public void myInitComponents()
	{
	    // $hide>>$
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
		
		
		this.textArea.setEditable(false);
		((DefaultCaret) this.textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		this.mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
		this.mapKit.setAddressLocationShown(false); // don't show center
		//this.mapKit.setAddressLocation(new GeoPosition(48.74670985863194, 9.105284214019775)); // Uni
	    this.mapKit.setAddressLocation(new GeoPosition(48.89088888888889, 9.225294444444444)); // Home
		this.mapKit.setZoom(1);
        this.mapKit.setMiniMapVisible(false);
        
		
		//String f = "/Users/Julian/Desktop/aldnoah3.png";
	    //String f = "/Users/Julian/Documents/Dropbox/Kamera-Uploads/2015-03-15 20.58.39.jpg";
	    
	    //this.overlayImages.add(new OverlayImage("/Users/Julian/Documents/Dropbox/Kamera-Uploads/2015-03-15 20.58.39.jpg")
        //    .maxSize(200));
	    //this.overlayImages.add(new OverlayImage("/Users/Julian/Documents/Dropbox/Kamera-Uploads/2015-03-16 22.04.51.jpg")
        //    .maxSize(200));
	    
        
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
		mapKit.getMainMap().setOverlayPainter(c);
		updateWaypoints();
		// $hide<<$
	}
	
	
	public void clearMap()
	{
		this.overlayLines.clear();
		this.mapKit.repaint();
		this.currSource = null;
		this.currTarget = null;
	}
	
	
	public void btnLoadGraph(ActionEvent e)
	{
		FileDialog fd = new FileDialog(this, "Choose a graph", FileDialog.LOAD);
		fd.setDirectory("");
		fd.setVisible(true);
		String f = fd.getFile();
		
		if (f != null) {
			try {
				this.g = GraphFactory.loadArrayRepresentation(f);
				this.d = new Dijkstra(this.g);
				this.clearMap();
			}
			catch (InvalidGraphFormatException ex) {
				System.out.println("Supplied graph has invalid format");
			}
			catch (IOException ex) {
				System.out.println("Error reading graph");
			}
		}
	}
	
	
	public void btnClearAll(ActionEvent e)
	{
		this.clearMap();
	}
	
	
	public void btnClearLast(ActionEvent e)
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
        System.out.println("Clicked at:      " + clickPos.getLatitude() + ", " + clickPos.getLongitude());

        StopWatch.lap();
        int n = g.getNearestNode(clickPos.getLatitude(), clickPos.getLongitude());
        StopWatch.lap();
        if (n == -1) {
            System.out.println("Found no node!");
            return;
        }
        System.out.println("Nearest node at: " + g.getLat(n) + ", " + g.getLon(n) + "  (found in "
                + String.format("%.6f", StopWatch.getLastLapSec()) + " sec)");

        
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
                System.out.println("Calculated route in " + String.format("%.6f", StopWatch.getLastLapSec()) + " sec");
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
		if (currLines++ > maxLines) {
			currLines = 1;
			this.textArea.setText("");
		}
		this.textArea.append(s);
	}

}
