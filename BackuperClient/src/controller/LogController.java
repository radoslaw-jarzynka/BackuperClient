/**
 * created on 12:53:55 10 paź 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package controller;

import model.Log;
import view.LogArea;
import view.MainWindow;
//klasa kontrolera logu - okienka tekstowego przez ktore aplikacja komunikuje sie z uzytkownikiem
public class LogController {
	//widoczne okienko logu
	private LogArea logArea;
	//niewidoczny plik logu
	private Log log;
	
	public LogArea getLogArea() {
		return logArea;
	}
	
	public Log getLog() {
		return log;
	}
	//zapisanie do pliku w folderze logs jako plik tekstowy o nazwie bedacej akutalna data i godzina
	public void saveLogToFile() {
		log.saveLogToFile();
	}
	//konstruktor pobierajacy przy okazji wymiary i polozenie okna logArea
	public LogController(int x, int y, int width, int height) {
		logArea = new LogArea(x, y, width, height);
		log = new Log();
	}
	//dodanie liniki logu
	public void addLine(String logLine) {
		log.getLog().add(logLine + "\n");
		logArea.append(logLine + "\n");
	}
	//konstruktor pobierajacy tylko okno mainWindow, w koncu nie zostal wykorzystany w kodzie
	public LogController(MainWindow mW) {
		logArea = new LogArea();
		log = new Log();
	}
	

}
