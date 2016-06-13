package accanalyer.forms.models;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import accanalyer.data.Common;
import accanalyer.forms.Main;

public class Statistics extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8084458964223553254L;
	private JTable table;
	private Map<String, double[]> data;
	private Main panel;

	public Statistics(JTable t, Main m) {
		table = t;
		data = new HashMap<>();
		table.setModel(this);
		panel = m;
	}

	@Override
	public int getColumnCount() {
		// One per Axis + description + |v|
		return 5;
	}

	@Override
	public int getRowCount() {

		return Common.VALUES.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		double[] i = data.get(Common.VALUES[rowIndex]);
		if (i == null)
			return null;
		if (colIndex == 0)
			return Common.VALUES[rowIndex];
		colIndex--;
		if (colIndex >= i.length)
			return null;
		return panel.format(i[colIndex]);
	}

	public void setData(String ident, double[] val) {
		data.put(ident, val);
		fireTableDataChanged();
	}

	public String getColumnName(int colIndex) {
		switch (colIndex) {
		case 0:
			return "Value";
		case 1:
			return "X";
		case 2:
			return "Y";
		case 3:
			return "Z";
		case 4:
			return "|V|";
		}
		return "N/A";
	}

	public boolean isCellEditable(int rowIndex, int colIndex) {
		if (colIndex == 1 && Common.VALUES[rowIndex].equals(Common.WINDOW_SIZE))
			return true;
		return false;

	}

	public void setValueAt(Object o, int rowIndex, int colIndex) {
		double[] i = data.get(Common.VALUES[rowIndex]);
		i[--colIndex] = Double.parseDouble(o.toString());
		data.put(Common.VALUES[rowIndex], i);
		panel.setWindow((int) i[colIndex]);
	}
}
