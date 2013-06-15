package org.picocontainer.jetty;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

public class FooTestCase {

    @Test public void testFoo() throws Exception {
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.addFilter(new FilterHolder(HelloFilter.class), "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(new ServletHolder(HelloServlet.class), "/*");

//        server.start();
//        server.join();
    }

    public static class HelloFilter implements Filter {
        public void init(final FilterConfig filterConfig) throws ServletException {
        }

        public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException {
            req.setAttribute("foo", "true");
            chain.doFilter(req, resp);
        }

        public void destroy() {
        }
    }

    @SuppressWarnings("serial")
	public static class HelloServlet extends HttpServlet {
        @Override
		protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello SimpleServlet "+request.getAttribute("foo")+"</h1>");

            response.getWriter().println("session=" + request.getSession(true).getId());
        }
    }

}
