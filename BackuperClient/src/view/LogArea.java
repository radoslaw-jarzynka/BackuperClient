/**
 * created on 20:44:25 9 paź 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package view;

import javax.swing.JTextArea;

public class LogArea extends JTextArea {
	
	private static final long serialVersionUID = 1L;
	

	public LogArea(int x, int y, int width, int height) {
		super();
		this.setEditable(false);
		this.setBounds(x, y, width, height);
	}
	
	public LogArea() {
		super();
		this.setEditable(false);
	}

}

