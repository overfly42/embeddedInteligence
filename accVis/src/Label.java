import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Label {
	private String name;

	public Label(String n) {
		name = n;
	}

	public Label() {

	}

	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}
}