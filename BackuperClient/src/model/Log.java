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

//model logu
public class Log {
	
	private Vector<String> log;
	//konstruktor - tworzy wektor stringow i na wszelki wypadek go czysci
	public Log() {
		
		log = new Vector<String>();
		log.clear();
	}
	//dodanie linni logu
	public void addLineToLog(String line) {
		log.add(line);
	}
	//pobranie danej linijki kodu
	public String getLineFromLog(int lineNo) {
		return log.get(lineNo);
	}
	//zapisanie logu do pliku tekstowego
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
	//pobranie calego wektora stringow
	public Vector<String> getLog() {
		return log;
	}
}
