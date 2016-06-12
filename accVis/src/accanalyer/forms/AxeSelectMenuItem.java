package accanalyer.forms;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import accanalyer.forms.Main.AxeSelectMenuItem;

class AxeSelectMenuItem extends JCheckBoxMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4916361850246451578L;
	private List<AxeSelectMenuItem> allBoxes;
	private MenuElement[] selectedPath;

	public AxeSelectMenuItem(String lbl, List<AxeSelectMenuItem> all) {
		super(lbl);
		allBoxes = all;
		getModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (getModel().isArmed() && isShowing())
					selectedPath = MenuSelectionManager.defaultManager().getSelectedPath();
			}
		});

	}

	public void doClick(int time) {
		if (isSelected() || isSelectionFree())
			super.doClick(time);
		MenuSelectionManager.defaultManager().setSelectedPath(selectedPath);
	}

	private boolean isSelectionFree() {
		int free = 0;
		for (AxeSelectMenuItem asmi : allBoxes)
			if (asmi.isSelected())
				free++;
		return free < 3;
	}
}