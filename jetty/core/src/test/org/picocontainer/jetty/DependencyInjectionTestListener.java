package org.picocontainer.jetty;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class DependencyInjectionTestListener implements ServletContextListener {

    private final StringBuffer buffer;

    public DependencyInjectionTestListener(StringBuffer buffer) {
        this.buffer = buffer;
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        buffer.append("-contextInitialized");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        buffer.append("-contextDestroyed");
    }

}
