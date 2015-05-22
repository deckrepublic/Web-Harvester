package cs455.routing;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

//Tyler Decker
public class RoutingTable {
	//a list of linked routing entries
	public TreeMap <Integer, RoutingEntry> table;
	//constructor
	public RoutingTable(){
		table = new TreeMap<Integer, RoutingEntry>();;
	}
	//add entry to end of table and return id number for node
	public synchronized void addEntry(RoutingEntry entry, int id){
		//hash table 
		table.put(new Integer(id), entry);
	}
	//getter from node id
	public synchronized RoutingEntry getEntry(Integer id){
		return table.get(id);
	}
	public synchronized void clear(){
		table.clear();
	}
	//getter from node index
	public synchronized RoutingEntry getEntry(int index){
		int checker = 0;
		for(RoutingEntry check : table.values()){
			if(checker == index) return check;
			checker ++;
		}
		return null;
	}
	//getter for key from int index
	public synchronized Integer getKey(int index){
		int checker = 0;
		for(Integer check : table.keySet()){
			if(checker == index) return check;
			checker++;
		}
		return null;
	}
	//checks to see if the table contains routing entry at ipAddress and portNum
	public synchronized boolean contains(String ipAddress, int portNum){
		for(RoutingEntry check : table.values()){
			if(check.getIpAddressString().compareTo(ipAddress) == 0 && check.getPortNum() == portNum) return true;
		}
		return false;
	}
	//returns all the values of the routing table
	public synchronized Collection<RoutingEntry> getValues() {
		return table.values();
	}
	//returns an enumeration of table keys
	public synchronized Set<Integer> getKeys(){
		return table.keySet();
	}
	//checks to see if the table contains routing entry at node id
	public synchronized boolean contains(int id){
		if(table.containsKey(new Integer(id))) return true;
		return false;
	}
	//remove from the table
	public synchronized void remove(int id){
		//remove from the hash table
		table.remove(new Integer(id));
	}
	//returns size of table
	public synchronized int size(){
		return table.size();
	}
}
