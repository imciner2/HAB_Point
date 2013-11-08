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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import java.util.Timer;

import rotators.*;

public class RotatorFrame extends JPanel{
	
	JTabbedPane tabs;
	HashMap<String, RotorPanel> panels = new HashMap();
	HashMap<String, Rotator> rotors = new HashMap();
	HashMap<String, Timer> timers = new HashMap();
	
	public RotatorFrame(int windowWidth, int windowHeight) {
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
	
	public void addRotator(int windowWidth, int windowHeight, String name) {
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
    		rotors.get(name).disconnect();
        }
	}
}
