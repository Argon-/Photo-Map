package gui.main_window;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import javax.swing.JButton;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.mapviewer.GeoPosition;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JScrollPane;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextArea;
import java.awt.Font;



public class MainWindow extends JFrame
{
	private static final long	serialVersionUID	= -590468540732816556L;
	
	private JPanel		contentPane;
	private JButton		btnNewButton;
	private JXMapKit	mapKit;
	private JScrollPane scrollPane;
	private JTextArea textArea;



	/**
	 * Create the frame.
	 */
	public MainWindow()
	{
		initComponents();
		myInitComponents();
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
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 9.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
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
		
		this.scrollPane = new JScrollPane();
		this.scrollPane.setMinimumSize(new Dimension(200, 100));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 4;
		gbc_scrollPane.gridwidth = 19;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 13;
		this.contentPane.add(this.scrollPane, gbc_scrollPane);
		
		this.textArea = new JTextArea();
		textArea.setFont(new Font("Hasklig", Font.PLAIN, 11));
		this.scrollPane.setViewportView(this.textArea);
		
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
		gbc_btnNewButton.gridy = 14;
		this.contentPane.add(this.btnNewButton, gbc_btnNewButton);
	}


	public void myInitComponents()
	{
		this.mapKit.getMainMap().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	mapMouseClicked(e);
            }
        });
		
		
		this.mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps);
		// set the initial view on the map
		this.mapKit.setAddressLocationShown(false); // don't show center
		this.mapKit.setAddressLocation(new GeoPosition(48.74670985863194, 9.105284214019775)); // Uni
		this.mapKit.setZoom(1); // set zoom level
	}
	
	
	public void btnLoadGraph(ActionEvent e)
	{
		
	}
	
	
	public void mapMouseClicked(MouseEvent e)
	{
		if (SwingUtilities.isLeftMouseButton(e)) {
			System.out.println("left");
		}
		else if (SwingUtilities.isRightMouseButton(e)) {
			System.out.println("right");
		}
		else {
			System.out.println("unrecognized click, ignoring");
			return;
		}
		
		GeoPosition pos = mapKit.getMainMap().convertPointToGeoPosition(e.getPoint());
		
	}
	
	
	public void log(String s)
	{
		this.textArea.append(s);
	}

}
