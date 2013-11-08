package rotatorFramework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;

import rotators.Rotator;

public class PeriodicOrientationCheck implements ActionListener{
	Rotator rotor;
	
	/**
	 * Constructor for the periodic orientation checking task
	 * 
	 * @param rot The rotator object
	 */
	public PeriodicOrientationCheck(Rotator rot) {
		rotor = rot;
	}

	/**
	 * 
	 */
	public void actionPerformed(ActionEvent a) {
		rotor.getOrientation();		
	}
}
