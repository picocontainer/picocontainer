package org.picocontainer.jetty.groovy;

import groovy.util.NodeBuilder;

import java.util.Map;

import org.picocontainer.jetty.PicoContext;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoCompositionException;

public class CustomNodeBuilder extends NodeBuilder {

    public CustomNodeBuilder(PicoContainer parentContainer, PicoContext context, Map attributes) {
        PicoContainer parentContainer1 = parentContainer;
        PicoContext context1 = context;
    }

    public Object createNode(Object name, Map attributes) {
        String value = (String) attributes.get("name");
        if ( value == null || !value.equals("value") ){
            throw new PicoCompositionException("Attribute 'name' should have value 'value'");
        }
        return value;
    }
    
}