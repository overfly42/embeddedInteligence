import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

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
		// One per Axis + description
		return 4;
	}

	@Override
	public int getRowCount() {

		return Main.VALUES.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		double[] i = data.get(Main.VALUES[rowIndex]);
		if (i == null)
			return null;
		if (colIndex == 0)
			return Main.VALUES[rowIndex];
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
		}
		return "N/A";
	}

	public boolean isCellEditable(int rowIndex, int colIndex) {
		if (colIndex == 1 && Main.VALUES[rowIndex].equals(Main.WINDOW_SIZE))
			return true;
		return false;
		
	}

	public void setValueAt(Object o, int rowIndex, int colIndex) {
		double[] i = data.get(Main.VALUES[rowIndex]);
		i[--colIndex] = Double.parseDouble(o.toString());
		data.put(Main.VALUES[rowIndex], i);
		panel.setWindow((int)i[colIndex]);
	}
}
