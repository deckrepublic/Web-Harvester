package cs455.transport;
//Tyler Decker
import java.util.Iterator;
import java.util.LinkedList;

public class TCPConnectionsCache {
	private LinkedList<TCPConnection> cache; //cache that holds all current connections 
	public TCPConnectionsCache(){
		cache = new LinkedList<TCPConnection>();
	}
	//return connection based of id number
	public TCPConnection getConnection(int id){
		for(TCPConnection connection : cache){
			if (connection.getId() == id) return connection;
		}
		return null;
	}
	public Iterator<TCPConnection> getConnections(){
		return cache.iterator();
	}
	//getter

	public TCPConnection getConnection(String address, int portNum){
		for(TCPConnection connection : cache){
			String localAddress = connection.getSocket().getInetAddress().getHostAddress();
			int localPort = connection.getSocket().getPort();
			if (localAddress.compareTo(address) == 0 && localPort == portNum ) return connection;
		}
		return null;
	}
	//get completeness of connections
	public boolean getCompletion(){
		for(TCPConnection connection : cache){
			if (connection.getCompleteness() == false) return false;
		}
		return true;
	}
	//adder
	public void addConnection(TCPConnection connection){
		cache.add(connection);
	}
	public int size(){
		return cache.size();
	}
	//remover
	public void removeConnection(TCPConnection connection){
		cache.remove(connection);
	}
}