import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.ScrollPane;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class Main extends JPanel implements MouseListener, MouseMotionListener {

	private class VisibleDecisions extends AbstractTableModel {
		boolean[] rawData = new boolean[3];
		boolean[] smoothData = new boolean[3];

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public int getRowCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			if (colIndex == 0)
				if (rowIndex == 0)
					return "Show raw Data";
				else
					return "Show smooth Data";

			colIndex--;
			switch (rowIndex) {
			case 0:
				return rawData[colIndex];
			case 1:
				return smoothData[colIndex];
			default:
				return null;
			}
		}

		public Class getColumnClass(int columnIndex) {
			if (columnIndex == 0)
				return String.class;
			return Boolean.class;

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

			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4648172894076113183L;

	private static final int SMOOTHING = 20;
	private static final int BORDER = 25;
	private static final int DEFAULT_WINDOW_MS = 2000;
	public static final String MAX = "Max. Value";
	public static final String MIN = "Min. Value";
	public static final String AVERAGE = "Average";
	public static final String WINDOW_SIZE = "Window size";
	public static final String TOTAL_AVERAGE_ACC = "TOGAL Acceleration (average)";
	public static final String DIVIATION = "Normal diviation";
	public static final String VARIANZ = "Varianz";
	public static final String[] VALUES = { MAX, MIN, AVERAGE, DIVIATION, VARIANZ, TOTAL_AVERAGE_ACC, WINDOW_SIZE };

	private List<List<Double>> data;
	private List<Integer> time;
	private List<Color> colors;
	private int maxVal;
	private int minVal;
	private int diff;
	private int window;
	private int startAt;
	private double valPerPxH;
	private double valPerPxW;
	// UI
	private JFrame mainFrame;
	private Controls controls;
	private JTable dataView;
	private JTable showOptions;
	private Statistics stats;
	private VisibleDecisions vd;

	public static void main(String[] args) throws IOException {

		new Main();
	}

	public Main() throws IOException {
		colors = new ArrayList<>();
		colors.add(Color.GREEN);
		colors.add(Color.RED);
		colors.add(Color.BLUE);
		JFileChooser fc = new JFileChooser("../");
		fc.showOpenDialog(this);

		List<String> rowData = readData(fc.getSelectedFile().getAbsolutePath());
		data = splitData(rowData);
		calcMinMax();
		calcValPerPx();
		startAt = 0;
		initFrame();
		double[] w_size = { DEFAULT_WINDOW_MS };
		window = (int) w_size[0];
		stats.setData(WINDOW_SIZE, w_size);
		for (int i = 0; i < 3; i++) {
			vd.rawData[i] = true;
			vd.smoothData[i] = false;
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
		paintAxis(g);
		calcValPerPx();
		for (int i = 0; i < colors.size(); i++) {
			g.setColor(colors.get(i));
			if (vd.rawData[i])
				paintGraph(g, convertValToPos(data.get(i), false));
			if (vd.smoothData[i])
				paintGraph(g, convertValToPos(data.get(i), true));
		}
		calcStats();

	}

	private void calcValPerPx() {
		valPerPxW = (1.0 * time.size()) / Math.max(this.getWidth(), 1);
		valPerPxH = (1.0 * diff) / (Math.max(this.getHeight() - 2 * BORDER, 1));
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
		int t = time.get(startAt);
		int win = time.get(startAt) + window;
		boolean win_painted = false;
		int stopAt = Math.min(startAt + this.getWidth(), time.size());
		for (int i = startAt, pos = BORDER; i < stopAt; i++, pos++) {
			if (!(time.get(i) < win || win_painted)) {
				Color c = g.getColor();
				g.setColor(Color.CYAN);
				g.drawRect(BORDER, BORDER, pos, Y_LENGTH - BORDER);
				g.setColor(c);
				win_painted = true;
			}
			if (time.get(i) < t + 1000)
				continue;
			t += 1000;
			g.drawLine(BORDER + pos, BORDER, BORDER + pos, Y_LENGTH);
			g.drawString("" + time.get(i), BORDER + pos, Y_LENGTH);
		}
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

	private void calcStats() {
		// Find values to calculate
		int stopAt = 0;
		int startTime = time.get(startAt);
		for (int i = startAt; i < time.size(); i++) {
			if (time.get(i) - startTime < window)
				continue;
			stopAt = i;
			break;
		}
		// Storage
		double[] max = new double[3];
		double[] min = new double[3];
		double[] average = new double[3];
		double[] divertion = new double[3];
		double[] varianz = new double[3];
		for (int i = 0; i < 3; i++) {
			average[i] = 0;
			divertion[i] = 0;
			varianz[i] = 0;
		}
		int values = stopAt - startAt;
		for (int i = startAt; i < stopAt; i++) {
			for (int n = 0; n < 3; n++) {
				max[n] = Math.max(max[n], data.get(n).get(i));
				min[n] = Math.min(min[n], data.get(n).get(i));
				average[n] += data.get(n).get(i);
			}
		}
		double[] totalAcc = new double[1];
		totalAcc[0] = 0;
		for (int i = 0; i < 3; i++) {
			average[i] = average[i] / values;
			totalAcc[0] += Math.pow(average[i], 2);
		}
		totalAcc[0] = Math.sqrt(totalAcc[0]);
		stats.setData(AVERAGE, average);
		stats.setData(MAX, max);
		stats.setData(MIN, min);
		stats.setData(TOTAL_AVERAGE_ACC, totalAcc);
		for (int i = startAt; i < stopAt; i++) {
			for (int n = 0; n < 3; n++) {
				divertion[n] += Math.pow(data.get(n).get(i) - average[n], 2);

			}
		}
		for (int n = 0; n < 3; n++) {
			divertion[n] = divertion[n] / values;
			varianz[n] = Math.sqrt(divertion[n]);
		}
		stats.setData(DIVIATION, divertion);
		stats.setData(VARIANZ, varianz);
	}

	private List<Integer> convertValToPos(List<Double> ld, boolean smooth) {
		List<Integer> converted = new ArrayList<>();
		int stopAt = startAt + this.getWidth();
		stopAt = stopAt < ld.size() ? stopAt : ld.size();
		for (int i = startAt; i < stopAt; i++) {
			Double d = ld.get(i);
			int val = ((int) ((d - minVal) / valPerPxH));
			// val -= BORDER;
			val = this.getHeight() - BORDER - val;
			if(smooth &&!converted.isEmpty())
			{
				int lastVal = converted.get(converted.size()-1);
				if(lastVal + SMOOTHING > val && lastVal-SMOOTHING <val )
					val = lastVal;
			}
			converted.add(val);
		}
		return converted;
	}

	private void initFrame() {
		mainFrame = new JFrame();
		mainFrame.setTitle("Data print out");
		mainFrame.setBounds(50, 50, 2 * BORDER + 950, 2 * BORDER + 450);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// this.setResizable(false);
		mainFrame.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.setLayout(new BorderLayout());

		controls = new Controls(this);
		mainFrame.add(controls, BorderLayout.SOUTH);

		mainFrame.addComponentListener(new ComponentListener() {

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

		dataView = new JTable();
		stats = new Statistics(dataView, this);
		dataView.setPreferredSize(new Dimension(100, 1000));

		showOptions = new JTable();
		vd = new VisibleDecisions();
		showOptions.setModel(vd);
		
		JSplitPane s = new JSplitPane();
		JSplitPane s1 = new JSplitPane();
		
		s1.setRightComponent(new JScrollPane(dataView));
		s1.setLeftComponent(new JScrollPane(showOptions));
		s1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		s.setRightComponent(s1);
		s.setLeftComponent(this);
		s.setDividerLocation(600);
		mainFrame.add(s, BorderLayout.CENTER);

		// Last actions of initFrame()
		mainFrame.setVisible(true);
		controls.updateWidth(this.getWidth());
	}

	private List<List<Double>> splitData(List<String> rowData) {
		List<List<Double>> data = new ArrayList<>();// 3 Coordinates
		time = new ArrayList<>();
		int axis = 3;
		for (int i = 0; i < axis + 1; i++)// +1 for time after start recording
			data.add(new ArrayList<Double>());
		int hash = 0;
		int empty = 0;
		int timeVal = 0;
		for (String s : rowData) {
			if (s.length() == 0) {
				empty++;
				continue;
			}
			if (s.startsWith("#")) {
				hash++;
				continue;
			}
			String[] split = s.split(" ");
			for (int i = 0; i < axis; i++)
				data.get(i).add(Double.parseDouble(split[i]));
			timeVal += Integer.parseInt(split[3]);
			this.time.add(timeVal);
		}
		return data;
	}

	private List<String> readData(String filename) throws IOException {
		File f = new File(filename);
		if (!f.exists()) {
			System.out.println("File " + f.getName() + " not exist");
			return null;
		}
		BufferedReader br = new BufferedReader(new FileReader(f));
		List<String> l = new ArrayList<>();
		String str = br.readLine();
		do {
			l.add(str);
			str = br.readLine();
		} while (str != null);
		br.close();
		return l;
	}

	private void resized() {
		controls.updateWidth(this.getWidth());
		repaint();
	}

	@Override

	public void mouseClicked(MouseEvent arg0) {
		repaint();

	}

	public void setStartVal(int start) {
		startAt = start;
		repaint();

	}

	public int getMaxVal() {
		if (data != null && data.size() > 0)
			return data.get(0).size() - startAt;
		return 0;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (e.getX() < BORDER)
			return;
		int pos = startAt + e.getX() - BORDER;
		if (pos > time.size())
			return;
		String msg = "<html>";
		msg += "x: " + data.get(0).get(pos);
		msg += "<br>";
		msg += "y: " + data.get(1).get(pos);
		msg += "<br>";
		msg += "z: " + data.get(2).get(pos);
		msg += "<br>";
		msg += "t: " + time.get(pos) + "ms";
		msg += "</html>";
		this.setToolTipText(msg);

	}

	// not implementet yet

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

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void setWindow(int w) {
		window = w;
		repaint();
	}
}
