package cs455.wireformat;
//Tyler Decker
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;



public class CrawlerSendsCompleteness implements Event{
	
	private int messageType; //message type 1 - 12
	private int crawlerId; //crawler id
	
	private Protocol t = Protocol.CRAWLER_SENDS_COMPLETENESS; //able to identify type of event

	public CrawlerSendsCompleteness(byte[] data) {
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); //byte array input stream
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));//get a data input stream
		//try to populate fields
		try {
			messageType = din.readInt();
			crawlerId = din.readInt();

			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	public CrawlerSendsCompleteness(int node) {
		messageType = 4;
		crawlerId = node;
	}
	//return byte array of message
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream (new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(messageType);
			dout.writeInt(crawlerId);
			dout.flush();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		marshalledBytes = baOutputStream.toByteArray();
		try {
			baOutputStream.close();
			dout.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return marshalledBytes;
	}
	//returns protocol for event
	public Protocol getType() {
		return t;
	}
	//get id of sending crawler
	public int getId() {
		return crawlerId;
	} 
}
