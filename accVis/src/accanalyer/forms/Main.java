package accanalyer.forms;

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
import javax.swing.filechooser.FileFilter;
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

import accanalyer.calc.Calculator;
import accanalyer.data.Common;
import accanalyer.data.LabelType;
import accanalyer.forms.models.Labels;
import accanalyer.forms.models.Statistics;
import accanalyer.forms.models.VisibleDecisions;
import jogamp.newt.driver.x11.DisplayDriver;
//gluegen-rt.jar
//jogl-all.jar
//gluegen.jar
//joal.jar
//jocl.jar
//jzy3d-jdt-core
//jzy3d-api
//log4j

public class Main extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4648172894076113183L;

	Calculator calc;
	Common common;
	RawView rv;

	// UI
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

		common = new Common();
		calc = new Calculator(common);
		open();
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
		rv = new RawView(calc, common,controls);
		double[] w_size = { Common.DEFAULT_WINDOW_MS };
		common.window = (int) w_size[0];
		stats.setData(Common.WINDOW_SIZE, w_size);
		for (int i = 0; i < 4; i++) {
			vd.rawData[i] = true;
			vd.smoothData[i] = false;
			vd.label[i] = true;
		}
		chart = null;

	}

	private void initFrame() {
		setTitle("Data print out");
		setBounds(50, 50, 2 * Common.BORDER + 950, 2 * Common.BORDER + 450);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new BorderLayout());

		controls = new Controls(this);
		add(controls, BorderLayout.SOUTH);


		dataView = new JTable();
		stats = new Statistics(dataView, this);
		dataView.setPreferredSize(new Dimension(1000, 1000));
		JScrollPane sp1 = new JScrollPane(dataView);

		showOptions = new JTable();
		vd = new VisibleDecisions(this);
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
		add(s, BorderLayout.CENTER);

		// Last actions of initFrame()
		createMenu();
		setVisible(true);
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
					dispose();

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
		for (String s : Common.VALUES_CALC) {
			AxeSelectMenuItem asmi = new AxeSelectMenuItem(s, axeItems);
			axeItems.add(asmi);
			plotMenu.add(asmi);
			if (s.equals(Common.MIN) || s.equals(Common.MAX) || s.equals(Common.AVERAGE))
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
		setJMenuBar(mainMenu);
	}

	public String format(double d) {
		return String.format("%.2f", d).replace(',', '.');
	}

	private void open() throws IOException {

		JFileChooser fc = new JFileChooser("../");
		fc.showOpenDialog(this);
		calc.init(fc.getSelectedFile().getAbsolutePath());
		JOptionPane.showMessageDialog(this, "Found " + calc.classification.size() + " Labels");

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
			bw.write("example \"a " + Common.UNDEFINED_LABEL + "\"\n");
			int max = calc.time.size();
			int lastVal = 0;
			if (calc.classification.isEmpty())
				bw.write("$a " + Common.UNDEFINED_LABEL + "\n");

			int lpos = 0;
			for (int i = 0; i < max; i++) {
				if (calc.classification.size() > lpos) {
					if (calc.classification.get(lpos).labelPos < i) {
						bw.write("$");
						switch (calc.classification.get(lpos).t) {
						case automatic:
							bw.write("a ");
							break;
						case manual:
							bw.write("m ");
							break;
						default:
							break;
						}
						bw.write(calc.classification.get(lpos).labelName);
						bw.write("\n");
						lpos++;
					}
				}
				for (int n = 0; n < 3; n++)
					bw.write("" + calc.data.get(n).get(i) + " ");
				bw.write((calc.time.get(i) - lastVal) + "\n");
				lastVal = calc.time.get(i);
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
		common.window = w;
		repaint();
	}

	private void plotData() {
		int size = 0;

		Map<String, List<Map<String, double[]>>> clusters = calc.calcWindowsByLabels();
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
		int n = Common.GRAPHS - 1;// Last one
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

}
