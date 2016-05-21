import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollBar;

public class Controls extends JPanel implements AdjustmentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4835598359100552657L;
	private JLabel begin;
	private JScrollBar startVal;
	private JLabel end;
	private Main mainClass;
	private JButton save;
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
		
		save = new JButton("save");
		add(save,BorderLayout.SOUTH);
		save.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				mc.save();
				
			}
		});
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
