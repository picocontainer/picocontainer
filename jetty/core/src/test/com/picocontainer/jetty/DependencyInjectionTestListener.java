package com.picocontainer.jetty;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class DependencyInjectionTestListener implements ServletContextListener {

    private final StringBuffer buffer;

    public DependencyInjectionTestListener(final StringBuffer buffer) {
        this.buffer = buffer;
    }

    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        buffer.append("-contextInitialized");
    }

    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        buffer.append("-contextDestroyed");
    }

}
