/**
 * created on 12:53:55 10 paź 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package controller;

import model.Log;
import view.LogArea;
import view.MainWindow;

public class LogController {
	private LogArea logArea;
	private Log log;
	private MainWindow mainWindow;

	public LogArea getLogArea() {
		return logArea;
	}
	
	public Log getLog() {
		return log;
	}
	
	public void saveLogToFile() {
		log.saveLogToFile();
	}
	
	public LogController(MainWindow mW,int x, int y, int width, int height) {
		logArea = new LogArea(x, y, width, height);
		log = new Log();
	}
	
	public void addLine(String logLine) {
		log.getLog().add(logLine + "\n");
		logArea.append(logLine + "\n");
	}
	
	public LogController(MainWindow mW) {
		logArea = new LogArea();
		log = new Log();
	}
	

}
