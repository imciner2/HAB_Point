package dataInputs;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import geoUtils.Location;
import dataInputFramework.GPSDataLocation;
import dataInputFramework.NewDataEvent;
import dataInputFramework.NewDataListener;

public class SerialStream extends TimerTask implements DataInput {

	private List listeners = new ArrayList();
	private CommPortIdentifier portId;
	private SerialPort port;
	private InputStream input;
	private OutputStream output;
	
	private GPSDataLocation dataFormat;
	
	public SerialStream(String portNum, GPSDataLocation dataFormat) {
		// Initialize the Serial Port to gather the data from
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
		
		this.dataFormat = dataFormat;
	}
	
	/**
	 * Disconnect from the serial port
	 */
	public void disconnect() {
		port.close();	// Close the serial port
	}
	
	public String getRawData() {
		byte rec_buffer[] = new byte[12];
		String out = "";
		try {
			if (input.read(rec_buffer) == 12) {
				String rec_string = new String(rec_buffer);
				System.out.print(rec_string);
				out = rec_string;
			} else {
				JOptionPane.showMessageDialog(null, "No data received from the serial port.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error receving data from the serial port.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		return out;
	}
	
	public Location getBaloonLocation(String data) {
		String lat_string = data.substring(dataFormat.LatStart, dataFormat.LatStop);
		String long_string = data.substring(dataFormat.LongStart, dataFormat.LongStop);
		String elev_string = data.substring(dataFormat.ElevStart, dataFormat.ElevStop);
		System.out.println(lat_string);
		System.out.println(long_string);
		System.out.println(elev_string);
		
		Location loc = new Location();
		loc.Elevation = Double.valueOf(elev_string);;
		loc.Latitude = Double.valueOf(lat_string);
		loc.Longitude = Double.valueOf(long_string);
		return loc;
	}

	public void addNewDataListener(NewDataListener listener) {
		listeners.add(listener);
	}

	public void removeNewDataListener(NewDataListener listener) {
		listeners.remove(listener);
	}
	
	private synchronized void fireNewDataEvent(Location loc, String data) {
		NewDataEvent event = new NewDataEvent(loc, data);
		Iterator i = listeners.iterator();
		while(i.hasNext())	{
			((NewDataListener) i.next()).HandleNewDataEvent(event);
		}
	}

	public void run() {
		String data = getRawData();
		fireNewDataEvent(getBaloonLocation(data), data);
	}


}
