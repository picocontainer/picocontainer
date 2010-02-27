package org.picocontainer.jetty.groovy;

import java.util.Map;

import org.mortbay.jetty.servlet.FilterHolder;

import groovy.util.NodeBuilder;

public final class FilterHolderBuilder extends NodeBuilder {

    private final FilterHolder filterHolder;

    public FilterHolderBuilder(FilterHolder filter) {
        this.filterHolder = filter;
    }

    protected Object createNode(Object name, Map map) {
        if (name.equals("initParam")) {
            return createInitParam(map);
        }
        return "";        
    }

    protected Object createInitParam(Map map) {
        String name = (String) map.remove("name");
        String value = (String) map.remove("value");
        filterHolder.setInitParameter(name, value);
        return null;
    }

}
