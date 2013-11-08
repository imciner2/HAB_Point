package gui;


import java.awt.*;
import javax.swing.*;

import java.awt.event.*;


public class AboutBox extends JDialog
{
	Container contentPane;
	
	JButton ok = new JButton("Close");


	public AboutBox(Frame f)
	{
		super(f,"About",true);
		contentPane = getContentPane();

		ok.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						dispose();
					}
				}
		);
		JPanel button = new JPanel();
		button.add(ok);
		
		Box b = Box.createVerticalBox();
		b.add(Box.createGlue());
		b.add(new JLabel("    High altitude ballonn antenna tracking program"));
		b.add(new JLabel("    Version 1.0"));
		b.add(Box.createGlue());
				
		contentPane.add(b, "Center");
		contentPane.add(button, "South");

		pack();
		setSize(300, 130);
		setLocationRelativeTo(null);
	}
}

