/**
 * created on 20:42:27 9 paź 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */

package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;


public class Log {
	
	private Vector<String> log;
	
	public Log() {
		
		log = new Vector<String>();
		log.clear();
	}
	
	public void addLineToLog(String line) {
		log.add(line);
	}
	
	public String getLineFromLog(int lineNo) {
		return log.get(lineNo);
	}
	
	public void saveLogToFile() {
		File file = new File("logs/log : " + new Date().toGMTString() + ".txt");
		FileWriter fw;

		try {
			fw = new FileWriter(file);

			for (String s : this.log) {
				fw.write(s + "\n");
			}

			fw.close();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
	public Vector<String> getLog() {
		return log;
	}
}
