package org.picocontainer.jetty.groovy.adapters;

import groovy.util.NodeBuilder;

import java.util.Map;

import org.picocontainer.jetty.PicoContext;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.classname.ClassName;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;

public class NodeBuilderAdapter {
    
    private final String nodeBuilderClassName;    
    private final PicoContext context;
    private final MutablePicoContainer parentContainer;
    private final Map attributes;

    public NodeBuilderAdapter(String nodeBuilderClassName, PicoContext context, MutablePicoContainer parentContainer, Map attributes) {
        this.nodeBuilderClassName = nodeBuilderClassName;
        this.context = context;
        this.parentContainer = parentContainer;
        this.attributes = attributes;
    }
    
    public NodeBuilder getNodeBuilder() {
        DefaultClassLoadingPicoContainer factory = new DefaultClassLoadingPicoContainer();
        factory.addComponent(PicoContext.class, context);
        factory.addComponent(MutablePicoContainer.class, parentContainer);
        factory.addComponent(Map.class, attributes);
        factory.addComponent("wb", new ClassName(nodeBuilderClassName));
        return (NodeBuilder) factory.getComponent("wb");
    }

}
