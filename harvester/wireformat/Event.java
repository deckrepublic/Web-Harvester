package cs455.wireformat;

//Tyler Decker
//event generated by what is in data[] from received socket
public interface Event {

	public Protocol getType();
	public abstract byte[] getBytes();
	
}