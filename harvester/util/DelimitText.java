package cs455.util;
//Tyler Decker
public class DelimitText {
	
	private String delimiter; //contains [ + delimeter to parse text + ] for string.split() method
	
	public DelimitText(String delimit){
		
		delimiter = new String("[" + delimit + "]");
		
	}
	//define a new delimiter
	public void defineNewDelimiter(String delimit){
		delimiter = new String("[" + delimit + "]");
	}
	//splits line based on the delimiter that is defined latest
	public String[] splitLine(String toSplit){
		return toSplit.split(delimiter);
	}
	
}
