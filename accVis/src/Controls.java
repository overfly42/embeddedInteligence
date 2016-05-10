import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JLabel;
import javax.swing.JScrollBar;

public class Controls extends JPanel implements AdjustmentListener {
	private JLabel begin;
	private JScrollBar startVal;
	private JLabel end;
	private Main mainClass;
	private int numberOfEntrys;

	public Controls(Main mc) {

		numberOfEntrys = 0;
		
		this.setLayout(new BorderLayout());
		begin = new JLabel("StartVal");
		add(begin, BorderLayout.WEST);

		startVal = new JScrollBar();
		startVal.setOrientation(JScrollBar.HORIZONTAL);
		startVal.addAdjustmentListener(this);
		add(startVal, BorderLayout.CENTER);

		end = new JLabel("EndVal");
		add(end, BorderLayout.EAST);

		mainClass = mc;
	}

	public void updateWidth(int numOfEntrys) {
		startVal.setMaximum(mainClass.getMaxVal());
		numberOfEntrys = numOfEntrys;
		mainClass.repaint();
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		begin.setText(""+arg0.getValue());
		end.setText(""+(arg0.getValue()+numberOfEntrys));
		mainClass.setStartVal(arg0.getValue());
		
	}
}
