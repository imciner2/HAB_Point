package rotators;


/**
 * This enumerated list is used to keep track of the different types of rotators available for connections
 */
public enum RotatorTypes {
	YAESU_GS232_Local ("Yaesu GS232 Local Port", "rotators.YaesuGS232_Local", "COM"),
	YAESU_GS232_Remote ("Yaesu GS232 Remote Serial Server", "rotators.YaesuGS232_Remote", "127.0.0.1");
	
	private final String description;
	private final String className;
	private final String connectionString;
	
	RotatorTypes(String description, String className, String connectionString) {
		this.description = description;
		this.className = className;
		this.connectionString = connectionString;
	}
	
	/**
	 * This function will return a description of the rotator option 
	 * 
	 * @return The description of the rotator
	 */
	public String getDescription() {
		return(description);
	}
	
	
	/**
	 * This function will return the name of the class the rotator option uses
	 * 
	 * @return A string with the class name
	 */
	public String getClassName() {
		return(className);
	}
	
	
	/**
	 * This function will return the default connection string for the rotator option
	 * 
	 * @return The default connection string
	 */
	public String getConnectionString() {
		return(connectionString);
	}
}
