package org.picocontainer.jetty.groovy.adapters;

import groovy.util.NodeBuilder;

import java.util.Map;

import org.picocontainer.jetty.PicoContext;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.classname.ClassName;

public class WaffleAdapter {

    private final PicoContext context;
    private final MutablePicoContainer parentContainer;
    private final Map attributes;

    public WaffleAdapter(PicoContext context, MutablePicoContainer parentContainer, Map attributes) {
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
