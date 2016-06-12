import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialException;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Rectangle;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;

import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import jogamp.newt.driver.x11.DisplayDriver;
//gluegen-rt.jar
//jogl-all.jar
//gluegen.jar
//joal.jar
//jocl.jar
//jzy3d-jdt-core
//jzy3d-api
//log4j

public class Main extends JPanel implements MouseListener, MouseMotionListener {

	private class VisibleDecisions extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8817316726011629804L;
		boolean[] rawData = new boolean[4];
		boolean[] smoothData = new boolean[4];
		boolean labelLine = true;
		boolean[] label = new boolean[4];// 0->label, 1 lines of label

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			if (colIndex == 0)
				if (rowIndex == 0)
					return "Show raw Data";
				else if (rowIndex == 1)
					return "Show smooth Data";
				else if (rowIndex == 2)
					return "Show Line to Label";
				else
					return "Show Label";

			colIndex--;
			switch (rowIndex) {
			case 0:
				return rawData[colIndex];
			case 1:
				return smoothData[colIndex];
			case 2:
				if (colIndex == 0)
					return labelLine;
			case 3:
				if (colIndex < 2)
					return label[colIndex];
			default:
				return null;
			}
		}

		public Class getColumnClass(int columnIndex) {
			if (columnIndex == 0)
				return String.class;
			return Boolean.class;

		}

		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "Option";
			case 1:
				return "X";
			case 2:
				return "Y";
			case 3:
				return "Z";
			case 4:
				return "|V|";
			default:
				return "N/A";
			}
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
			case 2:
				if (colIndex == 0)
					labelLine = (boolean) o;
			case 3:
				if (colIndex < 2)
					label[colIndex] = (boolean) o;
			}
			repaint();
		}
	}

	private class LabelPosition implements Comparable<LabelPosition> {
		int labelPos;
		String labelName;
		LabelType t;

		public LabelPosition(int pos, String name, LabelType type) {
			labelPos = pos;
			labelName = name;
			t = type;
		}

		@Override
		public int compareTo(LabelPosition o) {

			return labelPos - o.labelPos;
		}

	}

	private class AxeSelectMenuItem extends JCheckBoxMenuItem {
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

	private class Prio implements Comparable<Prio> {
		String name;
		int prio;

		public Prio(String n, int p) {
			name = n;
			prio = p;
		}

		@Override
		public int compareTo(Prio arg0) {

			return arg0.prio - this.prio;
		}
	}

	private class Prio2 implements Comparable<Prio2> {
		String name;
		double difference;

		public Prio2(String s, double d) {
			name = s;
			difference = d;
		}

		public int compareTo(Prio2 arg0) {
			return (int) (difference * 100 - arg0.difference * 100);
		}
	}

	private enum LabelType {
		manual, automatic
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4648172894076113183L;

	private static final int SMOOTHING = 20;
	private static final double PEAK_MIN = 10;
	private static final int BORDER = 25;
	private static final int GRAPHS = 4;
	private static final int DEFAULT_WINDOW_MS = 2000;
	private static final double DEFAULT_CONFIDENCE_INTERVALL = 0.2;
	private static final double DEFAULT_NEXT_WINDOW = 0.25;
	public static final String MAX = "Max. Value";
	public static final String MIN = "Min. Value";
	public static final String AVERAGE = "Average";
	public static final String WINDOW_SIZE = "Window size";
	public static final String TOTAL_AVERAGE_ACC = "TOTAL Acceleration (average)";
	public static final String DIVIATION = "Normal diviation";
	public static final String VARIANZ = "Varianz";
	public static final String PEAKS = "Peaks";
	public static final String TRAIN_AMOUNT = "Training Data";
	public static final String[] VALUES = { MAX, MIN, AVERAGE, DIVIATION, VARIANZ, PEAKS, TOTAL_AVERAGE_ACC,
			WINDOW_SIZE };
	public static final String[] VALUES_CALC = { MAX, MIN, AVERAGE, DIVIATION, VARIANZ, PEAKS };
	public static final String[] VALUES_LABEL = { MAX, MIN, AVERAGE, DIVIATION, VARIANZ, PEAKS, WINDOW_SIZE,
			TRAIN_AMOUNT };
	public static final String UNDEFINED_LABEL = "undefined";

	private List<List<Double>> data;
	private List<Integer> time;
	private List<Color> colors;
	private List<LabelPosition> classification;

	private int maxVal;
	private int minVal;
	private int diff;
	private int window;// Time for sliding window
	private int startAt;// Start of Data shown in Panel
	private int stopAt;// End of Data shown in Panel
	private int windowEndAt;// End of Sliding Window
	private double valPerPxH;
	private double valPerPxW;
	private int position;
	// UI
	private JFrame mainFrame;
	private Controls controls;
	private JTable dataView;
	private JTable showOptions;
	private JTable labelList;
	private Statistics stats;
	private VisibleDecisions vd;
	private Labels labels;
	private JMenuBar mainMenu;
	private List<AxeSelectMenuItem> axeItems;
	private Chart chart;

	public static void main(String[] args) throws IOException {

		new Main();
	}

	public Main() throws IOException {
		colors = new ArrayList<>();
		colors.add(Color.GREEN);
		colors.add(Color.PINK);
		colors.add(Color.BLUE);
		colors.add(Color.ORANGE);
		open();
		startAt = 0;
		position = 0;
		// createing labels
		try {
			JAXBContext c = JAXBContext.newInstance(Labels.class);
			Unmarshaller u = c.createUnmarshaller();
			FileInputStream fis = new FileInputStream(new File("labels.dat"));
			labels = (Labels) u.unmarshal(fis);
			labels.setMain(this);
			fis.close();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			labels = new Labels(this);
		}
		initFrame();
		double[] w_size = { DEFAULT_WINDOW_MS };
		window = (int) w_size[0];
		stats.setData(WINDOW_SIZE, w_size);
		for (int i = 0; i < 4; i++) {
			vd.rawData[i] = true;
			vd.smoothData[i] = false;
			vd.label[i] = true;
		}
		chart = null;

	}

	public void paint(Graphics g) {
		super.paint(g);
		calcPaintingAreas();
		paintAxis(g);
		calcValPerPx();
		for (int i = 0; i < colors.size(); i++) {
			g.setColor(colors.get(i));
			if (vd.rawData[i])
				paintGraph(g, convertValToPos(data.get(i), false));
			if (vd.smoothData[i])
				paintGraph(g, convertValToPos(data.get(i), true));
		}

		Map<String, double[]> stats = prepairStats();
		List<List<Double>> selectData = selectData(startAt, windowEndAt);
		List<Integer> selectTime = selectTime(startAt, windowEndAt);
		calcData(stats, selectData, selectTime);
		double[] tmp = { window, 0, 0, 0 };
		stats.put(WINDOW_SIZE, tmp);
		populateStats(stats);

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
		if (classification.isEmpty())
			return;
		String msg = UNDEFINED_LABEL + " a";
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

	private Map<String, double[]> prepairStats() {
		Map<String, double[]> val = new HashMap<>();
		for (String s : VALUES)
			val.put(s, new double[GRAPHS]);
		for (int i = 0; i < GRAPHS; i++) {
			val.get(AVERAGE)[i] = 0;
			val.get(DIVIATION)[i] = 0;
			val.get(VARIANZ)[i] = 0;
			val.get(MAX)[i] = -10000;// Random number, but small
			val.get(MIN)[i] = 10000;// Random number, but big
			val.get(PEAKS)[i] = 0;
			val.get(TOTAL_AVERAGE_ACC)[i] = 0;
		}
		return val;
	}

	private List<List<Double>> selectData(int beginnAt, int endAt) {
		List<List<Double>> val = new ArrayList<>();
		for (int i = 0; i < GRAPHS; i++) {
			List<Double> subset = new ArrayList<Double>();
			List<Double> tmp = data.get(i);
			val.add(subset);
			for (int n = beginnAt; n <= endAt && n < tmp.size(); n++)
				subset.add(tmp.get(n));
		}
		return val;
	}

	private List<Integer> selectTime(int beginnAt, int endAt) {
		List<Integer> val = new ArrayList<>();
		for (int i = beginnAt; i <= endAt && i < time.size(); i++)
			val.add(time.get(i));
		return val;
	}

	private void populateStats(Map<String, double[]> calcData) {
		for (String s : calcData.keySet())
			stats.setData(s, calcData.get(s));
	}

	/**
	 * Do the whole calculation for a given data set
	 * 
	 * @param calcData
	 *            [output]
	 * @param data
	 *            [input]
	 * @param time
	 *            [input], not needed at the moment
	 */
	private void calcData(Map<String, double[]> calcData, List<List<Double>> data, List<Integer> time) {
		if (data == null || data.size() == 0) {
			System.out.println("Not calculating any data");
			System.out.println("data available:" + (data == null));
			return;
		}
		calcStats(calcData, data);
		calcMinMax(calcData, data);
		calcPeaks(calcData, data);
	}

	private void calcStats(Map<String, double[]> calcData, List<List<Double>> data) {
		// Storage
		double[] average = calcData.get(AVERAGE);
		double[] divertion = calcData.get(DIVIATION);
		double[] varianz = calcData.get(VARIANZ);
		double[] totalAcc = calcData.get(TOTAL_AVERAGE_ACC);

		int values = data.get(0).size();
		double[] array = new double[GRAPHS];
		array[0] = (double) values;
		calcData.put("VALUES", array);
		for (int i = 0; i < GRAPHS; i++) {
			List<Double> tmp = data.get(i);
			for (double d : tmp)
				average[i] += d;
			average[i] = average[i] / values;
			if (i < GRAPHS - 1)
				totalAcc[0] += Math.pow(average[i], 2);
		}
		totalAcc[0] = Math.sqrt(totalAcc[0]);
		for (int i = 0; i < values; i++) {
			for (int n = 0; n < GRAPHS; n++) {
				divertion[n] += Math.pow(data.get(n).get(i) - average[n], 2);

			}
		}
		for (int n = 0; n < GRAPHS; n++) {
			divertion[n] = divertion[n] / values;
			varianz[n] = Math.sqrt(divertion[n]);
		}
	}

	private void calcMinMax(Map<String, double[]> calcData, List<List<Double>> data) {
		double[] min = calcData.get(MIN);
		double[] max = calcData.get(MAX);
		for (int i = 0; i < GRAPHS; i++) {
			List<Double> tmp = data.get(i);
			for (double d : tmp) {
				min[i] = Math.min(min[i], d);
				max[i] = Math.max(min[i], d);
			}
		}
	}

	private void calcPeaks(Map<String, double[]> calcData, List<List<Double>> data) {
		double[] peaks = calcData.get(PEAKS);
		boolean[] rise = new boolean[GRAPHS];
		for (int i = 0; i < 4; i++) {
			rise[i] = data.get(i).size() >= 2 ? data.get(i).get(0) < data.get(i).get(1) : false;
			peaks[i] = 0;
			for (int n = 2; n < data.get(i).size(); n++) {
				double d1 = data.get(i).get(n - 1);
				double d2 = data.get(i).get(n);
				if (rise[i] && d1 > d2 + PEAK_MIN) {
					peaks[i]++;
					rise[i] = false;
				} else if (!rise[i] && d1 + PEAK_MIN > d2) {
					peaks[i]++;
					rise[i] = true;
				}
			}
		}
	}

	private void calcPaintingAreas() {
		// startAt is set by slider
		stopAt = startAt + this.getWidth() - 2 * BORDER;
		if (startAt > time.size())
			return;
		int ms_in_window = time.get(startAt) + window;// this is in ms
		for (int i = startAt; i < time.size(); i++) {
			if (time.get(i) > ms_in_window) {
				windowEndAt = i;
				break;
			}
		}
	}

	private List<Integer> convertValToPos(List<Double> ld, boolean smooth) {
		List<Integer> converted = new ArrayList<>();
		// int stopAt = startAt + this.getWidth();
		// stopAt = stopAt < ld.size() ? stopAt : ld.size();
		for (int i = startAt; i < stopAt; i++) {
			Double d = ld.get(i);
			int val = ((int) ((d - minVal) / valPerPxH));
			// val -= BORDER;
			val = this.getHeight() - val - 2 * BORDER;
			if (smooth && !converted.isEmpty()) {
				int lastVal = converted.get(converted.size() - 1);
				if (lastVal + SMOOTHING > val && lastVal - SMOOTHING < val)
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
		this.addMouseListener(this);
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
		dataView.setPreferredSize(new Dimension(1000, 1000));
		JScrollPane sp1 = new JScrollPane(dataView);

		showOptions = new JTable();
		vd = new VisibleDecisions();
		showOptions.setPreferredSize(new Dimension(1000, 1000));
		showOptions.setModel(vd);
		JScrollPane sp2 = new JScrollPane(showOptions);

		labelList = new JTable();
		labelList.setPreferredSize(new Dimension(1000, 1000));
		labelList.setSize(new Dimension(1000, 1000));
		labelList.setModel(labels);
		labelList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		labelList.getColumnModel().getColumn(1).setCellRenderer(labels.getCellRenderer());
		labelList.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (labelList.columnAtPoint(e.getPoint()) == 1)
					labels.setColor(labelList.rowAtPoint(e.getPoint()));

			}
		});
		JScrollPane sp3 = new JScrollPane(labelList);
		sp3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		JSplitPane s = new JSplitPane();
		JPanel s1 = new JPanel();
		s1.setLayout(new GridLayout(3, 1));
		s1.add(sp1);
		s1.add(sp2);
		s1.add(sp3);

		s.setRightComponent(s1);
		s.setLeftComponent(this);
		s.setDividerLocation(800);
		mainFrame.add(s, BorderLayout.CENTER);

		// Last actions of initFrame()
		createMenu();
		mainFrame.setVisible(true);
		controls.updateWidth(this.getWidth());
	}

	private void createMenu() {

		axeItems = new ArrayList<>();
		mainMenu = new JMenuBar();
		// Basic Menu operations
		JMenu plotMenu = new JMenu("Plot");
		JMenu fileMenu = new JMenu("File");
		mainMenu.add(fileMenu);
		mainMenu.add(plotMenu);
		// Create File Menu
		{
			JMenuItem jmi = new JMenuItem("Open Data File");
			jmi.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						open();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});
			fileMenu.add(jmi);
			jmi = new JMenuItem("Exit");
			jmi.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					mainFrame.dispose();

				}
			});
			fileMenu.addSeparator();
			fileMenu.add(jmi);
		}
		// Create variable Plot Menu
		JMenuItem jmi = new JMenuItem("view plot data");
		jmi.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				plotData();
			}
		});
		for (String s : VALUES_CALC) {
			AxeSelectMenuItem asmi = new AxeSelectMenuItem(s, axeItems);
			axeItems.add(asmi);
			plotMenu.add(asmi);
			if (s.equals(MIN) || s.equals(MAX) || s.equals(AVERAGE))
				asmi.setSelected(true);
			else
				asmi.setSelected(false);
		}
		plotMenu.addSeparator();
		plotMenu.add(jmi);
		jmi = new JMenuItem("Save Plot");
		jmi.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveChart();

			}
		});

		// plotMenu.add(jmi);
		mainFrame.setJMenuBar(mainMenu);
	}

	private List<List<Double>> splitData(List<String> rowData) {
		List<List<Double>> data = new ArrayList<>();// 3 Coordinates
		time = new ArrayList<>();
		int axis = 4;
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
			if (s.startsWith("$")) {
				s = s.substring(1).trim().toLowerCase();
				String[] split = s.split(" ");
				if (split.length < 2)
					continue;
				LabelType t;
				switch (split[0]) {
				case "a":
					t = LabelType.automatic;
					break;
				case "m":
					t = LabelType.manual;
					break;
				default:
					continue;
				}
				classification.add(new LabelPosition(time.size(), split[1], t));

				continue;
			}
			String[] split = s.split(" ");
			for (int i = 0; i < axis; i++)
				if (i < 3)
					data.get(i).add(Double.parseDouble(split[i]));
				else {
					double val = 0;
					for (int n = 0; n < 3; n++) {
						List<Double> ld = data.get(n);
						double d = ld.get(ld.size() - 1);
						val += Math.pow(d, 2);
					}
					data.get(i).add(Math.sqrt(val));
				}
			timeVal += Integer.parseInt(split[3]);
			this.time.add(timeVal);
		}
		return data;
	}

	private List<String> readData(String filename) throws IOException {
		File f = new File(filename);
		if (!f.exists()) {
			JOptionPane.showMessageDialog(this, "File " + f.getName() + " not exist");
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
		controls.updateWidth(time.size());
		calcValPerPx();
		repaint();
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

	private void deleteLabel(LabelPosition lp) {
		classification.remove(lp);
	}

	private LabelPosition addLabel(String kind, int pos) {
		LabelPosition lp = new LabelPosition(pos, kind, LabelType.manual);
		classification.add(lp);
		Collections.sort(classification);
		return lp;
	}

	public void setStartVal(int start) {
		startAt = start;
		repaint();

	}

	public int getMaxVal() {
		if (data != null && data.size() > 0)
			return time.size();
		return 0;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (e.getX() < BORDER)
			return;
		int pos = startAt + e.getX() - BORDER;
		position = e.getX();
		if (data.get(0).size() <= pos)
			return;
		repaint();
		if (pos > time.size())
			return;
		String msg = "<html>";
		msg += "x:   " + format(data.get(0).get(pos));
		msg += "<br>";
		msg += "y:   " + format(data.get(1).get(pos));
		msg += "<br>";
		msg += "z:   " + format(data.get(2).get(pos));
		msg += "<br>";
		msg += "|v|: " + format(data.get(3).get(pos));
		msg += "<br>";
		msg += "t:   " + time.get(pos) + "ms";
		msg += "</html>";
		this.setToolTipText(msg);

	}

	public String format(double d) {
		return String.format("%.2f", d).replace(',', '.');
	}

	private void open() throws IOException {
		classification = new ArrayList<>();

		JFileChooser fc = new JFileChooser("../");
		fc.showOpenDialog(this);

		List<String> rowData = readData(fc.getSelectedFile().getAbsolutePath());
		data = splitData(rowData);
		JOptionPane.showMessageDialog(this, "Found " + classification.size() + " Labels");
		calcMinMax();
		calcValPerPx();

	}

	public void save() {
		JFileChooser fc = new JFileChooser(".");
		fc.showSaveDialog(this);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fc.getSelectedFile()));
			bw.write("#Comments starts with #\n");
			bw.write("#Created by Program\n");
			bw.write("# data format:\n");
			bw.write("# so we have 4 columns with values separated by the \" \"\n");
			bw.write("# X Y Z time_from_previous_sample(ms)\n");
			bw.write("# units set to: m/sec^2\n");
			bw.write("\n#labels start with \"$\" a or m [blank] type\n");
			bw.write("#a = automatic, m manual");
			bw.write("example \"a " + UNDEFINED_LABEL + "\"\n");
			int max = time.size();
			int lastVal = 0;
			if (classification.isEmpty())
				bw.write("$a " + UNDEFINED_LABEL + "\n");

			int lpos = 0;
			for (int i = 0; i < max; i++) {
				if (classification.size() > lpos) {
					if (classification.get(lpos).labelPos < i) {
						bw.write("$");
						switch (classification.get(lpos).t) {
						case automatic:
							bw.write("a ");
							break;
						case manual:
							bw.write("m ");
							break;
						default:
							break;
						}
						bw.write(classification.get(lpos).labelName);
						bw.write("\n");
						lpos++;
					}
				}
				for (int n = 0; n < 3; n++)
					bw.write("" + data.get(n).get(i) + " ");
				bw.write((time.get(i) - lastVal) + "\n");
				lastVal = time.get(i);
			}

		} catch (IOException e) {
			javax.swing.JOptionPane.showConfirmDialog(this, "Error, could not save data");
		} finally {
			if (bw != null)
				try {
					bw.close();
				} catch (IOException e) {
					System.out.println("Error closing stream");
				}
		}

	}

	public void saveLBL() {
		try {
			JAXBContext c = JAXBContext.newInstance(Labels.class);
			Marshaller m = c.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			FileOutputStream fos = new FileOutputStream(new File("labels.dat"));
			m.marshal(labels, fos);
			fos.close();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void saveChart() {
		if (chart == null) {
			JOptionPane.showMessageDialog(this, "Please view Plot first");
			return;
		}
		try {
			ChartLauncher.screenshot(chart, "blub");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setWindow(int w) {
		window = w;
		repaint();
	}

	/**
	 * 
	 * @return A Map that contains an entry for every label, that kontains a
	 *         value for every cacluated data
	 */
	private Map<String, List<Map<String, double[]>>> calcWindowsByLabels() {
		Map<String, List<Map<String, double[]>>> clusters = new HashMap<>();
		List<Label> labelList = labels.getLabels();
		for (Label l : labelList) {
			// Fetch all Labels with this name
			List<LabelPosition> tmp = new ArrayList<>();
			for (LabelPosition lp : classification)
				if (lp.labelName.toLowerCase().equals(l.getName().toLowerCase()))
					tmp.add(lp);
			// Sort labels
			Collections.sort(tmp);
			Collections.sort(classification);
			// Calcuate every label
			for (LabelPosition lp : tmp) {
				// Grep position in data for this label
				int orgPos = classification.indexOf(lp);
				int dest = orgPos + 1;
				LabelPosition follow = classification.size() > dest ? classification.get(dest) : null;
				int firstFrame = lp.labelPos;
				int lastFrame;
				if (follow == null)
					lastFrame = time.size() - 1;
				else
					lastFrame = follow.labelPos;
				// Collect the selected data from all of the data
				List<List<Double>> selectedData = new ArrayList<>();
				List<Integer> selectedTime = new ArrayList<>();
				for (int n = 0; n < GRAPHS; n++) {
					selectedData.add(new ArrayList<Double>());
					for (int i = firstFrame; i < lastFrame; i++)
						selectedData.get(n).add(data.get(n).get(i));
				}
				for (int i = firstFrame; i < lastFrame; i++)
					selectedTime.add(time.get(i));
				// Split data into windows size portions
				List<List<List<Double>>> splitData = splitToWindow(selectedData, selectedTime);
				List<Map<String, double[]>> lm = new ArrayList<>();
				clusters.put(l.getName(), lm);
				for (List<List<Double>> lld : splitData) {
					Map<String, double[]> stats = prepairStats();
					calcData(stats, lld, null);
					lm.add(stats);
				}
			}
		}
		return clusters;
	}

	private void plotData() {
		int size = 0;

		Map<String, List<Map<String, double[]>>> clusters = calcWindowsByLabels();
		String[] selected = new String[3];
		float[] f = new float[3];
		int i = 0;// i is a temp counter and has to be resettet after use
		for (AxeSelectMenuItem asmi : axeItems) {
			if (asmi.isSelected() && i < selected.length) {
				selected[i] = asmi.getText();
				f[i] = 0;
				i++;
			}
		}
		if (i != 3) {
			JOptionPane.showMessageDialog(this, "Exact three Values are needed for the plot");
			return;
		}
		i = 0;
		int n = GRAPHS - 1;// Last one
		for (String s : clusters.keySet()) {
			size += clusters.get(s).size();
			List<Map<String, double[]>> lm = clusters.get(s);
			for (Map<String, double[]> m : lm) {
				for (int j = 0; j < 3; j++)
					f[j] = (float) Math.max(Math.abs(m.get(selected[j])[n]), f[j]);
			}
		}
		Coord3d[] coordinates = new Coord3d[size];
		org.jzy3d.colors.Color[] c = new org.jzy3d.colors.Color[size];
		int a = 0;
		for (String s : clusters.keySet()) {
			org.jzy3d.colors.Color c1 = labels.getColor(s);
			for (Map<String, double[]> m : clusters.get(s)) {
				float x = (float) m.get(selected[0])[n] / f[0];
				float y = (float) m.get(selected[1])[n] / f[1];
				float z = (float) m.get(selected[2])[n] / f[2];
				coordinates[i] = new Coord3d(x, y, z);
				c[i++] = c1;
			}
			a++;
		}
		Scatter scatter = new Scatter(coordinates, c);
		scatter.setWidth(5.25f);
		chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
		chart.getAxeLayout().setXAxeLabel(selected[0]);
		chart.getAxeLayout().setYAxeLabel(selected[1]);
		chart.getAxeLayout().setZAxeLabel(selected[2]);
		chart.getAxeLayout().setXAxeLabelDisplayed(true);
		chart.getAxeLayout().setYAxeLabelDisplayed(true);
		chart.getAxeLayout().setZAxeLabelDisplayed(true);
		chart.setViewMode(ViewPositionMode.FREE);
		chart.addMouseController();
		chart.getScene().add(scatter);
		chart.show(new Rectangle(100, 100, 500, 500), "3D Plot");
	}

	public void trainLabels() {
		Map<String, List<Map<String, double[]>>> calcWindowsByLabels = calcWindowsByLabels();
		Map<String, Map<String, Double>> summedCalculation = new HashMap<>();
		Map<String, Integer> occurences = new HashMap<>();
		Double trainingsdone = 0.0;
		for (String s : calcWindowsByLabels.keySet()) {
			summedCalculation.put(s, new HashMap<>());
			occurences.put(s, 0);
			List<Map<String, double[]>> list = calcWindowsByLabels.get(s);
			trainingsdone = labels.getLabel(s).getValue(TRAIN_AMOUNT);
			if (trainingsdone == null)
				trainingsdone = 0.0;
			for (Map<String, double[]> msd : list) {
				for (String str : msd.keySet()) {
					Double d = summedCalculation.get(s).get(str);
					if (d == null)
						d = 0.0;
					d += msd.get(str)[3];
					summedCalculation.get(s).put(str, d);
				}
				occurences.put(s, occurences.get(s) + 1);
			}
			for (String str : VALUES_CALC) {
				Double d = summedCalculation.get(s).get(str);
				d /= occurences.get(s);
				summedCalculation.get(s).put(str, d);
			}
		}
		// Store calculation
		for (String s1 : summedCalculation.keySet()) {
			summedCalculation.get(s1).put(WINDOW_SIZE, (double) window);
			summedCalculation.get(s1).put(TRAIN_AMOUNT, 0.0);
			for (String s2 : VALUES_LABEL) {
				Double val = labels.getLabel(s1).getValue(s2);
				if (val == null)
					val = 0.0;
				val *= trainingsdone;
				try {
					val += summedCalculation.get(s1).get(s2);
				} catch (Exception e) {
					System.out.println("Summed calcs: " + summedCalculation.get(s1).get(s2));
				}
				val /= (trainingsdone + 1);
				labels.getLabel(s1).setValue(s2, val);
			}
			labels.getLabel(s1).setValue(TRAIN_AMOUNT, trainingsdone + 1);
		}
		labels.fireTableDataChanged();
	}

	/**
	 * Trys to label the Data, a label could only placed at the beginning of a
	 * Window
	 */
	public void label() {
		// Get average Window size for all Labels
		Double win_size = 0.0;
		for (Label l : labels.getLabels()) {
			Double d = l.getValue(WINDOW_SIZE);
			if (d == null)
				d = 0.0;
			win_size += d;
		}
		win_size /= labels.getLabels().size();
		// Backup and set the window size for this computing
		int window_backup = window;
		window = win_size.intValue();
		// Calculate Data
		List<List<List<Double>>> splitToWindow = splitToWindow(data, time);// Get
																			// Windows
		int i = 0;
		int position = 0;
		for (List<List<Double>> lld : splitToWindow) {
			System.out.println("==============Calculation of Window " + (i++) + "===============");
			String s = getLabelForWindow(lld, position);
			addLabel(s, position).t = LabelType.automatic;
			position += window;

		}
		// Clean up
		window = window_backup;
		List<LabelPosition> lpDelete = new ArrayList<>();
		// Workaround, split to windows has some problems
		// remove labels out of range

		int max = time.size();
		for (LabelPosition lp : classification)
			if (lp.labelPos > max)
				lpDelete.add(lp);
		classification.removeAll(lpDelete);
		lpDelete.clear();
		// Remove undefined Labels if there between the same kind of label
		for (i = 2; i < classification.size(); i++) {
			LabelPosition lp1 = classification.get(i - 2);
			LabelPosition lp2 = classification.get(i - 1);// THIS is the
															// interesting one
			LabelPosition lp3 = classification.get(i);
			if (lp2.labelName.equals(UNDEFINED_LABEL) && lp1.labelName.equals(lp3.labelName))
				lpDelete.add(lp2);
		}
		classification.removeAll(lpDelete);
		lpDelete.clear();
		// remove all Labels that are equal to the label before
		for (i = 1; i < classification.size(); i++)// skip sero to compare i and
													// i-1
		{
			LabelPosition lp1 = classification.get(i - 1);
			LabelPosition lp2 = classification.get(i);
			if (lp1.labelName.equals(lp2.labelName) && lp1.t == lp2.t)
				lpDelete.add(lp2);

		}
		classification.removeAll(lpDelete);
		lpDelete.clear();

	}

	private String getLabelForWindow(List<List<Double>> dataToLabel, int position) {
		Map<String, double[]> statistics = prepairStats();// has to be done
		// for every
		// window
		calcData(statistics, dataToLabel, null);
		Map<String, Integer> labelSelectionHelper = new HashMap<>();
		Map<String, Double> labelSelectionHelperDiff = new HashMap<>();
		for (Label l : labels.getLabels()) {
			labelSelectionHelper.put(l.getName(), 0);
			labelSelectionHelperDiff.put(l.getName(), 0.0);
		}
		// String[] VALUES_CALC = { DIVIATION, AVERAGE, VARIANZ };
		for (String s : VALUES_CALC) {
			Double d = statistics.get(s)[3];
			// System.out.println(s + ":\t" + format(d));
			for (Label l : labels.getLabels()) {
				Double d2 = l.getValue(s);
				// Double upper = d2 * (1.0 + DEFAULT_CONFIDENCE_INTERVALL);
				// Double lower = d2 * (1.0 - DEFAULT_CONFIDENCE_INTERVALL);
				Double u2 = d2 + DEFAULT_CONFIDENCE_INTERVALL;
				Double l2 = d2 - DEFAULT_CONFIDENCE_INTERVALL;
				// boolean b1 = (upper > d && d > lower);
				boolean b2 = (u2 > d && d > l2);
				// boolean b3 = b1 || b2;
				// if (b3)
				if (b2)
					labelSelectionHelper.put(l.getName(), labelSelectionHelper.get(l.getName()) + 1);
				Double diff1 = Math.abs(labelSelectionHelperDiff.get(l.getName()));
				Double diff2 = Math.abs(Math.abs(d) - Math.abs(d2));
				labelSelectionHelperDiff.put(l.getName(), diff1 + diff2);
			}
		}
		System.out.println("At Pos " + position + ":");
		List<Prio> descision = new ArrayList<>();
		for (String s : labelSelectionHelper.keySet()) {
			System.out.println("\t" + s + " " + labelSelectionHelper.get(s) + " of " + statistics.get("VALUES")[0]);
			descision.add(new Prio(s, labelSelectionHelper.get(s)));
		}
		List<Prio2> descision2 = new ArrayList<>();
		for (String s : labelSelectionHelperDiff.keySet()) {
			System.out.println("\t" + s + " " + labelSelectionHelperDiff.get(s));
			descision2.add(new Prio2(s, labelSelectionHelperDiff.get(s)));
		}
		Collections.sort(descision);
		Collections.sort(descision2);
		int maxValues = VALUES_CALC.length;
		int minValues = maxValues / 2;
		int minDistance = minValues / 2;
		System.out.println("Desc 2 will set to " + descision2.get(0).name);

		if (descision.get(0).prio > minValues && descision.get(0).prio - minDistance > descision.get(1).prio) {
			// addLabel(descision.get(0).name, position).t =
			// LabelType.automatic;
			System.out.println("setting to " + descision.get(0).name);
			return descision.get(0).name;
		} else {
			System.out.println(descision.get(0).name + " " + descision.get(0).prio + " <= " + minValues);
			System.out.println(descision.get(1).name + " " + descision.get(1).prio + " ~ " + minDistance);
			// addLabel(UNDEFINED_LABEL, position).t = LabelType.automatic;

		}
		// addLabel(descision2.get(0).name, position).t = LabelType.automatic;

		return UNDEFINED_LABEL;
	}

	/**
	 * 
	 * @param inputData
	 *            the data to split
	 * @param inputTime
	 *            the timing index
	 * @return List(a) of List(b) of List(c) of Double the Double are the values
	 *         within the time area a is a List of all windows b is A window c
	 *         is a single graph
	 */
	public List<List<List<Double>>> splitToWindow(List<List<Double>> inputData, List<Integer> inputTime) {
		List<List<List<Double>>> output = new ArrayList<>();

		int first = 0;
		while (first < inputTime.size()) {
			// System.out.println("First is: " + first + " of " +
			// inputTime.size());
			// Grep first and last position of data
			int last = inputTime.size();
			do {
				last--;
			} while (first < last && inputTime.get(last) - inputTime.get(first) > window);
			List<List<Double>> segment = new ArrayList<>();
			for (int i = 0; i < GRAPHS; i++) {
				List<Double> tmp = new ArrayList<>();
				segment.add(tmp);
				for (int n = first; n < last; n++)
					tmp.add(inputData.get(i).get(n));
			}
			output.add(segment);
			// System.out.print("Segment has ");
			// for (List<Double> ld : segment)
			// System.out.print(" " + ld.size());
			// System.out.println(" Elements ");
			int step = (int) ((last - first) * DEFAULT_NEXT_WINDOW);
			first += step < 100 ? 100 : step;
			// first = last + 1;
			if (first > inputTime.size())
				break;
		}
		return output;
	}

	public void cleanLabels() {
		for (Label l : labels.getLabels())
			for (String s : VALUES_LABEL)
				l.setValue(s, null);
		labels.fireTableDataChanged();
	}

	public void deleteLabels() {
		classification.clear();
	}

	public void exportToWeka() {
		JFileChooser jfc = new JFileChooser(".");
		jfc.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				
				return "Arff data Files (*.arff)";
			}
			
			@Override
			public boolean accept(File f) {
				if(f.getName().endsWith(".arff"))return true;
				return false;
			}
		});
		jfc.showSaveDialog(this);
		System.out.println(jfc.getSelectedFile().getName());
		OutputStream os;
		try {
			os = new FileOutputStream(jfc.getSelectedFile());
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

			bw.write("%" + (new Date(System.currentTimeMillis())).toString() + "\n\n");
			bw.write("@relation test\n\n");
			for (String s : VALUES_CALC) {
				bw.write("@attribute " + s.replace(' ', '_') + " numeric\n");
			}
			Map<String, List<Map<String, double[]>>> labels = calcWindowsByLabels();
			bw.write("@attribute class {");
			boolean addKomma = false;
			for (String s : labels.keySet()) {
				if (addKomma)
					bw.write(",");
				else
					addKomma = true;
				bw.write(s);
			}
			bw.write("}\n\n");
			bw.write("@data\n");
			for (String s1 : labels.keySet()) {
				List<Map<String, double[]>> list = labels.get(s1);
				for (Map<String, double[]> m : list) {
					for (String s2 : VALUES_CALC)
						bw.write(format(m.get(s2)[3]) + ",");
					bw.write(s1+"\n");
				}

			}
			bw.close();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

}
