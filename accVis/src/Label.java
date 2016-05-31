
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Label {
	private String name;
	@XmlElement
	private int red;
	@XmlElement
	private int blue;
	@XmlElement
	private int green;
	@XmlElement
	private Map<String, Double> data;

	public Label(String n) {
		this();
		name = n;
		red = 0;
		blue = 0;
		green = 0;
	}

	public Label() {
		data = new HashMap<>();
	}

	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	public void setColor(Color c) {
		red = c.getRed();
		blue = c.getBlue();
		green = c.getGreen();
	}

	@XmlTransient
	public Color getColor() {
		return new Color(red, green, blue);
	}
	public Double getValue(String key) {
		return data.get(key);
	}

	public void setValue(String key, Double val) {
		data.put(key, val);
	}
}