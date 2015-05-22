package cs455.wireformat;
//Tyler Decker
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

//class responsible for creating an Event based off of the data 
public class EventFactory {
	
	public EventFactory (){	
		
	}
	//creates the event
	public Event createEvent(byte[] data){
		switch(getMessageType(data)){
			case 2: return new CrawlerSendsIncompleteness(data);
			case 3: return new CrawlerSendsHandoff(data);
			case 4: return new CrawlerSendsCompleteness(data);
			case 5: return new CrawlerSendsHandoffCompleteness(data);
			default: return null;
		}
	}
	//for when event factory only gets pure byte[]
	public int getMessageType(byte[] data){
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); //byte array input stream
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));//get a data input stream
		int messageType = -1;
		//try to populate fields
		try {
			messageType = din.readInt();
			baInputStream.close();
			din.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return messageType;
	}
}
