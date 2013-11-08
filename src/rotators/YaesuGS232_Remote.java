package rotators;

import rotatorFramework.CurrentOrientationListener;
import rotatorFramework.NewOrientationEvent;
import rotatorFramework.Orientation;

public class YaesuGS232_Remote implements Rotator {

	
	public YaesuGS232_Remote(String connectionString) {
		System.out.println("Hello, I am the remote class!");
	}
	
	
	@Override
	public void HandleNewOrientationEvent(NewOrientationEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public Orientation getOrientation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOrientation(Orientation orient) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCurrentOrientationListener(
			CurrentOrientationListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCurrentOrientationListener(
			CurrentOrientationListener listener) {
		// TODO Auto-generated method stub

	}

}
