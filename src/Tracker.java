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

public class Tracker extends JFrame {
	
	// Graphical Variables
	JMenuBar menuBar = new JMenuBar();
	JMenuItem rotatorItem = new JMenuItem("Connect to Rotator");
	JMenuItem inputStreamItem = new JMenuItem("Connect to Input Data Stream");
	JMenuItem setLocationItem = new JMenuItem("Set Current Location");
	JMenuItem aboutItem = new JMenuItem("About");
	JMenuItem exitItem = new JMenuItem("Exit");
	JMenu fileMenu = new JMenu("File");
	JMenu helpMenu = new JMenu("Help");
	Container c;
	
	JPanel centerPanel = new JPanel(new GridLayout());			// A panel to hold the two frames in the center section
	StatusBar status;											// The status bar at the bottom of the screen
	RotatorFrame rotorFrame;									// The frame to hold the rotator panels
	DataInputFrame inputFrame;									// The frame to hold the data input streams
	
	// Action listeners to add new inputs and rotators
	ActionListener addRotator = new ActionListener() {public void actionPerformed(ActionEvent e){addRotator();}};
	ActionListener addInputStream = new ActionListener() {public void actionPerformed(ActionEvent e){addInputStream();}};

	// Location data
	Location currLoc = new Location();	// The location of the antenna system
	Location baloonLoc = new Location();	// The location of the balloon
	

	/**
	 * Constructor for the main program window
	 */
	Tracker() {
		super("HAB Tracker");		// Create the window
		buildMenu();				// Add the menus
		
		// Format the overall structure of the window
		c = getContentPane();
		setSize(700,500);
		setExtendedState(Frame.MAXIMIZED_BOTH);  
		c.setLayout(new BorderLayout());

		// Create the status bar at the bottom of the screen
		status = new StatusBar(c.getWidth());
		c.add(status, BorderLayout.PAGE_END);

		// Create the main rotor frame
		rotorFrame = new RotatorFrame(c.getWidth(),c.getHeight());
		GridBagConstraints rotorConstraints = new GridBagConstraints();
		rotorConstraints.fill = GridBagConstraints.HORIZONTAL;
		rotorConstraints.gridx = 0;
		rotorConstraints.gridy = 0;
		centerPanel.add(rotorFrame, rotorConstraints);
		
		// Create the main data input stream frame
		inputFrame = new DataInputFrame(c.getWidth(), c.getHeight());
		GridBagConstraints inputConstraints = new GridBagConstraints();
		inputConstraints.fill = GridBagConstraints.HORIZONTAL;
		inputConstraints.gridx = 0;
		inputConstraints.gridy = 0;
		centerPanel.add(inputFrame, inputConstraints);
		
		// Add the panel to the center
		c.add(centerPanel);
			
		// if the window is being closed, call our exit function
		addWindowListener(new WindowAdapter()
		{public void windowClosing(WindowEvent evt){exit();}});

		// Display the window
		setVisible(true);
		setLocationRelativeTo(null);
	}
	
	/**
	 * The main program execution function
	 * 
	 * @param args	The arguements to pass to the program
	 */
	static public void main(String[] args) {
		// Create an instance of the main graphical class
		Tracker tracker = new Tracker();
	}
	
	/**
	 * Create the menu bar
	 */
	private void buildMenu()
	{
		// Item to allow for data stream creation
		inputStreamItem.addActionListener(addInputStream);
		fileMenu.add(inputStreamItem);

		// Item to allow for rotator item creation
		rotatorItem.addActionListener(addRotator);
		fileMenu.add(rotatorItem);
		
		// Item to allow for the geographical location to be set
		setLocationItem.addActionListener(new ActionListener()
		{public void actionPerformed(ActionEvent e){setLocation();}});
		fileMenu.add(setLocationItem);
		
		// The function to call upon program termination
		exitItem.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e){
					exit();
				}
			}
		);
		fileMenu.add(exitItem);
		
		
		// Create the help menu
		helpMenu.add(aboutItem);
		aboutItem.addActionListener(new ActionListener()
		{public void actionPerformed(ActionEvent e){about();}});

		// Add both menus to the menu bar and display it
		menuBar.add(fileMenu);
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
		currLoc.Latitude = Double.parseDouble(latitude.getText());
		currLoc.Longitude = Double.parseDouble(longitude.getText());
		currLoc.Elevation = Double.parseDouble(elevation.getText());
	}
	
	/**
	 * Add a new input stream to the program
	 */
	private void addInputStream() {
		// A text box to get the mane of the new data stream
		JTextField name = new JTextField();
		name.setText("");
		
		// Create an array of all the components which need to go in the dialog box
		final JComponent[] inputs = new JComponent[] {
				new JLabel("Input Name:"),
				name
		};
		
		// Detect if the user cancelled out of the box, and if they did then don't create the data input
		if (0 != JOptionPane.showConfirmDialog(null, inputs, "Name the new data input", JOptionPane.OK_CANCEL_OPTION))
			return;
		
		
		// Add the data input
		c = getContentPane();
		inputFrame.addDataInput(c.getWidth(), c.getHeight(), name.getText());
	}
	
	
	/**
	 * Add a new rotator object to the program
	 */
	private void addRotator() {
		// A text box to get the mane of the new rotator
		JTextField name = new JTextField();
		name.setText("");
		
		// Create an array of all the components which need to go in the dialog box
		final JComponent[] inputs = new JComponent[] {
				new JLabel("Rotator Name:"),
				name
		};
		
		// Detect if the user cancelled out of the box, and if they did then don't create the rotator
		if (0 != JOptionPane.showConfirmDialog(null, inputs, "Name the new rotator", JOptionPane.OK_CANCEL_OPTION))
			return;
		
		// Add the rotator
		c = getContentPane();
		rotorFrame.addRotator(c.getWidth(), c.getHeight(), name.getText());
	}
	
	/**
	 * Display the about box
	 */
	private void about() {
		new AboutBox(this).setVisible(true);
	}
	
};
