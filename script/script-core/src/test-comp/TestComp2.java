
public class TestComp2 implements com.picocontainer.Startable {

	public TestComp2(TestComp tc, StringBuffer sb) {
        sb.append("-TestComp2");
	    if (tc == null) {
			throw new NullPointerException();
		}
	}

	public void start() {}
	public void stop() {}
}

