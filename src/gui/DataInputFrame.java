package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import java.util.Timer;

import dataInputFramework.GPSDataLocation;
import dataInputs.*;

public class DataInputFrame extends JPanel{
	
	JTabbedPane tabs;
	HashMap<String, RotorPanel> panels = new HashMap();
	HashMap<String, DataInput> inputs = new HashMap();
	HashMap<String, Timer> timers = new HashMap();
	
	public DataInputFrame(int windowWidth, int windowHeight) {
		super();
		
		// Set it to a beveled border
		setBorder(new BevelBorder(BevelBorder.LOWERED));
		setPreferredSize(new Dimension(windowWidth/32,windowHeight));

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		tabs = new JTabbedPane();
        //The following line enables to use scrolling tabs.
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(tabs);
	}
	
	public void addDataInput(int windowWidth, int windowHeight, String name) {
		RotorPanel tempPanel = new RotorPanel(windowWidth, windowHeight, name); 
		System.out.println(name);
		panels.put(name, tempPanel);
		
		tabs.addTab(name, tempPanel);
		int index = tabs.indexOfTab(name);
		JPanel pnlTab = new JPanel(new GridBagLayout());
		pnlTab.setOpaque(false);
		JLabel lblTitle = new JLabel(name);
		JButton btnClose = new JButton("x");

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;

		pnlTab.add(lblTitle, gbc);

		gbc.gridx++;
		gbc.weightx = 0;
		pnlTab.add(btnClose, gbc);

		tabs.setTabComponentAt(index, pnlTab);

		btnClose.addActionListener(new ActionListener()
		{public void actionPerformed(ActionEvent e){closeActionPerformed();}});
		
		Timer tempTimer = new Timer();
		//tempTimer.scheduleAtFixedRate((YaesuGS232) rotor, 10000, 1000);
		timers.put(name, tempTimer);
	}
	
	private void closeActionPerformed() {
		Component selected = tabs.getSelectedComponent();
        if (selected != null) {
        	String name = tabs.getName();
            tabs.remove(selected);
            timers.get(name).cancel();
    		inputs.get(name).disconnect();
        }
	}
	/*
	private void test(){

		JRadioButton serialButton = new JRadioButton("Serial Data Stream");
		serialButton.setSelected(true);
		JRadioButton manualButton = new JRadioButton("Manual Data Entry");
		manualButton.setEnabled(false);
		ButtonGroup dataStreamButtons = new ButtonGroup();
		dataStreamButtons.add(serialButton);
		
		JComponent[] firstDialog = new JComponent[] {
				new JLabel("Data Stream Source:"),
				serialButton,
				manualButton
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
		} 
		
		dataStreamItem.removeActionListener(startDataItem);
		dataStreamItem.addActionListener(stopDataItem);
		dataStreamItem.setText("Disconnect from Input Data Stream");
	}
	}
	*/
}
