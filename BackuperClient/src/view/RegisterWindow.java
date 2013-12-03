/**
 * created on 18:02:56 2 lis 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import common.BackuperInterface;
//okienko rejestracji na serwerze
public class RegisterWindow extends JDialog implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField retypePasswordField;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel retypePasswordLabel;
    private JButton registerButton;
    private JButton cancelButton;
    private boolean succeeded;
    private MainWindow parent;
    private BackuperInterface server;
    //konstruktor pobierajacy frame ojca i interfejs serwera (by wiedzial gdzie sie loguje)
    public RegisterWindow(final Frame parent, final BackuperInterface server) {
        super(parent, "Register", true);
        
        this.parent = (MainWindow) parent;
        this.server = server;
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
 
        constraints.fill = GridBagConstraints.HORIZONTAL;
        //ustawienie layoutu okna
        usernameLabel = new JLabel("Set Username: ");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        panel.add(usernameLabel, constraints);
 
        usernameField = new JTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        panel.add(usernameField, constraints);
 
        passwordLabel = new JLabel("Set password: ");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        panel.add(passwordLabel, constraints);
 
        passwordField = new JPasswordField(20);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(passwordField, constraints);
        panel.setBorder(new LineBorder(Color.GRAY));

        retypePasswordLabel = new JLabel("Write it again: ");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        panel.add(retypePasswordLabel, constraints);
 
        retypePasswordField = new JPasswordField(20);
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        panel.add(retypePasswordField, constraints);
        panel.setBorder(new LineBorder(Color.GRAY));
        
        registerButton = new JButton("Register");
        registerButton.setActionCommand("register");
        registerButton.addActionListener(this);
            
        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
 
           
        JPanel bp = new JPanel();
        bp.add(registerButton);
        bp.add(cancelButton);
 
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
 
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }
    //getter nazwy uzytkownika
    public String getUsername() {
        return usernameField.getText().trim();
    }
    //pobranie hasla - haslo w obu polach musi sie zgadzac!
    public String getPassword() {
    	String s1 = new String(passwordField.getPassword());
    	String s2 = new String(retypePasswordField.getPassword());
    	if (s1.equals(s2)) 
    			return s1;
    	else return null;
    }
    //czy udalo sie zarejestrowac na serwerze?
    public boolean isSucceeded() {
        return succeeded;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "register") {
			try {
				if (getPassword() != null) {
					//proba rejestracji, jesli jest juz taki uzytkownik to sie nie uda
		        	succeeded = server.register(getUsername(), getPassword());
		            if (succeeded) {
		                this.parent.getLogController().addLine("Successfully registered as " + getUsername());
		                dispose();
		            } else {
		                this.parent.getLogController().addLine("Failed to register, username already taken");
		                dispose();
		            }
				} else  {
					this.parent.getLogController().addLine("Passwords don't match!");
					dispose();
				}
			} catch(RemoteException ex) {
				ex.printStackTrace();
			}
        }
		if (e.getActionCommand() == "cancel") {
			dispose();
		}
	}
}
