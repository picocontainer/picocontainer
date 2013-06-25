package com.picocontainer.jetty.groovy.adapters;

import groovy.util.NodeBuilder;

import java.util.Map;

import com.picocontainer.jetty.PicoContext;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.classname.ClassName;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;

public class WaffleAdapter {

    private final PicoContext context;
    private final MutablePicoContainer parentContainer;
    private final Map attributes;

    public WaffleAdapter(final PicoContext context, final MutablePicoContainer parentContainer, final Map attributes) {
        this.context = context;
        this.parentContainer = parentContainer;
        this.attributes = attributes;
    }

    public NodeBuilder getNodeBuilder() {
        String className = "com.thoughtworks.waffle.groovy.WaffleBuilder";
        DefaultClassLoadingPicoContainer factory = new DefaultClassLoadingPicoContainer();
        factory.addComponent(PicoContext.class, context);
        factory.addComponent(MutablePicoContainer.class, parentContainer);
        factory.addComponent(Map.class, attributes);
        factory.addComponent("wb", new ClassName(className));
        return (NodeBuilder) factory.getComponent("wb");
    }

}
