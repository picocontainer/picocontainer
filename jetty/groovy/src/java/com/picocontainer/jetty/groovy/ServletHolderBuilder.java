package com.picocontainer.jetty.groovy;

import groovy.util.NodeBuilder;

import java.util.Map;

import org.eclipse.jetty.servlet.ServletHolder;

public final class ServletHolderBuilder extends NodeBuilder {

    private final ServletHolder servletHolder;

    public ServletHolderBuilder(final ServletHolder servlet) {
        this.servletHolder = servlet;
    }

    @Override
	protected Object createNode(final Object name, final Map map) {
        if (name.equals("initParam")) {
            return createInitParam(map);
        }
        return null;
    }

    protected Object createInitParam(final Map map) {
        String name = (String) map.remove("name");
        String value = (String) map.remove("value");
        servletHolder.setInitParameter(name, value);
        return null;
    }

}
