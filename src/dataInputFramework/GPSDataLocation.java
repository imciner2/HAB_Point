package dataInputFramework;

public class GPSDataLocation {
	// Location of the latitude in the data stream
	public int LatStart;
	public int LatStop;
	
	// Location of the longitude in the data stream
	public int LongStart;
	public int LongStop;
	
	// Location of the elevation in the data stream
	public int ElevStart;
	public int ElevStop;
	
	public GPSDataLocation(int latStart, int latStop, int longStart, int longStop, int elevStart, int elevStop){
		this.LatStart = latStart;
		this.LatStop = latStop;
		this.LongStart = longStart;
		this.LongStop = longStop;
		this.ElevStart = elevStart;
		this.ElevStop = elevStop;
	}
}
