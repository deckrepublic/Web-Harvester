package cs455.transport;
//Tyler Decker

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import cs455.harvester.*;


public class TCPServerThread implements Runnable{
	private ServerSocket serverSocket;
	private TCPConnection theConnection;
	private Node node; //node to trigger event in receiver
	//create a thread to essentially be a server for a node or the registry
	public TCPServerThread(Node node, int port){
		try {
			//node server is responsible for
			this.node = node;
			//server socket responsible for listening 
			serverSocket = new ServerSocket(port);
			port = serverSocket.getLocalPort();
			Thread t = new Thread(this);
			t.start();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	//returns socket of server
	public ServerSocket getSocket(){
		return serverSocket;
	}
	//sends data through server connection
	public void sendData(byte[] data){
		try {
			theConnection.sendData(data);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	//thread function
	//remember to implement an event listener when the server thread
	//receives a message trigger the event and respond
	public void run() {
		while(true){
			try {
				
				Socket socket = serverSocket.accept();
				//String ipAddress = socket.getInetAddress().getHostAddress();
				this.theConnection = new TCPConnection(socket,node);
				this.theConnection.initializeReceiver();
				//add the connection to the list of connections
				//if(node.get)
				//if(node.checkConnection(ipAddress, portNum));
				//node.addConnection(theConnection);

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
			
	}
}