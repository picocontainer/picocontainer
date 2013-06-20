package org.picocontainer.web.sample.stub;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.web.PicoServletFilter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Use this as a starting point to work out how to integrate your web framework with PicoContainer
 */
@SuppressWarnings("serial")
public class StubServlet extends HttpServlet {

    private static Integer ctr = 0;

    private final PicoHook picoHook = new PicoHook();

	private static class PicoHook extends PicoServletFilter {
    	protected final MutablePicoContainer getRequestPicoForThread() {
    		return this.getRequestContainer();
    	}
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream os = response.getOutputStream();
        os.print("<html><body><p>");
        try {
            response.setContentType("text/html");
            RequestScoped requestScopeComp = picoHook.getRequestPicoForThread().getComponent(RequestScoped.class);
            os.println("Servlet object id: " + System.identityHashCode(this) + "<br/>Servlet counter: "
                    + ++ctr + "<br/>Scoped components using Depenency Injection with their counters: " + requestScopeComp.getCounterAndDependantsCounters());
        } catch (Throwable e) {
            os.println("some exception in Servlet.service():" + e.getMessage());
        }
        os.print("<br/><br/>Note - 'System.identityHashCode(this)' is used to determine id for each object</p></body></html>");
    }

   
}
