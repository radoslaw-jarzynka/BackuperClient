/**
 * created on 21:29:33 9 lis 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import common.BackuperInterface;
import utils.NotifyingThread;
//watek odbierajacy pliki z serwera
public class FileReceiver extends NotifyingThread {

	private String username;
	private BackuperInterface server;
	//kolejka plikow, ktore chcemy pobrac
	private volatile static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	//konstruktor pobierajacy nazwe uzytkownika, serwer i wektro nazw plikow
	public FileReceiver(String username, BackuperInterface server, Vector<String> selectedFiles) {
		this.username = username;
		this.server = server;
		//zamiana wektora na kolejke
		queue.addAll(selectedFiles);
	}


	@Override
	public void doRun() {
		if (queue.peek() != null) {
			//sprawdz czy jest cos na czubku kolejki - jak jest to to pobierz i sprawdzaj ponownie
			String fileName = queue.poll();
			InputStream istream = null;
			FileOutputStream ostream = null;
			try {
				istream = RemoteInputStreamClient.wrap(server.getFile(username, fileName));
				File receivedFile = new File(fileName);
				ostream = new FileOutputStream(receivedFile);
				//logController.addLine("Writing file " + receivedFile);
	  
				byte[] buf = new byte[1024];
				
				int bytesRead = 0;
				while((bytesRead = istream.read(buf)) >= 0) {
					ostream.write(buf, 0, bytesRead);
				}
				ostream.flush();
				//logController.addLine("Finished writing file " + receivedFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			doRun();
		}
	}

}
