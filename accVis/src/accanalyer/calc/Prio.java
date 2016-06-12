package accanalyer.calc;

class Prio implements Comparable<Prio> {
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