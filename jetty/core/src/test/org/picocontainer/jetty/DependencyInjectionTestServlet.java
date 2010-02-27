package org.picocontainer.jetty;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DependencyInjectionTestServlet extends HttpServlet {
    private final String name;
    private String foo;
    
    public DependencyInjectionTestServlet(String name) {
        this.name = name;
    }
        
    public void init(ServletConfig servletConfig) throws ServletException {
        String initParameter = servletConfig.getInitParameter("foo");
        if (initParameter!= null) {
            foo = initParameter;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        String message = name;
        if (request.getAttribute("foo2") != null) {
            message = message + request.getAttribute("foo2");
        }
        
        String text = "hello " + message + ( foo != null ? " "+  foo : "" );
        response.getWriter().write(text);
    }

    public void destroy() {
    }

    // used when handling this servlet directly rather than letting Jetty instantiate it.    
    public void setFoo(String foo) {
        this.foo = foo;
    }
}