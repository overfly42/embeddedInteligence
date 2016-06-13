package accanalyer.forms;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import accanalyer.calc.Calculator;
import accanalyer.data.Common;
import accanalyer.data.Label;
import accanalyer.forms.models.VisibleDecisions;

public class RawView extends JPanel implements MouseListener, MouseMotionListener {

	private Calculator calc;
	private Common common;
	private Controls controls;
	private VisibleDecisions vd;

	private int startAt;// Start of Data shown in Panel
	private int stopAt;// End of Data shown in Panel
	private int position;

	private List<Color> colors;

	public RawView(Calculator c, Common co, Controls con, VisibleDecisions visDis) {
		calc = c;
		common = co;
		controls = con;
		vd = visDis;
		colors = new ArrayList<>();
		colors.add(Color.GREEN);
		colors.add(Color.PINK);
		colors.add(Color.BLUE);
		colors.add(Color.ORANGE);

		startAt = 0;
		position = 0;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentResized(ComponentEvent e) {
				resized();

			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub

			}
		});

	}

	public void init() {
		calcMinMax();
		calcValPerPx();

	}

	private void calcPaintingAreas() {
		// startAt is set by slider
		stopAt = startAt + this.getWidth() - 2 * Common.BORDER;
		if (startAt > calc.time.size())
			return;
		int ms_in_window = calc.time.get(startAt) + common.window;// this is in
																	// ms
		for (int i = startAt; i < calc.time.size(); i++) {
			if (calc.time.get(i) > ms_in_window) {
				common.windowEndAt = i;
				break;
			}
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
		calcPaintingAreas();
		paintAxis(g);
		calcValPerPx();
		for (int i = 0; i < colors.size(); i++) {
			g.setColor(colors.get(i));
			if (vd.rawData[i])
				paintGraph(g, calc.convertValToPos(calc.data.get(i), false));
			if (vd.smoothData[i])
				paintGraph(g, calc.convertValToPos(calc.data.get(i), true));
		}

		Map<String, double[]> stats = calc.prepairStats();
		List<List<Double>> selectData = calc.selectData(startAt, common.windowEndAt);
		List<Integer> selectTime = calc.selectTime(startAt, common.windowEndAt);
		calc.calcData(stats, selectData, selectTime);
		double[] tmp = { common.window, 0, 0, 0 };
		stats.put(Common.WINDOW_SIZE, tmp);
		calc.populateStats(stats);

		// Draw Label Line
		if (vd.labelLine) {
			g.setColor(Color.BLACK);
			g.drawLine(position, 0, position, this.getHeight());

		}
		if (vd.label[0])
			paintLabel(g);
		if (vd.label[1])
			paintLabelIndication(g);
	}

	private void paintLabel(Graphics g) {
		if (calc.classification.isEmpty())
			return;
		String msg = Common.UNDEFINED_LABEL + " a";
		// StartAt, Startposition of Sliding Window
		// StopAt, EndPositon of SlidingWIndow
		LabelPosition lp_pre = null; // Get first label, in front of start
		LabelPosition lp_post = null;// Get Last Label relevant for Sliding
										// Window
		// lp_pre
		for (LabelPosition lp : classification) {
			if (lp.labelPos >= startAt)
				break;
			lp_pre = lp;
		}
		// lp_post
		for (LabelPosition lp : classification) {
			if (lp.labelPos > windowEndAt)
				break;
			lp_post = lp;
		}
		if (lp_pre != null && lp_post != null && lp_pre.labelName.equals(lp_post.labelName))
			msg = lp_pre.labelName + (lp_pre.t == LabelType.automatic ? " a" : " m");
		g.setColor(Color.BLACK);
		g.drawString(msg, 2 * BORDER, BORDER);
	}

	private void paintLabelIndication(Graphics g) {
		List<LabelPosition> lps = new ArrayList<>();
		for (LabelPosition lp : classification)
			if (lp.labelPos > startAt && lp.labelPos < startAt + window)
				lps.add(lp);
		g.setColor(Color.RED);
		for (LabelPosition lp : lps) {
			g.drawLine(lp.labelPos - startAt, BORDER, lp.labelPos - startAt, this.getHeight());
		}

	}

	private void paintAxis(Graphics g) {
		g.setColor(Color.BLACK);
		int Y_LENGTH = this.getHeight() - BORDER;
		int X_LENGTH = this.getWidth() - BORDER;
		g.drawLine(BORDER, BORDER, BORDER, Y_LENGTH);// Y - Axis
		g.drawLine(BORDER, (Y_LENGTH + BORDER) / 2, X_LENGTH, (Y_LENGTH + BORDER) / 2);// X-Axis
		g.drawString("" + maxVal, BORDER, BORDER);
		g.drawString("" + minVal, BORDER, Y_LENGTH);
		g.drawLine(BORDER, BORDER, 2 * BORDER, BORDER);// Marker for max val
		g.drawLine(BORDER, Y_LENGTH, 2 * BORDER, Y_LENGTH);// Marker for min val
		// Draw Timestamps
		if (time.size() < startAt)
			return;
		int t = time.get(startAt);
		int win = time.get(startAt) + window;
		boolean win_painted = false;
		stopAt = Math.min(startAt + this.getWidth(), time.size());
		for (int i = startAt, pos = BORDER; i < stopAt; i++, pos++) {
			if (time.get(i) < t + 1000)
				continue;
			t += 1000;
			g.drawLine(BORDER + pos, BORDER, BORDER + pos, Y_LENGTH);
			g.drawString("" + time.get(i), BORDER + pos, Y_LENGTH);
		}
		g.setColor(Color.CYAN);
		g.drawRect(BORDER, BORDER, windowEndAt - startAt + BORDER, Y_LENGTH - BORDER);
	}

	private void paintGraph(Graphics g, List<Integer> vals) {
		int X_LENGTH = this.getWidth();
		int[] x = new int[X_LENGTH];
		int[] y = new int[X_LENGTH];
		for (int i = 0; i < vals.size() && i < X_LENGTH; i++) {
			x[i] = i + BORDER;
			y[i] = vals.get(i);
		}
		int points = vals.size() < X_LENGTH ? vals.size() : X_LENGTH;
		g.drawPolyline(x, y, points);
	}

	private void calcValPerPx() {
		valPerPxW = (1.0 * time.size()) / Math.max(this.getWidth(), 1);
		valPerPxH = (1.0 * diff) / (1.0 * Math.max(this.getHeight() - 2 * BORDER, 1));
	}

	private void calcMinMax() {
		double max = 0;
		double min = 0;
		for (List<Double> ld : data) {
			for (Double d : ld) {
				max = d > max ? d : max;
				min = d < min ? d : min;
			}
		}
		maxVal = (int) (max + 1);
		minVal = (int) (min - 1);
		diff = Math.abs(maxVal) + Math.abs(minVal);
	}

	@Override

	public void mouseClicked(MouseEvent arg0) {
		contextMenu(arg0.getX() + startAt - BORDER, arg0.getX(), arg0.getY());
		repaint();

	}

	private void contextMenu(int absPos, int xPos, int yPos) {
		JPopupMenu pop = new JPopupMenu();
		JMenu m1 = new JMenu("Add Label");
		JMenu m2 = new JMenu("Delete Label");

		int p = position + startAt;
		// Add Labels
		for (Label l : labels.getLabels()) {
			JMenuItem mi = new JMenuItem("Set Label " + l.getName());
			mi.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					addLabel(l.getName(), p);

				}
			});
			m1.add(mi);
		}
		// Undefined Label
		{

			JMenuItem mi = new JMenuItem("Set Label " + UNDEFINED_LABEL);
			mi.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					addLabel(UNDEFINED_LABEL, p);

				}
			});
			m1.add(mi);
		}
		// remove Labels
		for (

		LabelPosition lp : classification)

		{
			JMenuItem mi = new JMenuItem("Remove " + lp.labelName + " at: " + lp.labelPos);
			mi.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					deleteLabel(lp);

				}
			});
			m2.add(mi);
		}

		pop.add(m1);
		pop.add(m2);

		pop.show(this, xPos, yPos);

	}

	private void resized() {
		controls.updateWidth(time.size());
		calcValPerPx();
		repaint();
	}

	// Not implemented yet

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
}
