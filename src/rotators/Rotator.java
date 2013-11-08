package rotators;

import rotatorFramework.*;

public interface Rotator extends NewOrientationListener, Runnable{
	public void disconnect();
	public Orientation getOrientation();
	public void setOrientation(Orientation orient);
	public void addCurrentOrientationListener(CurrentOrientationListener listener);
	public void removeCurrentOrientationListener(CurrentOrientationListener listener);
}
