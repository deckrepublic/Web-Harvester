package cs455.transport;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;

import cs455.harvester.*;
//Tyler Decker
//TCPConnection intakes the two sockets of the connected nodes, then creates a connection for them based on that
public class TCPConnection {
	private Socket socket; //to send a message
	private TCPSender toSend; //object in charge of sending bytes
	private TCPReceiver toReceive; //object in charge of receiving bytes
	private Node node; //node to trigger onEvent or the response to the message\
	private int idNumber; //id number of node on the other side of the socket
	private boolean complete;
	//constructor
	public TCPConnection(Socket socket, Node node) {
		this.socket = socket;
		this.node = node;
		this.complete = false;
		//create the objects in charge of sending and receiving
		try {
			this.toReceive = new TCPReceiver(socket, node); //Initialize
			this.toSend = new TCPSender(socket); //Initialize 
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	public TCPConnection(Socket socket, Node node, int id) {
		this.socket = socket;
		this.node = node;
		this.complete = false;
		this.idNumber = id;
		//create the objects in charge of sending and receiving
		try {
			this.toReceive = new TCPReceiver(socket, node); //Initialize
			this.toSend = new TCPSender(socket); //Initialize 
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	public void initializeReceiver(){
		this.toReceive.initialize();
	}
	public void sendData(byte[] dataToSend) throws IOException {
		this.toSend.sendData(dataToSend);
	}
	//return the node
	public Node getNode(){
		return node;
	}
	public int getId(){
		return idNumber;
	}
	public void setId(int id){
		idNumber = id;
	}
	public Socket getSocket(){
		return socket;
	}
	public boolean getCompleteness(){
		return complete;
	}
	public void updateCompleteness(boolean flag){
		complete = flag;
	}
	//function closes socket so receive thread can close]
	public void close() {
		try {
			this.socket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	//The Sender class in charge of sending the bytes
	public class TCPSender {
		private Socket socket; //the socket to send to
		private DataOutputStream dout; //data to send

		//constructor get socket from Connection constructor
		public TCPSender(Socket socket) throws IOException {
			this.socket = socket;
			dout = new DataOutputStream(this.socket.getOutputStream());//data sent through this stream will go through sockets stream
		}
		//in charge of sending data
		public void sendData(byte[] dataToSend) throws IOException {
			//create a thread to manage the sending of messages from the socket to prevent group lock
			int dataLength = dataToSend.length; //length of message
			try {
				dout.writeInt(dataLength); //write the length
				dout.write(dataToSend, 0, dataLength); //send the data to socket
				dout.flush(); //flush stream, good practice
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		public void run() {

		}
	}
	public class TCPReceiver implements Runnable {
		private Socket socket; //socket to receive from
		private DataInputStream din; //stream to put bytes of data
		private Node node; //node to trigger event
		private Thread t;

		public TCPReceiver(Socket socket, Node node) throws IOException {
			this.socket = socket;
			this.node = node;
			din = new DataInputStream(socket.getInputStream());
			//create a thread to manage the receiving of messages from the socket
			try {
				t = new Thread(this);
				
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
		}			
		public void initialize(){
			t.start();
		}
		//the receive function that tries to read in data
		public void run() {
			int dataLength;
			while(socket != null) {
				try{
					//get length of message
					dataLength = din.readInt();//can throw IO Exception
					//array that holds information serialized
					byte[] data = new byte[dataLength];
					//read in data
					din.readFully(data, 0, dataLength);// can throw socket Exception
					this.node.onEvent(data); //throws event from received data
					
				}catch (SocketException se) {
					System.out.println(se.getMessage());
					break;
				}catch (IOException ioe) {
					//System.out.println(ioe.getMessage());
					break;
				}
			}
		}
	}
}
