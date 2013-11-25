/**
 * created on 13:43:21 22 paź 2013 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
package model;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class FileTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 2L;
	
	@SuppressWarnings("rawtypes")
	private Vector<Vector> tableData;
	private Vector<String> columnNames;
	
	@SuppressWarnings("rawtypes")
	public FileTableModel() {
		tableData = new Vector<Vector>();
		columnNames = new Vector<String>();
		columnNames.add("File Name");
    	columnNames.add("Last Modified");
    	columnNames.add("MD5");
	}
	
	public void add(Vector<String> v) {
		tableData.add(v);
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.size();
	}
	
	@Override
	public int getRowCount() {
		return tableData.size();
	}

	public String getColumnName(int col) {
        return columnNames.elementAt(col);
    }
	
	public boolean isCellEditable(int row, int col) {
		return false;
	}
    
	public void removeRowAtIndex(int r) {
		tableData.remove(r);
	}
	
	public void clearTable() {
		tableData.clear();
	}
	
	@Override
	public Object getValueAt(int arg0, int arg1) {
		return tableData.elementAt(arg0).elementAt(arg1);
	}
	
	public void setValueAt(String s, int arg0, int arg1) {
		tableData.elementAt(arg0).add(arg1, s);
	}
	
}
