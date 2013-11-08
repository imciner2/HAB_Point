package rotatorFramework;

public class CurrentOrientationEvent extends java.util.EventObject {
	private Orientation orient;
	
	public CurrentOrientationEvent(Object source, Orientation ori) {
		super(source);
		orient = ori;
	}
	
	public Orientation orientation() {
		return(orient);
	}
}
