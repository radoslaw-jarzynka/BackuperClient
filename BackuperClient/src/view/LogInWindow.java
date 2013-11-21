/**
 * created on 17:32:13 2 lis 2013 by Radoslaw Jarzynka
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

public class LogInWindow extends JDialog implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JButton loginButton;
    private JButton cancelButton;
    private boolean succeeded;
    private MainWindow parent;
    private BackuperInterface server;
    
    public LogInWindow(final Frame parent, final BackuperInterface server) {
        super(parent, "Login", true);
        
        this.parent = (MainWindow) parent;
        this.server = server;
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
 
        constraints.fill = GridBagConstraints.HORIZONTAL;
 
        usernameLabel = new JLabel("Username: ");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        panel.add(usernameLabel, constraints);
 
        usernameField = new JTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        panel.add(usernameField, constraints);
 
        passwordLabel = new JLabel("Password: ");
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
 
        loginButton = new JButton("Login");
        loginButton.setActionCommand("login");
        loginButton.addActionListener(this);
            
        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
 
           
        JPanel bp = new JPanel();
        bp.add(loginButton);
        bp.add(cancelButton);
 
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
 
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }
 
    public String getUsername() {
        return usernameField.getText().trim();
    }
 
    public String getPassword() {
        return new String(passwordField.getPassword());
    }
 
    public boolean isSucceeded() {
        return succeeded;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "login") {
			try {
	        	succeeded = server.logIn(getUsername(), getPassword());
	            if (succeeded) {
	                this.parent.getLogController().addLine("Successfully logged in to server as " + getUsername());
	                this.parent.setUsername(getUsername());
	                this.parent.updateFilesOnServerTable();
	                this.parent.repaint();
	                dispose();
	            } else {
	                this.parent.getLogController().addLine("Failed to log in, invalid username of password");
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
