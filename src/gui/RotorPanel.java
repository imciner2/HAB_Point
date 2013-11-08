package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import rotatorFramework.*;
import rotatorFramework.Orientation;
import rotators.*;

import eu.hansolo.steelseries.extras.WindDirection;
import eu.hansolo.steelseries.gauges.*;
import eu.hansolo.steelseries.tools.*;
//import eu.hansolo.steelseries.extras.*;

public class RotorPanel extends JPanel implements CurrentOrientationListener{
	
	private List listeners = new ArrayList();
	
	String name;		// The name of the rotator panel
	Rotator rotor;		// The rotator object which this panel is referencing
	
	// Labels for the manual rotor control text boxes
	JLabel elevLabel = new JLabel("Elevation:");
	JLabel aziLabel = new JLabel("Azimuth:");
	
	// Variables used to create the dialog box to connect to a rotator
	JLabel typeLabel = new JLabel("Rotator Type: ");
	JLabel connectionLabel = new JLabel("  Location: "); 
	JComponent connectPane = new JPanel();					// Panel to contain the connection controls
	JComboBox<String> rotorTypes = new JComboBox();			// Combo box to allow selection of a rotator type
	JTextField connectionString = new JTextField(10);		// Text box to allow entry of the rotator connection information
	JButton connect = new JButton("Connect");				// Button to begin connection to the rotator
	ActionListener rotorTypeAction;
	ActionListener connectAction;
	ActionListener disconnectAction;
		
	JComponent aziManPane = new JPanel();					// Panel to contain the manual azimuth controls
	JComponent elevManPane = new JPanel();					// Panel to contain the manual elevation controls
	JTextField elevMan = new JTextField(20);				// Manual elevation control input
	JTextField aziMan = new JTextField(20);					// Manual azimuth control input
	JCheckBox manOver = new JCheckBox("Manual Override");	// Checkbox to select override
	ItemListener overAction;								// Listener to handle the change in override state
	ActionListener enterAction;								// Listener to handle the enter key press in the text boxes
	Radial1Square elevKnob = new Radial1Square();
	AzimuthDial aziKnob = new AzimuthDial();
	
	public RotorPanel(int windowWidth, int windowHeight, String name) {
		// Create the panel for the bar
		super();
		this.name = name;
		
		// Set it to a beveled border
		setBorder(new BevelBorder(BevelBorder.LOWERED));
		setPreferredSize(new Dimension(windowWidth/32,windowHeight));

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// Configure the Azimuth dial
		aziKnob.setPointer2Visible(false);
		aziKnob.setPointer2Type(PointerType.TYPE1);
		aziKnob.setLedVisible(false);
		aziKnob.setTitle("Azimuth");
		aziKnob.setUnitString("");
		aziKnob.setSize(100,100);
		aziKnob.setValueCoupled(true);
		setAzimuthGauge(300);
		
		// Configure the elevation dial
		elevKnob.setTrackStart(0);	// Configure the track sections because otherwise setting max value fails
		elevKnob.setTrackStop(90);
		elevKnob.setMaxValue(90);
		elevKnob.setLcdDecimals(1);
		elevKnob.setLedVisible(false);
		elevKnob.setTitle("Elevation");
		elevKnob.setUnitString("");
		elevKnob.setSize(100,100);
		setElevationGauge(45);
		
		// Listener to handle the override state change
		overAction = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    if (e.getStateChange() == ItemEvent.DESELECTED) {
			    	elevMan.setEditable(false);
			    	aziMan.setEditable(false);
			    } else if (e.getStateChange() == ItemEvent.SELECTED) {
			    	elevMan.setEditable(true);
			    	aziMan.setEditable(true);
			    }
			}
		};
		
		// Listener to handle if an enter key is pressed in the orientation boxes
		enterAction = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Fire a New Orientation Event
				fireNewOrientationEvent();
			}
		};
		
		// Configure the connection pane
		
		rotorTypeAction = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Determine which rotator was selected
				RotatorTypes selectedRotator = RotatorTypes.values()[rotorTypes.getSelectedIndex()];
				
				// Change the connection string accordingly
				connectionString.setText(selectedRotator.getConnectionString());
			}
		};
		
		connectAction = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				connectionString.setEditable(false);
				rotorTypes.setEnabled(false);
				connect.removeActionListener(connectAction);
				startRotator();
				connect.addActionListener(disconnectAction);
				connect.setText("Disconnect");
			}
		};
		
		disconnectAction = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				connectionString.setEditable(true);
				rotorTypes.setEnabled(true);
				connect.removeActionListener(disconnectAction);
				connect.addActionListener(connectAction);
				connect.setText("Connect");
			}
		};
		
		// Populate the rotator selection with all available rotators
		for(RotatorTypes type : RotatorTypes.values()) {
			rotorTypes.insertItemAt(type.getDescription(), type.ordinal());
		}
		
		// Change the size of the rotator selector
		rotorTypes.setMaximumSize(rotorTypes.getPreferredSize());
		rotorTypes.setSelectedIndex(0);
		rotorTypes.addActionListener(rotorTypeAction);
		
		// Populate the connection string information
		connectionString.setText("COM");
		connectionString.setMaximumSize(connectionString.getPreferredSize());
		connect.addActionListener(connectAction);
		
		// Lay out the connection pane
		connectPane.setLayout(new BoxLayout(connectPane, BoxLayout.LINE_AXIS));
		connectPane.add(typeLabel);
		connectPane.add(rotorTypes);
		connectPane.add(connectionLabel);
		connectPane.add(connectionString);
		connectPane.add(connect);
		
    	// Configure the checkbox for manual control
		manOver.addItemListener(overAction);
		manOver.setSelected(false);
		
		// Configure the controls to their initial state
		elevMan.setEditable(false);
		elevMan.addActionListener(enterAction);
    	elevMan.setMaximumSize(elevMan.getPreferredSize());

		aziMan.setEditable(false);
		aziMan.addActionListener(enterAction);
    	aziMan.setMaximumSize(aziMan.getPreferredSize());
    	
		// Configure a new layout system to contain the elevation controls
		elevManPane.setLayout(new BoxLayout(elevManPane, BoxLayout.LINE_AXIS));
		elevManPane.add(elevLabel);
		elevManPane.add(Box.createRigidArea(new Dimension(5,0)));
		elevManPane.add(elevMan);
		
		// Configure a new layout system to contain the azimuth controls
		aziManPane.setLayout(new BoxLayout(aziManPane, BoxLayout.LINE_AXIS));
		aziManPane.add(aziLabel);
		aziManPane.add(Box.createRigidArea(new Dimension(5,0)));
		aziManPane.add(aziMan);

		// Add the components to the frame
		add(connectPane);
		add(aziKnob);
		add(elevKnob);
		add(manOver);
		add(elevManPane);
		add(aziManPane);

		setVisible(true);
	}
	
	private void startRotator() {
		// Determine which rotator was selected
		RotatorTypes selectedRotator = RotatorTypes.values()[rotorTypes.getSelectedIndex()];
		
		// Get the rotator class infomation
		try{
			Class rotatorClass = Class.forName(selectedRotator.getClassName());
			
			Class[] types = {String.class};
			Constructor rotorConstructor = rotatorClass.getConstructor(types);
			
			
			// Start the desired rotator
			rotor = (Rotator) rotorConstructor.newInstance(connectionString.getText());
			rotor.addCurrentOrientationListener(this);	// Add the rotor panel to the listener list
			//rotor.addNewOrientationListener(rotor);
		} catch (ClassNotFoundException e) {
			// The desired class doesn't exist
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// The constructor doesn't exist
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// I don't know what this does, but it won't work without it
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	}
	
	private void stopRotator() {
		
	}
	
	public String getName() {
		return(name);
	}
	
	public void setElevationGauge(double angle) {
		elevKnob.setValueAnimated(angle);
		
	}
	
	/**
	 * Function to set the visible angle on the azimuth gauge.
	 * The angle starts with 0 at North, then increases towards the East.
	 * 180 is at South, and it keeps increasing through the West.
	 * 
	 * @param angle
	 */
	public void setAzimuthGauge(double angle) {
		aziKnob.setValueAnimated(angle);
	}
	
	/**
	 * Add an object to the receiver list for event notifications
	 * 
	 * @param listener The object to add to the list
	 */
	public synchronized void addNewOrientationListener(NewOrientationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener from the event receiver list
	 * 
	 * @param listener Object to remove
	 */
	public synchronized void removeNewOrientationListener(NewOrientationListener listener) {
		listeners.remove(listener);
	}
	
	// call this method whenever you want to notify
	//the event listeners of the particular event
	private synchronized void fireNewOrientationEvent()	{
		Orientation curr_orient = new Orientation(Integer.decode(aziMan.getText()), Integer.decode(elevMan.getText()));
		NewOrientationEvent event = new NewOrientationEvent(this, curr_orient);
		Iterator i = listeners.iterator();
		while(i.hasNext())	{
			((NewOrientationListener) i.next()).HandleNewOrientationEvent(event);
		}
	}

	/**
	 * Handle the new orientation information received from the rotator
	 */
	public void HandleCurrentOrientationEvent(CurrentOrientationEvent e) {
		setAzimuthGauge(e.orientation().azimuth);
		setElevationGauge(e.orientation().elevation);
	}
}
