package com.picocontainer.jetty.groovy.adapters;

import groovy.util.NodeBuilder;

import java.util.Map;

import com.picocontainer.jetty.PicoContext;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.classname.ClassName;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;

public class NodeBuilderAdapter {

    private final String nodeBuilderClassName;
    private final PicoContext context;
    private final MutablePicoContainer parentContainer;
    private final Map attributes;

    public NodeBuilderAdapter(final String nodeBuilderClassName, final PicoContext context, final MutablePicoContainer parentContainer, final Map attributes) {
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
