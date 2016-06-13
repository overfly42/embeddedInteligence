package accanalyer.calc;

import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import accanalyer.data.Common;
import accanalyer.data.Label;
import accanalyer.forms.Main.LabelPosition;
import accanalyer.forms.Main.LabelType;
import accanalyer.forms.Main.Prio;
import accanalyer.forms.Main.Prio2;

public class Calculator {
	public List<List<Double>> data;
	public List<Integer> time;
	public List<LabelPosition> classification;

	private Common c;

	public Calculator(Common c) {
		this.c = c;
		classification = new ArrayList<>();
	}

	public void init(String path) {
		List<String> rowData = readData(path);
		data = splitData(rowData);

	}

	public Map<String, double[]> prepairStats() {
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

	public List<List<Double>> selectData(int beginnAt, int endAt) {
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

	public List<Integer> selectTime(int beginnAt, int endAt) {
		List<Integer> val = new ArrayList<>();
		for (int i = beginnAt; i <= endAt && i < time.size(); i++)
			val.add(time.get(i));
		return val;
	}

	public void populateStats(Map<String, double[]> calcData) {
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
	public void calcData(Map<String, double[]> calcData, List<List<Double>> data, List<Integer> time) {
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

	public List<Integer> convertValToPos(List<Double> ld, boolean smooth) {
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

	public List<List<Double>> splitData(List<String> rowData) {
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

	public List<String> readData(String filename) throws IOException {
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

	/**
	 * 
	 * @return A Map that contains an entry for every label, that kontains a
	 *         value for every cacluated data
	 */
	public Map<String, List<Map<String, double[]>>> calcWindowsByLabels() {
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
				if (f.getName().endsWith(".arff"))
					return true;
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
					bw.write(s1 + "\n");
				}

			}
			bw.close();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
