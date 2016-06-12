package accanalyer.forms.models;

import javax.swing.table.AbstractTableModel;

import accanalyer.forms.Main;

public class VisibleDecisions extends AbstractTableModel {

	private Main main;

	/**
	 * @param main
	 */
	public VisibleDecisions(Main m) {
		main = m;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8817316726011629804L;
	public boolean[] rawData = new boolean[4];
	public boolean[] smoothData = new boolean[4];
	public boolean labelLine = true;
	public boolean[] label = new boolean[4];// 0->label, 1 lines of label

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public int getRowCount() {
		return 4;
	}

	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		if (colIndex == 0)
			if (rowIndex == 0)
				return "Show raw Data";
			else if (rowIndex == 1)
				return "Show smooth Data";
			else if (rowIndex == 2)
				return "Show Line to Label";
			else
				return "Show Label";

		colIndex--;
		switch (rowIndex) {
		case 0:
			return rawData[colIndex];
		case 1:
			return smoothData[colIndex];
		case 2:
			if (colIndex == 0)
				return labelLine;
		case 3:
			if (colIndex < 2)
				return label[colIndex];
		default:
			return null;
		}
	}

	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 0)
			return String.class;
		return Boolean.class;

	}

	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Option";
		case 1:
			return "X";
		case 2:
			return "Y";
		case 3:
			return "Z";
		case 4:
			return "|V|";
		default:
			return "N/A";
		}
	}

	public boolean isCellEditable(int rowIndex, int colIndex) {
		if (colIndex > 0)
			return true;
		return false;
	}

	public void setValueAt(Object o, int rowIndex, int colIndex) {
		colIndex--;
		switch (rowIndex) {
		case 0:
			rawData[colIndex] = (boolean) o;
			return;
		case 1:
			smoothData[colIndex] = (boolean) o;
			return;
		case 2:
			if (colIndex == 0)
				labelLine = (boolean) o;
		case 3:
			if (colIndex < 2)
				label[colIndex] = (boolean) o;
		}
		this.main.repaint();
	}
}