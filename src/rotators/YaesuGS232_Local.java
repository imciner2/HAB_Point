package rotators;

import gnu.io.*;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

import rotatorFramework.*;

/**
 * Class to interface with a Yaesu GS232 antenna rotator system.
 * 
 * @author Ian McInerney
 *
 */
public class YaesuGS232_Local extends TimerTask implements Rotator, NewOrientationListener{

	private List listeners = new ArrayList();
	private CommPortIdentifier portId;
	private SerialPort port;
	private InputStream input;
	private OutputStream output;

	
	/**
	 * Main constructor for the class
	 * 
	 * @param portNum The name of the port the rotator is on
	 */
	public YaesuGS232_Local(String portNum) {
		System.out.println("Hello I am the local class!");
		
		try {
			// Get the port identifier
			portId = CommPortIdentifier.getPortIdentifier(portNum);

			// Get the serial port
			port = (SerialPort) portId.open(this.getClass().getName(),2000);

			// Configure the port
			port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			// Get the streams
			input = port.getInputStream();
			output = port.getOutputStream();
		} catch (UnsupportedCommOperationException e) {
			// Comm settings not supported
			JOptionPane.showMessageDialog(null, "Communications settings not supported.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			// Could not get the streams
			JOptionPane.showMessageDialog(null, "Could not open communication buffers.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (PortInUseException e) {
			// Serial port is in use
			JOptionPane.showMessageDialog(null, "Serial port is in use.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (NoSuchPortException e) {
			// Serial port doesn't exist
			JOptionPane.showMessageDialog(null, "Serial port does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
		}	
	}
	
	/**
	 * Disconnect from the rotator
	 */
	public void disconnect() {
		port.close();	// Close the serial port
	}
	
	/**
	 * Get the orientation of the antenna rotator
	 * 
	 * @return A variable of type Orientation containing the orientation
	 */
	public Orientation getOrientation() {		
		// Place the command to get azimuth and elevation into the buffer
		byte trans_buffer[] = new byte[3];
		trans_buffer[0] = 'C';
		trans_buffer[1] = '2';
		trans_buffer[2] = 0x0D;	// Command letter
		
		// Send the packet over the port
		try {
			output.write(trans_buffer);
			output.flush();
		} catch (IOException e) {
			// Couldn't send message
			JOptionPane.showMessageDialog(null, "Error sending data over serial port.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		byte rec_buffer[] = new byte[12];
		double azi = 0;
		double elev = 0;
		try {
			if (input.read(rec_buffer) == 12) {
				String rec_string = new String(rec_buffer);
				System.out.print(rec_string);
				String azi_string = rec_string.substring(2,5);
				String elev_string = rec_string.substring(7, 11);
				System.out.println(azi_string);
				System.out.println(elev_string);
				azi = Double.valueOf(azi_string);
				elev = Double.valueOf(elev_string);
			} else {
				JOptionPane.showMessageDialog(null, "No data received from the serial port.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error receving data from the serial port.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		// Rotate the azimuth by 180 degrees
		azi = azi + 180;
		azi = azi % 360;
		
/*		if(elev_test > 90)
			elev_test = 0;
		else
			elev_test++;
		
		if(azi_test > 360)
			azi_test = 0;
		else
			azi_test++;
*/		// Return the received orientation values
		return(new Orientation(azi, elev));
	}

	/**
	 * Set the orientation of the antenna rotator
	 * 
	 * @param orient A variable of type Orientation containing the necessary data
	 */
	public void setOrientation(Orientation orient) {
		
		// Rotate the azimuth by 180 degrees
		orient.azimuth = orient.azimuth + 180;
		orient.azimuth = orient.azimuth % 360;
		
		String azi = String.format("%03d", (int) orient.azimuth);
		String elev = String.format("%03d",(int) orient.elevation);

		byte buffer[] = new byte[9];
		buffer[0] = 'W';
		buffer[1] = (byte) azi.charAt(0);
		buffer[2] = (byte) azi.charAt(1);
		buffer[3] = (byte) azi.charAt(2);
		buffer[4] = ' ';
		buffer[5] = (byte) elev.charAt(0);
		buffer[6] = (byte) elev.charAt(1);
		buffer[7] = (byte) elev.charAt(2);
		buffer[8] = 0x0D;	// Command Letter
		
		System.out.println(new String(buffer));

		// Send the packet over the port
		try {
			output.write(buffer);
			output.flush();
		} catch (IOException e) {
			// Couldn't send message
			JOptionPane.showMessageDialog(null, "Error sending data over serial port.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Add an object to the receiver list for event notifications
	 * 
	 * @param listener The object to add to the list
	 */
	public synchronized void addCurrentOrientationListener(CurrentOrientationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener from the event receiver list
	 * 
	 * @param listener Object to remove
	 */
	public synchronized void removeCurrentOrientationListener(CurrentOrientationListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * 
	 */
	private synchronized void fireCurrentOrientationEvent(Orientation orient)	{
		CurrentOrientationEvent event = new CurrentOrientationEvent(this, orient);
		Iterator i = listeners.iterator();
		while(i.hasNext())	{
			((CurrentOrientationListener) i.next()).HandleCurrentOrientationEvent(event);
		}
	}

	/**
	 * Handle the new orientation event
	 */
	public void HandleNewOrientationEvent(NewOrientationEvent e) {
		setOrientation(e.orientation());
	}
	
	public void run() {
		fireCurrentOrientationEvent(getOrientation());
	}
}
