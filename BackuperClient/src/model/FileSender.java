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

public class FileSender extends NotifyingThread {

	private String username;
	private BackuperInterface server;
	private volatile static LinkedBlockingQueue<File> queue = new LinkedBlockingQueue<File>();
	
	public FileSender(String username, BackuperInterface server, Vector<File> selectedFiles) {
		this.username = username;
		this.server = server;
		queue.addAll(selectedFiles);
	}


	@Override
	public void doRun() {
		if (queue.peek() != null) {
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
