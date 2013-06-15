package org.picocontainer.jetty.groovy;

import groovy.util.NodeBuilder;

import java.util.Map;

import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.jetty.PicoContext;

public class CustomNodeBuilder extends NodeBuilder {

    public CustomNodeBuilder(final PicoContainer parentContainer, final PicoContext context, final Map attributes) {
        PicoContainer parentContainer1 = parentContainer;
        PicoContext context1 = context;
    }

    @Override
	public Object createNode(final Object name, final Map attributes) {
        String value = (String) attributes.get("name");
        if (value == null || !value.equals("value")) {
            throw new PicoCompositionException("Attribute 'name' should have value 'value'");
        }
        return value;
    }

}