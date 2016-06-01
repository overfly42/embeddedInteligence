import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Labels extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1751950830080168865L;

	public class ColorChooseCellRenderer extends DefaultTableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8435235590752471868L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component val = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (column == 1 && value != null && Color.class.isAssignableFrom(value.getClass())
					&& JLabel.class.isAssignableFrom(val.getClass())) {
				// Obvious, this is a color
				Color c = (Color) value;
				JLabel lbl = (JLabel) val;
				val.setBackground(c);
				lbl.setText("");
			}
			return val;
		}
	}

	@XmlElement
	private List<Label> labels;
	private Main panel;

	ColorChooseCellRenderer cccr;

	public ColorChooseCellRenderer getCellRenderer() {
		return cccr;
	}

	public Labels() {
		labels = new ArrayList<>();
		cccr = new ColorChooseCellRenderer();

	}

	public void setMain(Main m) {
		panel = m;
	}

	public Labels(Main m) {
		this();
		setMain(m);

	}

	@Override
	public int getColumnCount() {
		if (panel == null)
			return 1;
		return Main.VALUES_LABEL.length + 2;
	}

	@Override
	public int getRowCount() {

		return labels.size() + 2;// One for name, second for color
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0) {
			if (row == labels.size())
				return "";
			else if (row < labels.size()) {
				return labels.get(row).getName();
			}
		} else if (col == 1) {
			if (row >= labels.size() || labels.get(row).getColor() == null)
				return Color.BLACK;
			return labels.get(row).getColor();
		}
		Object o;
		try {
			if (labels.size() > row)
				o = labels.get(row).getValue(getColumnName(col));
			else
				o = null;
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			o = null;
		}
		return o;
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

	public Class getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return Color.class;
		default:
			return Double.class;
		}
	}

	public String getColumnName(int col) {

		switch (col) {
		case 0:
			return "Name";
		case 1:
			return "Color";
		default:
			col -= 2;
			return panel.VALUES_LABEL[col];
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case 0:
		case 1:
			return true;
		default:
			return false;
		}
	}

	public List<Label> getLabels() {
		return labels;
	}

	public void setColor(int row) {
		if (row >= labels.size())
			return;
		Color c = JColorChooser.showDialog(panel, "Choose a new Color", labels.get(row).getColor());
		if (c == null)
			return;
		labels.get(row).setColor(c);
	}

	public org.jzy3d.colors.Color getColor(String s) {
		for (Label l : labels)
			if (s.equals(l.getName()) && l.getColor() != null)
				return new org.jzy3d.colors.Color(l.getColor().getRed(), l.getColor().getGreen(), l.getColor().getBlue());
		return org.jzy3d.colors.Color.BLACK;
	}

	public Label getLabel(String name) {
		for (Label l : labels)
			if (l.getName().equals(name))
				return l;
		return null;
	}

	@Override
	public void fireTableDataChanged() {
		super.fireTableDataChanged();
		System.out.println("Fire");
	}
}
