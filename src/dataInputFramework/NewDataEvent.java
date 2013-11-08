package dataInputFramework;

import geoUtils.Location;

public class NewDataEvent {

	public String rawData;
	public Location baloonLocation;
	
	public NewDataEvent(Location loc, String data){
		this.baloonLocation = loc;
		this.rawData = data;
	}
	
	public Location getBaloonLocation(){
		return(baloonLocation);
	}
	
	public String getRawData(){
		return(rawData);
	}
}
