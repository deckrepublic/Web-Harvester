package cs455.harvester;
//Tyler Decker
import cs455.transport.TCPConnection;

public interface Node {
	public void onEvent(byte[] data);
	public int getPort();
	public byte[] getIpAddress();
	public String getIpAddressString();
	public int getNodeId();
	public void addConnection(TCPConnection connection);
	public TCPConnection getConnection(int crawlerId);
	public boolean checkConnection(String Ip, int portNum);
}
