package org.picocontainer.jetty.groovy;

import groovy.util.NodeBuilder;

import java.util.Map;

import org.eclipse.jetty.servlet.FilterHolder;

public final class FilterHolderBuilder extends NodeBuilder {

    private final FilterHolder filterHolder;

    public FilterHolderBuilder(final FilterHolder filter) {
        this.filterHolder = filter;
    }

    @Override
	protected Object createNode(final Object name, final Map map) {
        if (name.equals("initParam")) {
            return createInitParam(map);
        }
        return "";
    }

    protected Object createInitParam(final Map map) {
        String name = (String) map.remove("name");
        String value = (String) map.remove("value");
        filterHolder.setInitParameter(name, value);
        return null;
    }

}
