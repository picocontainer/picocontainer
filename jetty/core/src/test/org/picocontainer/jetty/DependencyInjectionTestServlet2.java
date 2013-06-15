package org.picocontainer.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DependencyInjectionTestServlet2 extends HttpServlet {

    public DependencyInjectionTestServlet2(StringBuffer buffer) {
        buffer.append("-Servlet");
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(200);
        resp.getOutputStream().print("!");
    }
}