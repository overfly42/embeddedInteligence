package accanalyer.data;

public class Common {
	public static final int SMOOTHING = 20;
	public static final double PEAK_MIN = 10;
	public static final int BORDER = 25;
	public static final int GRAPHS = 4;
	public static final int DEFAULT_WINDOW_MS = 2000;
	public static final double DEFAULT_CONFIDENCE_INTERVALL = 0.2;
	public static final double DEFAULT_NEXT_WINDOW = 0.25;
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
	public int maxVal;
	public int minVal;
	public int diff;
	public int window;// Time for sliding window
	public int windowEndAt;// End of Sliding Window
	public double valPerPxH;
	public double valPerPxW;

}
