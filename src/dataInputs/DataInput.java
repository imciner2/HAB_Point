package dataInputs;

import geoUtils.Location;
import dataInputFramework.NewDataListener;

public interface DataInput {
	public void disconnect();
	public void addNewDataListener(NewDataListener listener);
	public void removeNewDataListener(NewDataListener listener);
}
