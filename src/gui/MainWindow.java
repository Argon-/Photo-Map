package gui;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.JButton;

import java.awt.BasicStroke;
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
import org.jdesktop.swingx.painter.Painter;

import path.search.Dijkstra;
import util.OverlayAggregate;
import util.OverlayElement;
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
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;



public class MainWindow extends JFrame
{
	private static final long	serialVersionUID	= -590468540732816556L;
	
	private ArrayRepresentation g = null;
	private Dijkstra d = null;
	
	static public final LinkedBlockingDeque<OverlayAggregate> overlayLines = new LinkedBlockingDeque<OverlayAggregate>();
	static public final LinkedBlockingDeque<OverlayAggregate> persistentOverlayLines = new LinkedBlockingDeque<OverlayAggregate>();

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
			//this.g = GraphFactory.loadArrayRepresentation("./15000K.bin");
			this.d = new Dijkstra(this.g);
			//g.drawCells();
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
		this.mapKit.getMainMap().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	mapMouseClicked(e);
            }
        });
		
		this.textArea.setEditable(false);
		((DefaultCaret) this.textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		this.mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
		// set the initial view on the map
		this.mapKit.setAddressLocationShown(false); // don't show center
		this.mapKit.setAddressLocation(new GeoPosition(48.74670985863194, 9.105284214019775)); // Uni
		this.mapKit.setZoom(1); // set zoom level
				
		
		Painter<JXMapViewer> lineOverlay = new Painter<JXMapViewer>() {

			public void paint(Graphics2D g, JXMapViewer map, int w, int h)
			{
				g = (Graphics2D) g.create();
				// convert from viewport to world bitmap
				Rectangle rect = mapKit.getMainMap().getViewportBounds();
				g.translate(-rect.x, -rect.y);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				for (OverlayAggregate oa : persistentOverlayLines)
				{
					for (OverlayElement oe : oa.getLines())
					{
						Point2D s = mapKit.getMainMap().getTileFactory().geoToPixel(oe.getSource(), mapKit.getMainMap().getZoom());
						Point2D t = mapKit.getMainMap().getTileFactory().geoToPixel(oe.getTarget(), mapKit.getMainMap().getZoom());
							
						g.setColor(oe.getColor());
						g.setStroke(new BasicStroke(oe.getWidth()));
						g.drawLine((int) s.getX(), (int) s.getY(), (int) t.getX(), (int) t.getY());
					}
					for (OverlayElement oe : oa.getPoints())
					{
						Point2D s = mapKit.getMainMap().getTileFactory().geoToPixel(oe.getSource(), mapKit.getMainMap().getZoom());
						Point2D t = mapKit.getMainMap().getTileFactory().geoToPixel(oe.getTarget(), mapKit.getMainMap().getZoom());
							
						g.setColor(oe.getColor());
						g.setStroke(new BasicStroke(oe.getWidth()));
						g.drawLine((int) s.getX(), (int) s.getY(), (int) t.getX(), (int) t.getY());
					}
				}
				
				for (OverlayAggregate oa : overlayLines)
				{
					for (OverlayElement oe : oa.getLines())
					{
						Point2D s = mapKit.getMainMap().getTileFactory().geoToPixel(oe.getSource(), mapKit.getMainMap().getZoom());
						Point2D t = mapKit.getMainMap().getTileFactory().geoToPixel(oe.getTarget(), mapKit.getMainMap().getZoom());
							
						g.setColor(oe.getColor());
						g.setStroke(new BasicStroke(oe.getWidth()));
						g.drawLine((int) s.getX(), (int) s.getY(), (int) t.getX(), (int) t.getY());
					}
					for (OverlayElement oe : oa.getPoints())
					{
						Point2D s = mapKit.getMainMap().getTileFactory().geoToPixel(oe.getSource(), mapKit.getMainMap().getZoom());
						Point2D t = mapKit.getMainMap().getTileFactory().geoToPixel(oe.getTarget(), mapKit.getMainMap().getZoom());
							
						g.setColor(oe.getColor());
						g.setStroke(new BasicStroke(oe.getWidth()));
						g.drawLine((int) s.getX(), (int) s.getY(), (int) t.getX(), (int) t.getY());
					}
				}


				g.dispose();
			}
		};
		mapKit.getMainMap().setOverlayPainter(lineOverlay);
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
	
	
	public void mapMouseClicked(MouseEvent e)
	{
		if (SwingUtilities.isMiddleMouseButton(e))
		{
			this.clearMap();
			return;
		}
		else if (SwingUtilities.isRightMouseButton(e) && this.currSource == null)
		{
			System.out.println("Plese set a source first");
			return;
		}
		
		GeoPosition clickPos = mapKit.getMainMap().convertPointToGeoPosition(e.getPoint());
		System.out.println("Clicked at:      " + clickPos.getLatitude() + ", " + clickPos.getLongitude());
		
		StopWatch.lap();
		int n = g.getNearestNode(clickPos.getLatitude(), clickPos.getLongitude());
		StopWatch.lap();
		if (n == -1)
		{
			System.out.println("Found no node!");
			return;
		}
		System.out.println("Nearest node at: " + g.getLat(n) + ", " + g.getLon(n) + "                              in " + String.format("%.9f", StopWatch.getLastLapSec()) + " sec");
		
		
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
				System.out.println("Calculated route in " + String.format("%.9f", StopWatch.getLastLapSec()) + " sec");
			}
		}
		
		System.out.println();
		this.mapKit.repaint();
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
