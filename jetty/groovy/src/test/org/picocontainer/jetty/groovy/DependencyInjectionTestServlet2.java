package org.picocontainer.jetty.groovy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DependencyInjectionTestServlet2 extends HttpServlet {

    public DependencyInjectionTestServlet2(final StringBuffer buffer) {
        buffer.append("-Servlet");
    }

    @Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(200);
        resp.getOutputStream().print("!");
    }
}