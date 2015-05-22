package cs455.util;
//Tyler Decker
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {
	private String path;
	
	public ReadFile(String file_path){
		path = file_path;
	}
	
	public String[] openFile() throws IOException {
		//reader from file
		FileReader file_to_read = new FileReader(path);
		BufferedReader bf = new BufferedReader(file_to_read);
		//dictates when to stop
		int numberOfLines = this.readLines();
		
		String[] returner = new String[numberOfLines];
		
		for(int i = 0; i < numberOfLines; i++){
			returner[i] = bf.readLine();
		}
		
		bf.close();
		return returner;
	}
	
	public int readLines() throws IOException {
		//reader from file
		FileReader file_to_read = new FileReader(path);
		BufferedReader bf = new BufferedReader(file_to_read);
		//aLine simple pointer and numberOfLines returner
		@SuppressWarnings("unused")
		String aLine;
		int numberOfLines = 0;
		
		while(( aLine = bf.readLine()) != null) {
			numberOfLines ++;
		}
		
		bf.close();
		
		return numberOfLines;
	}
}
