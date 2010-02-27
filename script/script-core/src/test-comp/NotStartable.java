
public class NotStartable {

    private StringBuffer sb;
	public NotStartable(TestComp tc, StringBuffer sb) {
        this.sb = sb;
        sb.append("-NotStartable");
	    if (tc == null) {
			throw new NullPointerException();
		}
	}

	public void start() {
        sb.append("*NotStartable(started)");

    }
	public void stop() {}
}

