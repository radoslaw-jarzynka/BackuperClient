/**
 * created on 14:55:58 5 lis 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import common.BackuperInterface;
import utils.NotifyingThread;
//klasa watku wysylajacego pliki na serwer
public class FileSender extends NotifyingThread {

	private String username;
	private BackuperInterface server;
	//kolejka plikow oczekujacych na wyslanie na serwer
	//w przeciwienstwie do kolejki przy watki liczacym md5 ta nie bedzie zmieniana w trakcie jego pracy
	private volatile static LinkedBlockingQueue<File> queue = new LinkedBlockingQueue<File>();
	//konstruktor pobierajacy nazwe uzytkownika, interfejs serwera i wektor nazw plikow
	public FileSender(String username, BackuperInterface server, Vector<File> selectedFiles) {
		this.username = username;
		this.server = server;
		//zamiana wektora na kolejke
		queue.addAll(selectedFiles);
	}


	@Override
	public void doRun() {
		if (queue.peek() != null) {
			//jesli cos jest na gorze kolejki - wyslij to i sprawdzaj znowu az kolejka bedzie pusta
			File f = queue.poll();
			SimpleRemoteInputStream istream = null;
			try {
				istream = new SimpleRemoteInputStream(new FileInputStream(f.getAbsolutePath()));
				server.uploadFile(username, f.getName(), f.lastModified(), istream.export());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} finally {
				istream.close();
			}
			doRun();
		}
	}

}
