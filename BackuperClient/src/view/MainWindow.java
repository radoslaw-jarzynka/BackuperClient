/**
 * created on 21:14:35 16 paź 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import common.BackuperInterface;
import model.FileReceiver;
import model.FileSender;
import model.FileTableModel;
import controller.LogController;
import utils.Reader;
import utils.ThreadCompleteListener;
import utils.Writer;
import view.LogInWindow;
import view.RegisterWindow;
import model.Md5Generator;

public class MainWindow extends JFrame implements ActionListener, ThreadCompleteListener {

	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 800;
	private static final int HEIGHT = 700;

	private String serverIP;
	private String serverPort;
	
	private JPanel contentPane; 

	//Threads
	Md5Generator md5gen;
	FileSender fileSender;
	
	// client's app log
	private LogController logController;

	private Vector<File> selectedFiles;
	private Vector<String> selectedFilesMd5;
	
	//is connected to server?
	private boolean isConnected;
	
	//server interface
	private BackuperInterface server;
	private String username;

	// file tables
	private JTable chosenFilesTable;
	private JTable serverFilesTable;

	// table data vector
	// @SuppressWarnings("rawtypes")
	private FileTableModel localTableModel;
	private FileTableModel serverTableModel;

	// Buttons
	private JButton chooseFilesButton;
	private JButton sendToServerButton;
	private JButton backupFilesButton;
	private JButton removeFilesButton;

	// Labels
	private JLabel selectedFilesLabel;
	private JLabel serverFilesLabel;

	// MenuBar
	private JMenuBar menuBar;

	
	//Public Constructor
	public MainWindow() {
		super("Backuper!");

		isConnected = false;
		username = null;
		
		System.setProperty("java.security.policy", "policy");
		System.setSecurityManager(new RMISecurityManager());
		
		//Adding Window Listener
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				((MainWindow) windowEvent.getComponent()).getLogController().saveLogToFile();
				Writer w = new Writer(((MainWindow) windowEvent.getComponent()).getSettings());
				if(isConnected)
					try {
						server.disconnect(username);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				System.exit(0);

			}
		});
		
		this.setResizable(false);
		
		//Create vector of selected files
		selectedFiles = new Vector<File>();
		selectedFilesMd5 = new Vector<String>();

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setLayout(null);

		// scroll bar for log
		JScrollPane logScrollPane = new JScrollPane();
		this.logController = new LogController(this, 0, 0, 780, 140);
		logScrollPane.setViewportView(logController.getLogArea());
		logScrollPane.setBounds(10, 510, 780, 140);
		logScrollPane.setAutoscrolls(true);
		contentPane.add(logScrollPane);

		
		//creating menu bar
		menuBar = new JMenuBar();

		JMenu filesMenu = new JMenu("Actions");
		JMenu serverPrefsMenu = new JMenu("Server");
		JMenu aboutMenu = new JMenu("?");

		menuBar.add(filesMenu);
		menuBar.add(serverPrefsMenu);
		menuBar.add(aboutMenu);

		JMenuItem chooseFiles = new JMenuItem("Select Local Files");
		JMenuItem sendFiles = new JMenuItem("Send Files To Server");
		JMenuItem backupFiles = new JMenuItem("Backup Files");
		JMenuItem removeFilesFromServer = new JMenuItem("Remove Selected File From Server");

		filesMenu.add(chooseFiles);
		filesMenu.add(sendFiles);
		filesMenu.add(backupFiles);
		filesMenu.add(removeFilesFromServer);

		chooseFiles.setActionCommand("chooseFiles");
		chooseFiles.addActionListener(this);
		sendFiles.setActionCommand("sendFiles");
		sendFiles.addActionListener(this);
		backupFiles.setActionCommand("backupFiles");
		backupFiles.addActionListener(this);
		removeFilesFromServer.setActionCommand("removeSelectedFilesFromServer");
		removeFilesFromServer.addActionListener(this);

		JMenuItem choosePort = new JMenuItem("Choose Server Port");
		JMenuItem chooseIP = new JMenuItem("Choose Server IP");
		JMenuItem connectToServer = new JMenuItem("Connect To Server");
		JMenuItem registerOnServer = new JMenuItem("Register On Server");
		JMenuItem disconnectFromServer = new JMenuItem("Disconnect From Server");

		serverPrefsMenu.add(chooseIP);
		serverPrefsMenu.add(choosePort);
		serverPrefsMenu.add(connectToServer);
		serverPrefsMenu.add(registerOnServer);
		serverPrefsMenu.add(disconnectFromServer);

		chooseIP.setActionCommand("chooseIP");
		chooseIP.addActionListener(this);
		choosePort.setActionCommand("choosePort");
		choosePort.addActionListener(this);
		connectToServer.setActionCommand("connect");
		connectToServer.addActionListener(this);
		registerOnServer.setActionCommand("register");
		registerOnServer.addActionListener(this);
		disconnectFromServer.setActionCommand("disconnect");
		disconnectFromServer.addActionListener(this);

		JMenuItem aboutMe = new JMenuItem("About");

		aboutMenu.add(aboutMe);
		aboutMe.setActionCommand("about");
		aboutMenu.addActionListener(this);

		menuBar.setVisible(true);

		this.setJMenuBar(menuBar);

		//creating table of selected files
		localTableModel = new FileTableModel();
		chosenFilesTable = new JTable(localTableModel);
		chosenFilesTable.setFillsViewportHeight(true);

		JScrollPane localTableScrollPane = new JScrollPane();
		localTableScrollPane.setViewportView(chosenFilesTable);
		localTableScrollPane.setRowHeaderView(chosenFilesTable);
		localTableScrollPane.setVisible(true);
		localTableScrollPane.setBounds(50, 50, 600, 100);
		contentPane.add(localTableScrollPane);
		
		//creating table of server files
		serverTableModel = new FileTableModel();
		serverFilesTable = new JTable(serverTableModel);
		serverFilesTable.setFillsViewportHeight(true);
		JScrollPane serverTableScrollPane = new JScrollPane();
		serverTableScrollPane.setViewportView(serverFilesTable);
		
		serverTableScrollPane.setRowHeaderView(serverFilesTable);
		serverTableScrollPane.setVisible(true);
		serverTableScrollPane.setBounds(50, 200, 600, 100);
		contentPane.add(serverTableScrollPane);
		

		//creating buttons
		chooseFilesButton = new JButton("Select Local Files");
		chooseFilesButton.setVerticalTextPosition(AbstractButton.CENTER);
		chooseFilesButton.setHorizontalTextPosition(AbstractButton.LEADING);
		chooseFilesButton.setActionCommand("chooseFiles");
		chooseFilesButton.setBounds(50, 350, 300, 50);
		chooseFilesButton.setVisible(true);
		chooseFilesButton.addActionListener(this);
		contentPane.add(chooseFilesButton);

		sendToServerButton = new JButton("Send Files To Server");
		sendToServerButton.setVerticalTextPosition(AbstractButton.CENTER);
		sendToServerButton.setHorizontalTextPosition(AbstractButton.LEADING);
		sendToServerButton.setActionCommand("sendFiles");
		sendToServerButton.setBounds(450, 350, 300, 50);
		sendToServerButton.setVisible(true);
		sendToServerButton.addActionListener(this);
		contentPane.add(sendToServerButton);

		backupFilesButton = new JButton("Backup Files From Server");
		backupFilesButton.setVerticalTextPosition(AbstractButton.CENTER);
		backupFilesButton.setHorizontalTextPosition(AbstractButton.LEADING);
		backupFilesButton.setActionCommand("backupFiles");
		backupFilesButton.setBounds(450, 410, 300, 50);
		backupFilesButton.setVisible(true);
		backupFilesButton.addActionListener(this);
		contentPane.add(backupFilesButton);

		removeFilesButton = new JButton("Remove Selected Files From List");
		removeFilesButton.setVerticalTextPosition(AbstractButton.CENTER);
		removeFilesButton.setHorizontalTextPosition(AbstractButton.LEADING);
		removeFilesButton.setActionCommand("removeFiles");
		removeFilesButton.setBounds(50, 410, 300, 50);
		removeFilesButton.setVisible(true);
		removeFilesButton.addActionListener(this);
		contentPane.add(removeFilesButton);

		this.setContentPane(contentPane);
		this.pack();
		this.setLocationByPlatform(true);
		this.setSize(WIDTH, HEIGHT);
		this.setVisible(true);

		
		try {
			//Load configuration file
			Reader reader = new Reader("settings.txt");
			
			// read configuration file
			if (reader.getInputFile()!=null) {
				serverIP = reader.getInputFile().elementAt(0);
				if (serverIP == null) serverIP = new String("localhost"); 
				serverPort = reader.getInputFile().elementAt(1);
				if (serverPort == null) serverPort = new String ("1099");
				reader.getInputFile().remove(0);
				reader.getInputFile().remove(0);
				for (String s : reader.getInputFile()) {
					String[] str = s.split("___");
					
					File f = new File(str[0]);
					selectedFiles.add(f);
					logController.addLine(new Date().toGMTString()
							+ "  :  Data from settings file: \n" + f.getAbsolutePath());
					Vector<String> v = new Vector<String>();
					v.add(f.getName());
					Date d = new Date(f.lastModified());
					v.add(d.toString());
					try{
						if (str[1]!= "null") {
							selectedFilesMd5.add(selectedFiles.indexOf(f), str[1]);
							v.add(str[1]);
						}
						else {
							v.add("");
						}
					}catch (Exception e) {	}
					localTableModel.add(v);
					this.repaint();
				}
			}
			else {
				serverIP = new String("localhost");
				serverPort = new String("1099");
			}
		}
		catch (Exception e) {
			logController.addLine("Error while reading settings file, server IP and port set to localhost:1099");
			serverIP = new String("localhost");
			serverPort = new String("1099");
		}
		
		
		
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand() == "chooseFiles") {
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(true);
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				for (File f : chooser.getSelectedFiles()) {
					selectedFiles.add(f);
					logController.addLine(new Date().toGMTString()
							+ "  :  Selected File: \n" + f.getAbsolutePath());
					Vector<String> v = new Vector<String>();
					v.add(f.getName());
					Date d = new Date(f.lastModified());
					v.add(d.toString());
					// empty md5 - just of now
					v.add("");
					localTableModel.add(v);
					if (md5gen == null) { // if thread wasn't created yet
						logController.addLine("Started calculating md5 for "+ f.getName());
						md5gen = new Md5Generator(f.getAbsolutePath());
						md5gen.addListener(this);
						md5gen.start();
					} else if (!md5gen.isAlive()) { // if thread was created but it's not working
						logController.addLine("Started calculating md5 for "+ f.getName());
						md5gen = new Md5Generator(f.getAbsolutePath());
						md5gen.addListener(this);
						md5gen.start();
					} else { //if thread is working
						logController.addLine("Calculating Md5 for " + f. getName() + " queued");
						md5gen.addToQueue(f.getAbsolutePath());
					//	md5gen.isQueued = true;
					}
				}
			}
		}
		if (ae.getActionCommand() == "sendFiles") {
			if (this.username != null) {
				if (server !=null) {
					//if thread wasn't initialized yet
					if(fileSender == null) {
						logController.addLine(new Date().toGMTString()
								+ "  :  Sending files...");
						fileSender = new FileSender(username, server, selectedFiles);
						fileSender.addListener(this);
						fileSender.start();
					}
					if (!fileSender.isAlive()) {
						logController.addLine(new Date().toGMTString()
								+ "  :  Sending files...");
						fileSender = new FileSender(username, server, selectedFiles);
						fileSender.addListener(this);
						fileSender.start();
					}
					else {
						logController.addLine(new Date().toGMTString() + "  :  Files currently transferred to server, please wait");
					}
				} else logController.addLine("Not connected to Server!");
			} else {
				logController.addLine("You're not logged in!");
			}
		}
		
		if (ae.getActionCommand() == "backupFiles") {
			Vector<String> v = new Vector<String>();
			int[] selRows = serverFilesTable.getSelectedRows();
			
			for (int i : selRows) {
				v.add((String)serverTableModel.getValueAt(i, 0));
			}
			try {
				if (!v.isEmpty()) {
					logController.addLine("Writing selected files");
					FileReceiver receiver = new FileReceiver(username, server, v);
					receiver.addListener(this);
					receiver.start();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			
		}

		if (ae.getActionCommand() == "removeFiles") {
			int selRow[] = chosenFilesTable.getSelectedRows();
			try {
				for (int i : selRow) {
					logController.addLine(new Date().toGMTString()
							+ "  :  Removed File: \n" + selectedFiles.get(i).getAbsolutePath());
					localTableModel.removeRowAtIndex(i);
					selectedFiles.remove(i);
					selectedFilesMd5.remove(i);
					repaint();
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				
			}
		}

		if (ae.getActionCommand() == "chooseIP") {
			this.serverIP = (String) JOptionPane.showInputDialog(this,
					"Enter server IP", "Choose IP", JOptionPane.PLAIN_MESSAGE);
			if (!this.isProperIP()) {
				this.logController.addLine("Wrong IP Entered: " + serverIP);
				JOptionPane.showMessageDialog(this,
						"Wrong IP Entered! Try Again!\nServer IP set to default (localhost)",
								"Warning", JOptionPane.ERROR_MESSAGE);
				serverIP = "localhost";
			}
			this.logController.addLine(new Date().toGMTString()
					+ "  :  Server IP set:\n" + this.serverIP);
		}
		if (ae.getActionCommand() == "choosePort") {
			this.serverPort = (String) JOptionPane.showInputDialog(this,
					"Enter server port", "Choose Port",
					JOptionPane.PLAIN_MESSAGE);
			if (!this.isProperPort()) {
				this.logController.addLine("Wrong Port Entered: " + serverPort);
				JOptionPane.showMessageDialog(this,
						"Wrong Port Entered! Try Again!\nServer port set to default (1099)",
								"Warning", JOptionPane.ERROR_MESSAGE);
				serverPort = "1099";
			}
			this.logController.addLine(new Date().toGMTString()
					+ "  :  New server port entered:\n" + this.serverPort);
		}
		if (ae.getActionCommand() == "about") {

		}
		
		if (ae.getActionCommand() == "connect") {
			try {
				Remote remote = Naming.lookup("rmi://"+serverIP+":"+serverPort+"/BackuperServer");
				if (remote instanceof BackuperInterface) server = (BackuperInterface)remote;
				LogInWindow logInWindow = new LogInWindow(this, server);
				logInWindow.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (ae.getActionCommand() == "register") {
			try {
				Remote remote = Naming.lookup("rmi://"+serverIP+":"+serverPort+"/BackuperServer");
				if (remote instanceof BackuperInterface) server = (BackuperInterface)remote;
				RegisterWindow registerWindow = new RegisterWindow(this, server);
				registerWindow.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (ae.getActionCommand() == "removeSelectedFileFromServer") {
			Vector<String> v = new Vector<String>();
			int[] selRows = serverFilesTable.getSelectedRows();
			
			for (int i : selRows) {
				try {
					server.removeSelectedFile(username,(String)serverTableModel.getValueAt(i, 0));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			repaint();
		}
		if (ae.getActionCommand() == "disconnect") {
			try {
				server.disconnect(username);
				this.username = null;
				logController.addLine("Successfully logged out from server");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the serverIP
	 */
	public String getServerIP() {
		return serverIP;
	}

	/**
	 * @param serverIP
	 *            the serverIP to set
	 */
	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	/**
	 * @return the serverPort
	 */
	public String getServerPort() {
		return serverPort;
	}
	
	public Vector<String> getSettings() {
		Vector<String> settings = new Vector<String>();
		settings.add(serverIP);
		settings.add(serverPort);
		for (File f : selectedFiles) {
			try {
				if (selectedFilesMd5.elementAt(selectedFiles.indexOf(f))!=null) {
				settings.add(f.getAbsolutePath() + "___" + selectedFilesMd5.elementAt(selectedFiles.indexOf(f)));
				}
				else settings.add(f.getAbsolutePath() + "___null");
			} catch (Exception e) {
				settings.add(f.getAbsolutePath() + "___null");
			} 
			}
		return settings;
	}

	/**
	 * @param serverPort
	 *            the serverPort to set
	 */
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	private boolean isProperIP() {
		boolean t = true;
		String[] s = this.serverIP.split("\\.");
		for (String ss : s) {
			try {
				int i = Integer.parseInt(ss);
				if (!((i == 0) || (i > 0 && i < 256)))
					t = false;
			} catch (NumberFormatException e) {
				t = false;
			}
		}
		if (this.serverIP.equals("localhost"))	t = true;
		return t;
	}

	private boolean isProperPort() {
		boolean t = true;
		try {
			int i = Integer.parseInt(serverPort);
			if (!((i == 0) || (i > 0)))
				return false;
		} catch (NumberFormatException e) {
			return false;
		}
		return t;
	}
	
	public LogController getLogController(){
		return logController;
	}

	/**
	 * @return the selectedFiles
	 */
	public Vector<File> getSelectedFiles() {
		return selectedFiles;
	}

	/**
	 * @param selectedFiles the selectedFiles to set
	 */
	public void setSelectedFiles(Vector<File> selectedFiles) {
		this.selectedFiles = selectedFiles;
	}

	/**
	 * @return the localTableModel
	 */
	public FileTableModel getLocalTableModel() {
		return localTableModel;
	}

	/**
	 * @param localTableModel the localTableModel to set
	 */
	public void setLocalTableModel(FileTableModel localTableModel) {
		this.localTableModel = localTableModel;
	}

	/**
	 * @param logController the logController to set
	 */
	public void setLogController(LogController logController) {
		this.logController = logController;
	}
	
	/**
	 * @param logController the logController to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @param logController the logController to set
	 */
	public String getUsername() {
		return username;
	}

	public void updateFilesOnServerTable() {
		try {
			HashMap<String,Long> map = server.getMapOfFilesOnServer(username);
			
			for (Entry<String, Long> entry : map.entrySet()) {
				Vector<String> v = new Vector<String>();
				v.add(entry.getKey());
				v.add(new Date(entry.getValue()).toString());
				v.add("");
				serverTableModel.add(v);
			}
			
			repaint();
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}	
	}

	/* (non-Javadoc)
	 * @see utils.ThreadCompleteListener#notifyOfThreadComplete(java.lang.Thread)
	 */
	@Override
	public void notifyOfThreadComplete(Thread thread) {
		
	if(thread instanceof Md5Generator) {
		for (File f : selectedFiles) {
			if (f.getAbsolutePath().equals(((Md5Generator) thread).getFilePath())) {
				selectedFilesMd5.add(selectedFiles.indexOf(f), ((Md5Generator) thread).getMd5());
				this.logController.addLine("Finished calculating md5 for file " + f.getName());
				this.logController.addLine(((Md5Generator) thread).getMd5());
				localTableModel.setValueAt(((Md5Generator) thread).getMd5(), selectedFiles.indexOf(f), 2);
				repaint();
				}
			}
		}
	if (thread instanceof FileSender) {
		updateFilesOnServerTable();
		}
	if (thread instanceof FileReceiver) {
		logController.addLine("Finished writing files");
	}
	}
	
}
