
import java.awt.Color;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Label{
	private String name;
	private Color col;

	public Label(String n) {
		name = n;
		col = null;
	}

	public Label() {

	}

	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}
	public void setColor(Color c)
	{
		col = c;
	}
	public Color getColor()
	{
		return col;
	}
}