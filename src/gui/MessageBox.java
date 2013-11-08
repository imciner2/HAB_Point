package gui;


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class MessageBox extends JFrame
{
	private Container c;
	
	private boolean isClicked;

	public MessageBox(String title, String message)
	{
		super(title);
		c = getContentPane();
		c.setLayout(new GridLayout(2, 2));

		JButton ok = new JButton("Ok");
		ok.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						isClicked= true;
						setVisible(false);
					}
				}
		);

		JTextArea text = new JTextArea(message);
		text.setEditable(false);

		c.add(text);
		c.add(ok);
		
		setSize(250, 150);
		setResizable(false);
		
		setVisible(true);
	}
	
	public boolean okClicked()
	{
		return(isClicked);
	}
}
