package gui;

import java.awt.Dimension;

import javax.swing.*;
import javax.swing.border.BevelBorder;

public class StatusBar extends JPanel {
	
	JLabel statusPacket = new JLabel("Packet Status");
	JLabel statusRotor = new JLabel("Rotor Status");
	JLabel statusInformation = new JLabel("");
	
	public StatusBar(int windowSize) {
		// Create the panel for the bar
		super();
		
		// Set it to a beveled border and take up the bottom of the window
		setBorder(new BevelBorder(BevelBorder.LOWERED));
		setPreferredSize(new Dimension(windowSize, 16));
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		statusPacket.setHorizontalAlignment(SwingConstants.LEFT);
		statusRotor.setHorizontalAlignment(SwingConstants.LEFT);
		statusRotor.setText("Rotor: Not Connected");
		statusPacket.setText("Packet: Not Connected");
		
		add(statusPacket);
		add(new JSeparator(SwingConstants.VERTICAL));
		add(statusRotor);
		add(new JSeparator(SwingConstants.VERTICAL));
		add(statusInformation);
		setVisible(true);
	}
	
	public void SetPacketStatus(String status) {
		statusPacket.setText("Packet: "+status);
	}
	
	public void SetRotorStatus(String status) {
		statusRotor.setText("Rotor: "+status);
	}
	
	public void setInformationStatus(String status) {
		statusInformation.setText(status);
	}
}
