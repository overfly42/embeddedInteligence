package accanalyer.calc;

public class Prio2 implements Comparable<Prio2> {
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