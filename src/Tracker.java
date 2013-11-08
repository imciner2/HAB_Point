/**
 * The main class for the Tracker program
 * @author Ian
 *
 */

import geoUtils.*;
import gui.*;
import java.awt.*;
import java.awt.event.*;
import java.io.UnsupportedEncodingException;
import java.nio.*;
import java.nio.charset.*;
import java.util.EventObject;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;

import dataInputFramework.GPSDataLocation;
import dataInputs.DataInput;
import dataInputs.SerialStream;

import packetEngine.*;
import rotatorFramework.*;
import rotators.*;

public class Tracker extends JFrame implements PacketUser{
	
	// Graphical Variables
	JMenuBar menuBar = new JMenuBar();
	JMenuItem startEngineItem = new JMenuItem("Connect to AGWPE");
	JMenuItem rotatorItem = new JMenuItem("Connect to Rotator");
	JMenuItem dataStreamItem = new JMenuItem("Connect to Input Data Stream");
	JMenuItem setLocationItem = new JMenuItem("Set Current Location");
	JMenuItem aboutItem = new JMenuItem("About");
	JMenuItem exitItem = new JMenuItem("Exit");
	JMenu fileMenu = new JMenu("File");
	JMenu helpMenu = new JMenu("Help");
	Container c;
	
	StatusBar status;	// The status bar at the bottom of the screen
	RotatorFrame rotorFrame;	// The frame to hold the rotator panels
	ActionListener addRotator = new ActionListener() {public void actionPerformed(ActionEvent e){addRotator();}};
	ActionListener startDataItem = new ActionListener() {public void actionPerformed(ActionEvent e){startDataStream();}};
	ActionListener stopDataItem = new ActionListener() {public void actionPerformed(ActionEvent e){stopDataStream();}};
	Rotator rotor;	// The object to store the rotator
	DataInput dataStream;

	java.util.Timer dataStreamTimer;
	
	// Location data
	Location currLoc = new Location();	// The location of the antenna system
	Location baloonLoc = new Location();	// The location of the balloon
	
	// teminal stuff
	Controller controller;
	PacketTransport remote;			// NIO support
	String server_address="127.0.0.1";		// Default host is the localhost
	int server_port=8000;		// Default server port
	int sends = 1;
	public boolean readOn = true;
	int state = Packet.CLOSED;		// Start disconnected
	boolean running = false;
	Charset charset;
	CharsetEncoder encoder;
	CharsetDecoder decoder;
	StringBuffer recvBuffer;

	/**
	 * Constructor for the program
	 */
	Tracker() {
		super("HABET Tracker");		// Create the window
		buildMenu();	// Add the menus
		
		c = getContentPane();
		setSize(700,500);
		setExtendedState(Frame.MAXIMIZED_BOTH);  
		c.setLayout(new BorderLayout());
		
		status = new StatusBar(c.getWidth());
		c.add(status, BorderLayout.SOUTH);

		rotorFrame = new RotatorFrame(c.getWidth(),c.getHeight());
		//c.add(rotorPanel, BorderLayout.WEST);
		c.add(rotorFrame);
				
		//c.add(new JSeparator(SwingConstants.VERTICAL));
		
		// if the window is being closed, call our exit function
		addWindowListener(new WindowAdapter()
		{public void windowClosing(WindowEvent evt){exit();}});

		setVisible(true);
		setLocationRelativeTo(null);
	}
	
	/**
	 * The main program execution function
	 * 
	 * @param args	The arguements to pass to the program
	 */
	static public void main(String[] args) {
		Tracker tracker = new Tracker();
	}
	
	/**
	 * Create the menu bar
	 */
	private void buildMenu()
	{
		JMenuItem item;

		// file menu
		dataStreamItem.addActionListener(new ActionListener()
		{public void actionPerformed(ActionEvent e){startDataStream();}});
		fileMenu.add(dataStreamItem);

		// Default to having start the rotator object
		rotatorItem.addActionListener(addRotator);
		fileMenu.add(rotatorItem);
		
		setLocationItem.addActionListener(new ActionListener()
		{public void actionPerformed(ActionEvent e){setLocation();}});
		fileMenu.add(setLocationItem);
		
		exitItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e){
						exit();
					}
				}
		);
		
		fileMenu.add(exitItem);
		menuBar.add(fileMenu);


		helpMenu.add(aboutItem);
		aboutItem.addActionListener(new ActionListener()
		{public void actionPerformed(ActionEvent e){about();}});

		menuBar.add(helpMenu);

		setJMenuBar(menuBar);
	}
	
	/**
	 * Function to stop all tasks and exit the program
	 */
	public void exit() {
		System.exit(0);
	}
	
	/**
	 * Action listener to set the current location
	 */
	private void setLocation() {
		// Create a text field to take the latitude input
		JTextField latitude = new JTextField();
		latitude.setText(""+currLoc.Latitude);
		
		// Create a text field to take the longitude input
		JTextField longitude = new JTextField();
		longitude.setText(""+currLoc.Longitude);
		
		// Create a text field to take the elevation input
		JTextField elevation = new JTextField();
		elevation.setText(""+currLoc.Elevation);
		
		// Combine all the of the inputs onto one window
		final JComponent[] inputs = new JComponent[] {
				new JLabel("Latitude:"),
				latitude,
				new JLabel("Longitude:"),
				longitude,
				new JLabel("Elevation (Meters):"),
				elevation
		};
		JOptionPane.showMessageDialog(null, inputs, "Set Current Location", JOptionPane.PLAIN_MESSAGE);
		
		// Parse the gathered data
		currLoc.Latitude = Integer.parseInt(latitude.getText());
		currLoc.Longitude = Integer.parseInt(longitude.getText());
		currLoc.Elevation = Integer.parseInt(elevation.getText());
	}
	
	/**
	 * Action Listener to handle AGWPE connection information
	 */
	private void startPacketEngine() {
		switch(state)
		{
			case Packet.CLOSED:
				// Start a connection with the server
				
				// Create a text field to take the server address
				JTextField address = new JTextField();
				address.setText(server_address);
				
				// Create a text field to take the server port
				JTextField port = new JTextField();
				port.setText(""+server_port);
				
				// Package the pieces into one window
				final JComponent[] inputs = new JComponent[] {
						new JLabel("Server Address:"),
						address,
						new JLabel("Server Port"),
						port
				};
				if (0 != JOptionPane.showConfirmDialog(null, inputs, "Connect to AGWPE", JOptionPane.OK_CANCEL_OPTION))
					return;
				
				// Parse the returned data
				server_address = address.getText();
				server_port = Integer.parseInt(port.getText());
				
				// Try the connection
				remote = new PacketTransport(controller);
				if(remote.connect(this,server_address,server_port))
					setSockState(Packet.OPENING);
				else remote = null;
				break;
			case Packet.OPENED:
			case Packet.OPENING:
			case Packet.CLOSING:
				// Disconnect from the server
				if(remote != null)remote.disconnect();
				break;
		}
	}
	
	/**
	 * Function to change the status information when changing the status of the packet connection
	 * 
	 * @param s	The status to change to
	 */
	private void setSockState (int s)
	{
		if(state != s)
		{
			state = s;
			switch(state)
			{
				case Packet.OPENED:
					startEngineItem.setText("Disconnect from AGWPE");
					status.SetPacketStatus("Connected to "+server_address);
					break;
				case Packet.CLOSED:
					startEngineItem.setText("Connect to AGWPE");
					status.SetPacketStatus("Disconnected");
					remote = null;
					if(!running)System.exit(0);
					break;
				case Packet.OPENING:
					status.SetPacketStatus("Connecting to "+server_address);
					startEngineItem.setText("Abort");
					break;

				case Packet.CLOSING:
					status.SetPacketStatus("Disconnecting from "+server_address);
					startEngineItem.setText("Abort");
					break;
			}
		}
	}
	
	private void startDataStream() {
		
		JRadioButton serialButton = new JRadioButton("Serial Data Stream");
		serialButton.setSelected(true);
		JRadioButton agwpeButton = new JRadioButton("AGWPE Data Stream");
		agwpeButton.setEnabled(false);
		ButtonGroup dataStreamButtons = new ButtonGroup();
		dataStreamButtons.add(serialButton);
		dataStreamButtons.add(agwpeButton);
		
		JComponent[] firstDialog = new JComponent[] {
				new JLabel("Data Stream Source:"),
				serialButton,
				agwpeButton
		};
		if (0 != JOptionPane.showConfirmDialog(null, firstDialog, "Connect to Input Data Stream", JOptionPane.OK_CANCEL_OPTION))
			return;
		
		if(serialButton.isSelected()) {
			JTextField port = new JTextField();
			port.setText("COM");
			JTextField longStart = new JTextField();
			longStart.setText("");
			JTextField longStop = new JTextField();
			longStop.setText("");
			JTextField latStart = new JTextField();
			latStart.setText("");
			JTextField latStop = new JTextField();
			latStop.setText("");
			JTextField elevStart = new JTextField();
			elevStart.setText("");
			JTextField elevStop = new JTextField();
			elevStop.setText("");
			final JComponent[] secondDialog = new JComponent[] {
					new JLabel("Serial Port:"),
					port,
					new JLabel("Longitude Start Character:"),
					longStart,
					new JLabel("Longitude Stop Character:"),
					longStop,
					new JLabel("Latitude Start Character:"),
					latStart,
					new JLabel("Latitude Stop Character:"),
					latStop,
					new JLabel("Elevation Start Character:"),
					elevStart,
					new JLabel("Elevation Stop Character:"),
					elevStop
			};
			if (0 != JOptionPane.showConfirmDialog(null, secondDialog, "Set Serial GPS Stream Parameters", JOptionPane.OK_CANCEL_OPTION))
				return;
			GPSDataLocation locs = new GPSDataLocation(Integer.getInteger(latStart.getText()),
													   Integer.getInteger(latStop.getText()),
													   Integer.getInteger(longStart.getText()),
													   Integer.getInteger(longStop.getText()),
													   Integer.getInteger(elevStart.getText()),
													   Integer.getInteger(elevStop.getText()));
			dataStream = new SerialStream(port.getText(), locs);
		} else if(agwpeButton.isSelected()) {
			
		}
		
		dataStreamItem.removeActionListener(startDataItem);
		dataStreamItem.addActionListener(stopDataItem);
		dataStreamItem.setText("Disconnect from Input Data Stream");
	}
	
	private void stopDataStream() {
		dataStreamTimer.cancel();
		dataStream.disconnect();
		dataStreamItem.setText("Connect to Input Data Stream");
	}
	
	private void addRotator() {
		JTextField name = new JTextField();
		name.setText("");
		
		final JComponent[] inputs = new JComponent[] {
				new JLabel("Rotator Name:"),
				name
		};
		if (0 != JOptionPane.showConfirmDialog(null, inputs, "Name the new rotator", JOptionPane.OK_CANCEL_OPTION))
			return;
		
		c = getContentPane();
		rotorFrame.addRotator(c.getWidth(), c.getHeight(), name.getText());
	}
	
	private void about() {
		new AboutBox(this).setVisible(true);
	}

	// We do not send packets over AGWPE
	public void postPacket(Packet pkt) {}

	/**
	 * Work with the received information from the packet class
	 */
	public void runPacket(Packet pkt) {
		int type = pkt.getType();
		
		switch(type)
		{
			case Packet.STATE:
				// Change the socket state
				int s = ((Integer)pkt.getArg()).intValue();
				setSockState(s);
				break;
			case Packet.RECEIVE:
				// Received a data packet
				receive(pkt);  
				break;
			case Packet.FLOW:
				// Do not care about this
				break;
			default:
				// A packet which is not valid
				status.setInformationStatus("Unexpected packet of type="+type);
				break;	
		}
	}
	
	/**
	 * Function to call when decoding a received data packet
	 * 
	 * @param pkt	The received packet
	 */
	private void receive(Packet pkt) {
		// Move the header into its buffer
		ByteBuffer hbuffer = pkt.getHeader();
		int hlen = hbuffer.limit();
		byte[] head = new byte[hlen];
		hbuffer.get(head,0,hlen);
		// Convert the bytes into ASCII and check to see if an exception is thrown
		try {
			String headStr = new String(head, "US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			status.setInformationStatus("Error: Your computer doesn't support ASCII");
		}	

		// Move the data into its buffer
		ByteBuffer buffer = pkt.getData();
		int dlen = buffer.limit();
		byte[] dat = new byte[dlen];
		buffer.get(dat,0,dlen);
		// Convert the bytes into ASCII and check to see if an exception if thrown
		try {
			String dataStr = new String(dat, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			status.setInformationStatus("Error: Your computer doesn't support ASCII");
		}	
	}
}