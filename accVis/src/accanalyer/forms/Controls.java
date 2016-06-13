package accanalyer.forms;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollBar;

import accanalyer.calc.Calculator;

public class Controls extends JPanel implements AdjustmentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4835598359100552657L;
	private JLabel begin;
	private JScrollBar startVal;
	private JLabel end;
	private Main mainClass;
	private Calculator calc;
	private JButton saveData;
	private JButton saveLBL;
	private JButton trainLBL;
	private JButton selectLBL;
	private JButton deleteLBLconfig;
	private JButton deleteLabels;
	private JButton exportToWeka;
	private int numberOfEntrys;

	public Controls(Main mc,Calculator c) {

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
		calc = c;

		saveData = new JButton("save Data");
		saveData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mc.save();

			}
		});

		saveLBL = new JButton("save Labels");
		saveLBL.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mc.saveLBL();

			}
		});

		trainLBL = new JButton("train Labels");
		trainLBL.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				calc.trainLabels();

			}
		});

		selectLBL = new JButton("Label auto set");
		selectLBL.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				calc.label();

			}
		});

		deleteLBLconfig = new JButton("delete Label config");
		deleteLBLconfig.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				calc.cleanLabels();

			}
		});

		deleteLabels = new JButton("clean Labels");
		deleteLabels.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				calc.deleteLabels();

			}
		});

		exportToWeka = new JButton("Export to Weka");
		exportToWeka.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				calc.exportToWeka();

			}
		});

		JPanel tmp = new JPanel();
		tmp.setLayout(new FlowLayout());
		tmp.add(saveData);
		tmp.add(trainLBL);
		tmp.add(saveLBL);
		tmp.add(deleteLBLconfig);
		tmp.add(selectLBL);
		tmp.add(deleteLabels);
		tmp.add(exportToWeka);
		add(tmp, BorderLayout.SOUTH);
	}

	public void updateWidth(int numOfEntrys) {
		startVal.setMaximum(calc.getMaxVal());
		numberOfEntrys = numOfEntrys;
		mainClass.repaint();
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		begin.setText("" + arg0.getValue());
		end.setText("" + (arg0.getValue() + numberOfEntrys));
		calc.setStartVal(arg0.getValue());

	}
}
