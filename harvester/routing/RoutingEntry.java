package cs455.routing;
//Tyler Decker

//Entry for routing table
public class RoutingEntry {
	private String ipAddress;
	private int portNum;
	
	//constructor
	public RoutingEntry(String ipAddress, int portNum){;
		this.ipAddress = ipAddress;
		this.portNum = portNum;
	}
	//getter
	public int getPortNum(){
		return portNum;
	}
	//getter
	public String getIpAddressString(){

      return ipAddress;
	}
}
