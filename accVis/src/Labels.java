import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Labels extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1751950830080168865L;

	@XmlElement
	private List<Label> labels;
	private Main panel;

	public Labels() {

	}

	public void setMain(Main m) {
		panel = m;
	}

	public Labels(Main m) {
		panel = m;
		labels = new ArrayList<>();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {

		return labels.size() + 1;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (row == labels.size())
			return "";
		else if (row < labels.size()) {
			return labels.get(row).getName();
		}
		return null;
	}

	public void setValueAt(Object o, int row, int col) {
		if (row > labels.size())
			return;
		if (row == labels.size()) {
			labels.add(new Label(o.toString()));
			fireTableDataChanged();
			return;
		}
		labels.get(row).setName(o.toString());

	}

	public String getColumnName(int col) {
		switch (col) {
		case 0:
			return "Name";
		default:
			return "N/A";
		}

	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}

	public List<Label> getLabels() {
		return labels;
	}
}
