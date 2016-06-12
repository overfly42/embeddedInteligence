package accanalyer.calc;

import accanalyer.data.LabelType;

public class LabelPosition implements Comparable<LabelPosition> {
	public int labelPos;
	public String labelName;
	public LabelType t;

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