package org.picocontainer.jetty;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
