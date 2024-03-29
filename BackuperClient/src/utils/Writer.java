/**
 * created on 14:29:40 22 paź 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
//klasa zapisujaca swoje ustawienia do pliku settings.txt
public class Writer {
	
	private File file = new File("settings.txt");
	private FileWriter fw;

	public Writer(Vector<String> v) {
	try {
		fw = new FileWriter(file);

		for (String s : v) {
			fw.write(s + "\n");
		}

			fw.close();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
}
