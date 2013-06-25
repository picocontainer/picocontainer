package com.picocontainer.jetty.groovy;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DependencyInjectionTestServlet extends HttpServlet {
    private final String name;
    private String foo;
    private ServletConfig servletConfig;

    public DependencyInjectionTestServlet(final String name) {
        this.name = name;
    }

    @Override
	public void init(final ServletConfig servletConfig) throws ServletException {
        this.servletConfig = servletConfig;
        String initParameter = servletConfig.getInitParameter("foo");
        if (initParameter!= null) {
            foo = initParameter;
        }
    }

    @Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        String message = name;
        if (request.getAttribute("foo2") != null) {
            message = message + request.getAttribute("foo2");
        }

        String initParameter = servletConfig.getServletContext().getInitParameter("a");
        String text = "hello " + message + (foo != null ? " "+  foo : "" ) + "<" + initParameter + ">";
        response.getWriter().write(text);
    }

    @Override
	public void destroy() {
    }

    // used when handling this servlet directly rather than letting Jetty instantiate it.
    public void setFoo(final String foo) {
        this.foo = foo;
    }
}