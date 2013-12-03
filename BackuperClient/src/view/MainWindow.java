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
//klasa glownego okna aplikacji
public class MainWindow extends JFrame implements ActionListener, ThreadCompleteListener {

	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 700;
	private static final int HEIGHT = 700;

	private String serverIP;
	private String serverPort;
	
	private JPanel contentPane; 

	//Watki
	Md5Generator md5gen;
	FileSender fileSender;
	
	// kontroler logu, ktorym aplikacja komunikuje sie z klientem
	private LogController logController;
	//wektory zaznaczonych lokalnych plikow i ich funkcji skrótu md5
	private Vector<File> selectedFiles;
	private Vector<String> selectedFilesMd5;
	
	//czy aplikacja jest polaczona z serwerem?
	private boolean isConnected;
	
	//interfejs serwera
	private BackuperInterface server;
	//nazwa aktualnie zalogowanego uzytkownika
	private String username;

	//tabele plikow lokalnych i na serwerze
	private JTable chosenFilesTable;
	private JTable serverFilesTable;

	//model plikow potrzebny do tworzenia tabel
	// @SuppressWarnings("rawtypes")
	private FileTableModel localTableModel;
	private FileTableModel serverTableModel;

	//przyciski
	private JButton chooseFilesButton;
	private JButton removeLocalFilesButton;
	private JButton removeServerFilesButton;
	private JButton sendToServerButton;
	private JButton backupFilesButton;
	private JButton checkFileButton;

	//labele
	private JLabel selectedFilesLabel;
	private JLabel serverFilesLabel;

	//pasek menu
	private JMenuBar menuBar;

	
	//konstruktor
	public MainWindow() {
		super("Backuper!");
		//aplikacja na poczatku nie jest polaczona i nikt nie jest zalogowany
		isConnected = false;
		username = null;
		//wczytanie polisy bezpieczenstwa
		System.setProperty("java.security.policy", "policy");
		System.setSecurityManager(new RMISecurityManager());
		
		//dodanie window listenera
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				//przy wylaczeniu aplikacji zapisanie logu do pliku i disconnect z serwerem
				((MainWindow) windowEvent.getComponent()).getLogController().saveLogToFile();
				Writer w = new Writer(((MainWindow) windowEvent.getComponent()).getSettings());
				if(isConnected)
					try {
						server.disconnect(username);
					} catch (RemoteException e) {
						logController.addLine("Something went wrong " + e.getMessage());
					}
				System.exit(0);

			}
		});
		
		this.setResizable(false);
		
		//tworzenie wektorow plikow
		selectedFiles = new Vector<File>();
		selectedFilesMd5 = new Vector<String>();

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setLayout(null);

		//scrollbar uzywany przez log
		JScrollPane logScrollPane = new JScrollPane();
		this.logController = new LogController(0, 0, 680, 140);
		logScrollPane.setViewportView(logController.getLogArea());
		logScrollPane.setBounds(10, 510, 680, 140);
		logScrollPane.setAutoscrolls(true);
		contentPane.add(logScrollPane);

		
		//tworzenie paska menu
		menuBar = new JMenuBar();

		JMenu filesMenu = new JMenu("Actions");
		JMenu serverPrefsMenu = new JMenu("Server");

		menuBar.add(filesMenu);
		menuBar.add(serverPrefsMenu);
		
		JMenuItem chooseFiles = new JMenuItem("Select Local Files");
		JMenuItem sendFiles = new JMenuItem("Send Files To Server");
		JMenuItem backupFiles = new JMenuItem("Backup Files");
		JMenuItem removeFilesFromServer = new JMenuItem("Remove Selected File From Server");
		JMenuItem checkFileIntegrity = new JMenuItem("Check If Selected File Was Sent Properly");

		filesMenu.add(chooseFiles);
		filesMenu.add(sendFiles);
		filesMenu.add(backupFiles);
		filesMenu.add(removeFilesFromServer);
		filesMenu.add(checkFileIntegrity);
		
		chooseFiles.setActionCommand("chooseFiles");
		chooseFiles.addActionListener(this);
		sendFiles.setActionCommand("sendFiles");
		sendFiles.addActionListener(this);
		backupFiles.setActionCommand("backupFiles");
		backupFiles.addActionListener(this);
		removeFilesFromServer.setActionCommand("removeSelectedFileFromServer");
		removeFilesFromServer.addActionListener(this);
		checkFileIntegrity.setActionCommand("checkFileIntegrity");
		checkFileIntegrity.addActionListener(this);

		JMenuItem choosePort = new JMenuItem("Choose Server Port");
		JMenuItem chooseIP = new JMenuItem("Choose Server IP");
		JMenuItem connectToServer = new JMenuItem("Connect To Server");
		JMenuItem registerOnServer = new JMenuItem("Register On Server");

		serverPrefsMenu.add(chooseIP);
		serverPrefsMenu.add(choosePort);
		serverPrefsMenu.add(connectToServer);
		serverPrefsMenu.add(registerOnServer);

		chooseIP.setActionCommand("chooseIP");
		chooseIP.addActionListener(this);
		choosePort.setActionCommand("choosePort");
		choosePort.addActionListener(this);
		connectToServer.setActionCommand("connect");
		connectToServer.addActionListener(this);
		registerOnServer.setActionCommand("register");
		registerOnServer.addActionListener(this);

		menuBar.setVisible(true);

		this.setJMenuBar(menuBar);
		
		//tworzenie labela nad tablica lokalnych plikow
		selectedFilesLabel = new JLabel("Selected Local Files", JLabel.CENTER);
		selectedFilesLabel.setVerticalTextPosition(JLabel.CENTER);
		selectedFilesLabel.setHorizontalTextPosition(JLabel.CENTER);
		selectedFilesLabel.setBounds(250, 15, 200, 20);
		selectedFilesLabel.setVisible(true);
		contentPane.add(selectedFilesLabel);
		
		//tworzenie tabeli lokalnych plikow
		localTableModel = new FileTableModel();
		chosenFilesTable = new JTable(localTableModel);
		chosenFilesTable.setFillsViewportHeight(true);

		JScrollPane localTableScrollPane = new JScrollPane();
		localTableScrollPane.setViewportView(chosenFilesTable);
		localTableScrollPane.setVisible(true);
		localTableScrollPane.setBounds(50, 50, 600, 100);
		contentPane.add(localTableScrollPane);
		
		//tworzenie labela nad tablica plikow na serwerze
		serverFilesLabel = new JLabel("Files On Server", JLabel.CENTER);
		serverFilesLabel.setVerticalTextPosition(JLabel.CENTER);
		serverFilesLabel.setHorizontalTextPosition(JLabel.CENTER);
		serverFilesLabel.setBounds(250, 165, 200, 20);
		serverFilesLabel.setVisible(true);
		contentPane.add(serverFilesLabel);
		
		//tworzenie tabeli plikow na serwerze
		serverTableModel = new FileTableModel();
		serverFilesTable = new JTable(serverTableModel);
		serverFilesTable.setFillsViewportHeight(true);
		JScrollPane serverTableScrollPane = new JScrollPane();
		serverTableScrollPane.setViewportView(serverFilesTable);
		serverTableScrollPane.setVisible(true);
		serverTableScrollPane.setBounds(50, 200, 600, 100);
		contentPane.add(serverTableScrollPane);
		

		//tworzenie przyciskow
		chooseFilesButton = new JButton("Select Local Files");
		chooseFilesButton.setVerticalTextPosition(AbstractButton.CENTER);
		chooseFilesButton.setHorizontalTextPosition(AbstractButton.LEADING);
		chooseFilesButton.setActionCommand("chooseFiles");
		chooseFilesButton.setBounds(50, 325, 250, 50);
		chooseFilesButton.setVisible(true);
		chooseFilesButton.addActionListener(this);
		contentPane.add(chooseFilesButton);
		
		removeLocalFilesButton = new JButton("Remove Selected Local File");
		removeLocalFilesButton.setVerticalTextPosition(AbstractButton.CENTER);
		removeLocalFilesButton.setHorizontalTextPosition(AbstractButton.LEADING);
		removeLocalFilesButton.setActionCommand("removeFiles");
		removeLocalFilesButton.setBounds(50, 385, 250, 50);
		removeLocalFilesButton.setVisible(true);
		removeLocalFilesButton.addActionListener(this);
		contentPane.add(removeLocalFilesButton);
		
		removeServerFilesButton = new JButton("Remove Selected Server File");
		removeServerFilesButton.setVerticalTextPosition(AbstractButton.CENTER);
		removeServerFilesButton.setHorizontalTextPosition(AbstractButton.LEADING);
		removeServerFilesButton.setActionCommand("removeSelectedFileFromServer");
		removeServerFilesButton.setBounds(50, 445, 250, 50);
		removeServerFilesButton.setVisible(true);
		removeServerFilesButton.addActionListener(this);
		contentPane.add(removeServerFilesButton);

		sendToServerButton = new JButton("Send Files To Server");
		sendToServerButton.setVerticalTextPosition(AbstractButton.CENTER);
		sendToServerButton.setHorizontalTextPosition(AbstractButton.LEADING);
		sendToServerButton.setActionCommand("sendFiles");
		sendToServerButton.setBounds(400, 325, 250, 50);
		sendToServerButton.setVisible(true);
		sendToServerButton.addActionListener(this);
		contentPane.add(sendToServerButton);

		backupFilesButton = new JButton("Backup Files From Server");
		backupFilesButton.setVerticalTextPosition(AbstractButton.CENTER);
		backupFilesButton.setHorizontalTextPosition(AbstractButton.LEADING);
		backupFilesButton.setActionCommand("backupFiles");
		backupFilesButton.setBounds(400, 385, 250, 50);
		backupFilesButton.setVisible(true);
		backupFilesButton.addActionListener(this);
		contentPane.add(backupFilesButton);
		
		checkFileButton = new JButton("Check If File Was Sent Properly");
		checkFileButton.setVerticalTextPosition(AbstractButton.CENTER);
		checkFileButton.setHorizontalTextPosition(AbstractButton.LEADING);
		checkFileButton.setActionCommand("checkFileIntegrity");
		checkFileButton.setBounds(400, 445, 250, 50);
		checkFileButton.setVisible(true);
		checkFileButton.addActionListener(this);
		contentPane.add(checkFileButton);

		this.setContentPane(contentPane);
		this.pack();
		this.setLocationByPlatform(true);
		this.setSize(WIDTH, HEIGHT);
		this.setVisible(true);

		
		try {
			//proba wczytania pliku konfiguracyjnego
			Reader reader = new Reader("settings.txt");
			
			//czytanie pliku konfiguracyjnego
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
					if(f.exists()) {
						//jesli plik istnieje - dodaj go, jak nie to wpisz w logu ze juz nie istnieje
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
					} else {
						logController.addLine("File " + f.getName() +" does not exist any more :<");
					}
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
		//dodanie mojego podpisu do logu
		logController.addLine("Author: Radosław Jarzynka");
		logController.addLine("miniProjekt OPA 13Z = System zdalnego archiwizowania plików");
		logController.addLine("25 XI 2013, Politechnika Warszawska");
		
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public void actionPerformed(ActionEvent ae) {
		//gdy wywolano akcje 'wybierz plik'
		if (ae.getActionCommand() == "chooseFiles") {
			//tworzenie obiektu JFileChooser
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(true);
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				for (File f : chooser.getSelectedFiles()) {
					//dodanie plikow
					selectedFiles.add(f);
					logController.addLine(new Date().toGMTString()
							+ "  :  Selected File: \n" + f.getAbsolutePath());
					Vector<String> v = new Vector<String>();
					v.add(f.getName());
					Date d = new Date(f.lastModified());
					v.add(d.toString());
					//na razie dodanie do pliku pustej wartosci md5
					v.add("");
					localTableModel.add(v);
					//liczenie md5
					if (md5gen == null) { // jesli watek nie zostal uruchomiony
						logController.addLine("Started calculating md5 for "+ f.getName());
						md5gen = new Md5Generator(f.getAbsolutePath());
						md5gen.addListener(this);
						md5gen.start();
					} else if (!md5gen.isAlive()) { // jesli watek zostal uruchomiony ale juz nie dziala
						logController.addLine("Started calculating md5 for "+ f.getName());
						md5gen = new Md5Generator(f.getAbsolutePath());
						md5gen.addListener(this);
						md5gen.start();
					} else { //jesli watek dziala
						logController.addLine("Calculating Md5 for " + f. getName() + " queued");
						md5gen.addToQueue(f.getAbsolutePath());
					//	md5gen.isQueued = true;
					}
				}
			}
		}
		//gdy wywolano akcje 'wyslij plik'
		if (ae.getActionCommand() == "sendFiles") {
			if (this.username != null) {
				if (server !=null) {
					try {
						Vector<File> filesToSend = new Vector<File>();
						//pobranie plikow z serwera
						HashMap<String,Long> map = server.getMapOfFilesOnServer(username);
						for (File f : selectedFiles) {
							//sprawdzamy czy plik jest juz na serwerze, jesli nie to wysylamy. jesli jest to sprawdzamy jego lastmodified
							//jesli chcemy wyslac nowszy to wysylamy, jak starszy badz niezmieniony to nie
							if (map.containsKey(f.getName())) {
								if (f.lastModified() != map.get(f.getName())) {
									logController.addLine(f.getName() + " was updated since last update, sending new version");
									filesToSend.add(f);
								} else {
									logController.addLine(f.getName() + " is already on server");
								}
							} else {
								logController.addLine(f.getName() + " is not on server and it will be sent");
								filesToSend.add(f);	
							}
						}
						//jesli watek wysylania nie zostal zainicjowany
						if(fileSender == null) {
							logController.addLine(new Date().toGMTString()
									+ "  :  Sending files...");
							fileSender = new FileSender(username, server, filesToSend);
							fileSender.addListener(this);
							fileSender.start();
						}
						//jesli watek zsotal zainicjowany, ale nie dziala
						if (!fileSender.isAlive()) {
							logController.addLine(new Date().toGMTString()
									+ "  :  Sending files...");
							fileSender = new FileSender(username, server, filesToSend);
							fileSender.addListener(this);
							fileSender.start();
						}
						//jesli watek aktualnie dziala - wyrzuc powiadomienie
						else {
							logController.addLine(new Date().toGMTString() + "  :  Files are currently being transferred to server, please wait");
						}
					} catch (RemoteException e) {logController.addLine("Something went wrong " + e.getMessage());}
				} else logController.addLine("Not connected to Server!");
			} else {
				logController.addLine("You're not logged in!");
			}
		}
		//gdy wywolano akcje 'pobierz pliki'
		if (ae.getActionCommand() == "backupFiles") {
			if (this.username != null) {
				if (server !=null) {
					Vector<String> v = new Vector<String>();
					//wez zaznaczone pliki z tabeli serwera
					int[] selRows = serverFilesTable.getSelectedRows();
					
					for (int i : selRows) {
						v.add((String)serverTableModel.getValueAt(i, 0));
					}
					try {
						if (!v.isEmpty()) {
							//odpal watek pobierania plikow z serwera
							logController.addLine("Writing selected files");
							FileReceiver receiver = new FileReceiver(username, server, v);
							receiver.addListener(this);
							receiver.start();
						}
					} catch(Exception e) {
						logController.addLine("Something went wrong " + e.getMessage());
					}
					
				}
			}
		}
		//gdy wywolano akcje 'usun plik lokalny'
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
		//gdy wywolano akcje 'wybierz ip' - tworzenie okna pobrania adresu ip serwera
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
		//gdy wywolano akcje 'wybierz port' - tworzenie okna pobrania numeru portu serwera
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
		//gdy wybrano akcje - polacz z serwerem - tworzenie okna logowania
		if (ae.getActionCommand() == "connect") {
			try {
				Remote remote = Naming.lookup("rmi://"+serverIP+":"+serverPort+"/BackuperServer");
				if (remote instanceof BackuperInterface) server = (BackuperInterface)remote;
				LogInWindow logInWindow = new LogInWindow(this, server);
				logInWindow.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
				logController.addLine("Server is offline");;
			}
		}
		//gdy wywolano akcje - zarejestruj na serwerze - tworzenie okna rejestracji
		if (ae.getActionCommand() == "register") {
			try {
				Remote remote = Naming.lookup("rmi://"+serverIP+":"+serverPort+"/BackuperServer");
				if (remote instanceof BackuperInterface) server = (BackuperInterface)remote;
				RegisterWindow registerWindow = new RegisterWindow(this, server);
				registerWindow.setVisible(true);
			} catch (Exception e) {
				logController.addLine("Server is offline");;
			}
		}
		//gdy wywolano akcje 'usun pliki z serwera'
		if (ae.getActionCommand() == "removeSelectedFileFromServer") {
			if (this.username != null) {
				if (server !=null) {
					//wez zaznaczone wiersze z tabeli serwera
					int[] selRows = serverFilesTable.getSelectedRows();
					
					for (int i : selRows) {
						try {
							server.removeSelectedFile(username,(String)serverTableModel.getValueAt(i, 0));
							logController.addLine("Removing file " + (String)serverTableModel.getValueAt(i, 0) + " from server");
						} catch (Exception e) {
							logController.addLine("Something went wrong " + e.getMessage());
						}
					}
					try {
						updateFilesOnServerTable();
					} catch (Exception e) {
						logController.addLine("Something went wrong " + e.getMessage());
					}
					repaint();
				}
			}
		}
		//gdy wywolano akcje 'sprawdz integralnosc wyslanego pliku'
		if (ae.getActionCommand() == "checkFileIntegrity") {
			if (this.username != null) {
				if (server !=null) {
					int[] selRows = serverFilesTable.getSelectedRows();
					for (int i : selRows) {
						try {
							//pobierz md5 i porownaj je z md5 pliku na dysku
							String md5 = server.getFileMD5(username, (String)serverTableModel.getValueAt(i, 0));
							if (md5 == null) {
								logController.addLine("Server is still counting this file's md5");
							} else {
								for (File f : selectedFiles) {
									if (f.getName().equals((String)serverTableModel.getValueAt(i, 0))) {
										if (selectedFilesMd5.get(selectedFiles.indexOf(f)).equals(md5)) {
											logController.addLine("File " + f.getName() + " sent properly!");
											serverTableModel.setValueAt(md5, i, 2);
										} else {
											//gdy sie nie zgadza - wyslij ponownie
											logController.addLine("md5's are not the same, trying to send file again!");
											Vector<File> filesToSend = new Vector<File>();
											filesToSend.add(f);
											if(fileSender == null) {
												logController.addLine(new Date().toGMTString()
														+ "  :  Sending files...");
												fileSender = new FileSender(username, server, filesToSend);
												fileSender.addListener(this);
												fileSender.start();
											}
											if (!fileSender.isAlive()) {
												logController.addLine(new Date().toGMTString()
														+ "  :  Sending files...");
												fileSender = new FileSender(username, server, filesToSend);
												fileSender.addListener(this);
												fileSender.start();
											}
											else {
												logController.addLine(new Date().toGMTString() + "  :  Files are currently being transferred to server, please wait");
											}
										}
									}
								}
							}
						} catch (Exception e) {
							logController.addLine("Something went wrong " + e.getMessage());
						}
					}
					repaint();
				}
			}
		}
	}

	//gettery i settery
	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public String getServerPort() {
		return serverPort;
	}
	
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	
	//pobranie ustawien z obiektu jako wektor stringow
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

	//sprawdzenie czy IP wprowadzone do okna pobrania ip ma poprawna forme (xxx.xxx.xxx.xxx lub 'localhost')
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
	//sprawdzenie czy wprowadzono poprawny numer portu
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
	//gettery i settery
	public LogController getLogController(){
		return logController;
	}

	public Vector<File> getSelectedFiles() {
		return selectedFiles;
	}

 void setSelectedFiles(Vector<File> selectedFiles) {
		this.selectedFiles = selectedFiles;
	}

	public FileTableModel getLocalTableModel() {
		return localTableModel;
	}

	public void setLocalTableModel(FileTableModel localTableModel) {
		this.localTableModel = localTableModel;
	}

	public void setLogController(LogController logController) {
		this.logController = logController;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	//metoda odswiezajaca tabele plikow na serwerze - pobiera mape pliko z serwera i ja interpretuje
	public void updateFilesOnServerTable() {
		try {
			HashMap<String,Long> map = server.getMapOfFilesOnServer(username);
			serverTableModel.clearTable();
			for (Entry<String, Long> entry : map.entrySet()) {
				Vector<String> v = new Vector<String>();
				v.add(entry.getKey());
				v.add(new Date(entry.getValue()).toString());
				v.add("");
				serverTableModel.add(v);
			}
			
			repaint();
			
		} catch (RemoteException e) {
			logController.addLine("Something went wrong " + e.getMessage());
		}	
	}

	//metoda wywolana gdy ktorys z watkow zakonczy swoje dzialanie
	@Override
	public void notifyOfThreadComplete(Thread thread) {
		
	if(thread instanceof Md5Generator) {
		//gdy byl to watek liczacy md5 - dodaj odpowiedni wpis w tabeli plikow lokalnych
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
		//gdy byl to watek wysylajacy pliki - odswiez tabele plikow na serwerze
		updateFilesOnServerTable();
		}
	if (thread instanceof FileReceiver) {
		//gdy byl to watek zapisujacy pliki - wyswietl powiadomienie
		logController.addLine("Finished writing files");
	}
	}
	
}
