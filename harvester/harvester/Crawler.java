package cs455.harvester;
//Tyler Decker
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import cs455.crawler.*;
import cs455.routing.*;
import cs455.transport.*;
import cs455.util.*;
import cs455.wireformat.*;


public class Crawler extends Thread implements Node {

	private int portNum; //port of registry
	private String domain; // the domain url
	//@SuppressWarnings("unused")
	private TCPServerThread server; //server thread for registry
	private int threadPoolSize;
	private String [][] configList;
	private ThreadPoolManager workerManager;
	private EventFactory eventFactory; //generates events from responses or messages from messaging node
	private RoutingTable globalTable; //will contain every single node that registers with the registry
	private int idNumber; //id number of crawler
	private String completeness;
	private LinkedList<String> handoffComplete;
	private TCPConnectionsCache localCache; //connection list
	
	public Crawler(int port, int threadPoolNum, String domainUrl, int id, String[][] list) {
		portNum = port; //set
		domain = domainUrl; //the domain the Crawler is ultimately responsible for
		threadPoolSize = threadPoolNum;
		configList = list;
		idNumber = id; 
		eventFactory = new EventFactory(); //Initialize
		server = new TCPServerThread(this,portNum); //Initialize
		globalTable = new RoutingTable(); //Initialize
		localCache = new TCPConnectionsCache();
		completeness = "false";
		handoffComplete = new LinkedList<String>();
	}
	//initialize the crawler including setting of connections and routing, setting up of thread pool and workers
	public void initialize() throws UnknownHostException, IOException, InterruptedException {
	    System.out.println("waiting 10 seconds to begin initialize..");
	    Thread.sleep(10000);
		//initialize the workers for the thread pool manager
		Queue<Worker> availableWorkerQueue = new LinkedList<Worker>();
		workerManager = new ThreadPoolManager(availableWorkerQueue, this);
		for(int i = 0; i < threadPoolSize; i++) {
			Worker newguy = new Worker(workerManager);
			newguy.initialize();
			workerManager.addWorker(newguy);
		}
		
		//set up connections
		synchronized(localCache) {
			for (int i = 0; i < configList.length; i++) {
				if(i == idNumber){

				}
				else{
					//first set up the routing table
					RoutingEntry toadd = new RoutingEntry(InetAddress.getByName(configList[i][0]).getHostAddress(), Integer.parseInt(configList[i][1]));
					this.addEntry(toadd, i);
					//now try to set up connection
					Socket imp = new Socket(InetAddress.getByName(toadd.getIpAddressString()), toadd.getPortNum());
					TCPConnection alsotoadd = new TCPConnection(imp, this, i);
					this.addConnection(alsotoadd);
				}
			} 
		}
		//***waiting to send messages could be ridden while each crawler initializes workers and thread pool***
		System.out.println("waiting 10 seconds to send initial messages..");
		Thread.sleep(10000);
		//send data now that table is setup
			for (int i = 0; i < configList.length; i++) {
				//send incomplete message
				if(i == idNumber) {}
				else {
					this.getConnection(i).sendData(new CrawlerSendsIncompleteness(idNumber).getBytes());
				}
			}
	    System.out.println("waiting 10 seconds to recieve initial messages and finish initialize..");
	    Thread.sleep(10000);
		workerManager.addNewTask(null, new Task(domain, domain, 0));
		workerManager.run();
	}
	//on event if Crawler receives a message 
	public void onEvent(byte[] data) {
		Event onevent = eventFactory.createEvent(data);
		switch(onevent.getType()) {
			case CRAWLER_SENDS_INCOMPLETENESS: { CrawlerSendsIncompleteness event = (CrawlerSendsIncompleteness)onevent;
					 System.out.println("crawler: " + event.getId() + " sends incompleteness");
					 //insure that connection is not removed or messed with while multiple threads are working
					 synchronized(localCache) {
						 this.getConnection(event.getId()).updateCompleteness(false);
					 } 
					 break;
			}
			case CRAWLER_SENDS_HANDOFF: { CrawlerSendsHandoff event = (CrawlerSendsHandoff) onevent;
				synchronized(workerManager) {
						//System.out.println("Crawler " + event.getCrawlerId() + " sends new task:" + event.getURLString());
						//System.out.println(this.getId() + " tries to add: "+task.getURL());

						Task handoff = new Task(event.getURLString(), event.getURLString(), 0);
						workerManager.addHandOffTask(handoff);
						workerManager.addNewTask(null, handoff);
				}
				Task thisguy = workerManager.pullHandOffTask();
				if(thisguy != null) {
					try {
						this.getConnection(event.getCrawlerId()).sendData(new CrawlerSendsHandoffCompleteness(idNumber, thisguy.getURL()).getBytes());
					} catch (IOException e) {
						System.out.println("problem sending handoff completeness");
					}
				}
					 break;
			}
			case CRAWLER_SENDS_COMPLETENESS: { CrawlerSendsCompleteness event = (CrawlerSendsCompleteness) onevent;
					 System.out.println("crawler: " + event.getId() + " sends completeness");
					 //insure that connection is not removed or messed with while multiple threads are working
					 synchronized(localCache) {
						 this.getConnection(event.getId()).updateCompleteness(true);
					 }
					 break;
			}
			case CRAWLER_SENDS_HANDOFF_COMPLETENESS: { CrawlerSendsHandoffCompleteness event = (CrawlerSendsHandoffCompleteness) onevent;
					 //insure that connection is not removed or messed with while multiple threads are working
					 synchronized(handoffComplete) {
						 handoffComplete.remove(event.getURLString());
					 }
			 		 break;
			}
			default: System.out.println("Error: invalid event from crawler");
					 break;
		}
		
	}
	//find the crawler responsible for the domain of the task and passes on a message to that crawler
	public void passOnTask(Task task) {
		//go through each domain
		for(int i = 0; i < this.configList.length; i++) {
			try {
				//if the task's url falls under the domain of host at i in configuration list
				if (this.checkDomain(task.getURL(), this.configList[i][2])){
					synchronized(handoffComplete) {
						handoffComplete.add(task.getURL());
					}
					this.getConnection(i).sendData(new CrawlerSendsHandoff(idNumber, task.getURL()).getBytes());
				}
			} catch (Exception e) {
				//System.out.println("problem passing on task to responsible crawler");
			}
		}
		
	}
	//return port number of the crawler
	public int getPort() {
		return portNum;
	}
	//return server (not really used)
	public TCPServerThread getServer() {
		return server;
	}
	//get raw byte[] of ip address from crawler
	public byte[] getIpAddress() {
		InetAddress address;
		byte[] rawAddress = null;
		try {
			address = InetAddress.getLocalHost();
			rawAddress = address.getAddress();
		} catch (UnknownHostException e) {
			System.out.println("unknown host");
		}
		return rawAddress;
	}
	//get the string of the crawlers ip
	public String getIpAddressString() {
		InetAddress address;
		String rawAddress = null;
		try {
			address = InetAddress.getLocalHost();
			rawAddress = address.getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("unknown host");
		}
		return rawAddress;
	}
	//return id of crawler
	public int getNodeId() {
		return idNumber;
	}
	//remove the entry
	public void removeEntry(int crawlerId) {
		synchronized(globalTable) {
			globalTable.remove(crawlerId);
		}
	}
	//add the entry
	public void addEntry(RoutingEntry entry, int id) {
		synchronized(globalTable) {
			globalTable.addEntry(entry, id);
		}
	}
	//checks to see if entry is in the table
	public boolean checkEntry(int id) {
		synchronized(globalTable) {
			RoutingEntry check = globalTable.getEntry(new Integer(id));
			if(check != null) return true;
			else return false;
		}		
	}
	//add a connection to the cahce of connections
	public void addConnection(TCPConnection connection) {
		synchronized(localCache) {
			localCache.addConnection(connection);
		}
	}
	//get a connection from the list of connections
	public TCPConnection getConnection(int crawlerId) {
		TCPConnection returner = null;
			returner = localCache.getConnection(crawlerId);
		return returner;
	}
	//check if the connection is in the cache
	public boolean checkConnection(String Ip, int portNum) {
		synchronized(localCache) {
			TCPConnection check = localCache.getConnection(Ip, portNum);
			if(check != null) return true;
			else return false;
		}		
	}
	//domain check to see if url belongs to crawler's jurisdiction
	public boolean checkDomain(String pageUrl) throws MalformedURLException {
		if (domain.equals("http://www.colostate.edu/Depts/Psychology/")) {
			if(pageUrl.contains("Psychology")) return true;
			else return false;
		}
		else return new URL(pageUrl).getHost().equals(new URL(domain).getHost());
	}
	//domain check to see if url belongs to crawler's jurisdiction
	public boolean checkDomain(String pageUrl, String domain) throws MalformedURLException {
		if (domain.equals("http://www.colostate.edu/Depts/Psychology/")) {
			if(pageUrl.contains("Psychology")) return true;
			else return false;
		}
		else return new URL(pageUrl).getHost().equals(new URL(domain).getHost());
	}
	//update completeness of manager
	public void updateCompleteness(boolean b) {
		synchronized(completeness){
			if(b == true){
				this.completeness = "true";
				sendCompleteness();
			}
			else{
				this.completeness = "false";
				sendIncompleteness();
			}
			
		}
	}
	//send notice to every crawler in network
	private void sendIncompleteness() {
		synchronized(localCache) {
			Iterator<TCPConnection> sends = localCache.getConnections();
			while(sends.hasNext()){
				try {
					sends.next().sendData(new CrawlerSendsIncompleteness(idNumber).getBytes());
				} catch (IOException e) {
					System.out.println("problem sending completion notice");
				}
			}
		}
	}
	//send notice to every crawler in network
	private void sendCompleteness() {
		synchronized(localCache) {
			Iterator<TCPConnection> sends = localCache.getConnections();
			while(sends.hasNext()){
				try {
					sends.next().sendData(new CrawlerSendsCompleteness(idNumber).getBytes());
				} catch (IOException e) {
					System.out.println("problem sending incompletion notice");
				}
			}
		}
	}
	//get the completeness of manager
	public boolean getCompleteness() {	
		synchronized(completeness){
			if(completeness.equals("true")) return true;
			else return false;
		}
	}
	//get the completeness of manager
	public boolean getCompletenessOfOthers() {	
		synchronized(localCache) {
			return localCache.getCompletion();
		}
	}
	//get the completeness of handoffs
	public boolean getHandoffCompleteness() {	
		synchronized(handoffComplete) {
			if(handoffComplete.isEmpty()) return true;
			else return false;
		}
	}
	public String getDomain() {
		return domain;
	}
	//get an [][] array with hostname, port, and domain from configuration file
	public static String[][] getConfigList(String file_name) {
		try {
			//delimitation of configuration to get thread pool size, host, port, and root url of other crawlers, allows for assigning of id number
			ReadFile file = new ReadFile(file_name);
			DelimitText firstSplitter = new DelimitText(",");
			DelimitText secondSplitter = new DelimitText(":");
			String []text_file = file.openFile();
			//split host:port,root url to host:port root url
			String splitTokens[][] = new String [text_file.length][2];
			//host and port for each entry, ex denver:55555 becomes denver 55555
			String host_and_port[][] = new String[text_file.length][2];
			String host_port_domain[][] = new String[text_file.length][3];
			for(int i = 0; i < text_file.length; i++){
				splitTokens[i] = firstSplitter.splitLine(text_file[i]);
				host_and_port[i] = secondSplitter.splitLine(splitTokens[i][0]);
				host_port_domain[i][0] = host_and_port[i][0];
				host_port_domain[i][1] = host_and_port[i][1];
				host_port_domain[i][2] = splitTokens[i][1];
			}	
			return host_port_domain;
		} catch (IOException e) {
			System.out.println("error reading file");
			return null;
		}
	}
	//get id number from config list position in list determines id number 
	public static int getIdFromList(String[][] list, int portNum) {
		int id = -1;
		InetAddress address;
		try {
			address = InetAddress.getLocalHost();
			for(int i = 0; i < list.length; i++) {
				if(address.getHostName().equalsIgnoreCase(list[i][0])) {
					if(portNum == Integer.parseInt(list[i][1])) {
						id = i;
					}
				}
			}
		} catch (UnknownHostException e) {
			System.out.println("could not find local host");
		}
		if(id == -1) {
			System.out.println("error with config file could not find local host on list");
		}
		return id;
	}
	//main
	public static void main(String args[]) {
		Crawler crawler;
		//check if args are present
		int portNum = 0;
		int threadPoolSize = 0;
		int id = -1;
		String domain = null;
		String file_name = null;
		if(args.length != 4 ){
			System.out.println("Invalid number of arguments");
			System.exit(0);
		}
		else {
			//initialize values to feed into constructor
			portNum = Integer.parseInt(args[0]);
			threadPoolSize = Integer.parseInt(args[1]);
			domain = args[2];
			file_name = args[3];
			String[][] configList = getConfigList(file_name);
			id = getIdFromList(configList, portNum);
			//finally initialize constructor
			crawler = new Crawler(portNum, threadPoolSize, domain, id, configList);
		    try {
		    	Thread.sleep(1000);
				crawler.initialize();
			} catch (InterruptedException | IOException e) {
				System.out.println("could not initialize");
			}                 //1000 milliseconds is one second., give time to wait
		}
	}




}
