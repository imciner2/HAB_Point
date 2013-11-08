package rotatorFramework;

public class NewOrientationEvent extends java.util.EventObject{
	private Orientation orient;
	public NewOrientationEvent(Object source, Orientation ori) {
		super(source);
		orient = ori;
	}
	
	public Orientation orientation() {
		return(orient);
	}
}
