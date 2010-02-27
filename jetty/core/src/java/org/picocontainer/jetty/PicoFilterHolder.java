package org.picocontainer.jetty;

import javax.servlet.Filter;

import org.mortbay.jetty.servlet.FilterHolder;
import org.picocontainer.PicoContainer;
import org.picocontainer.DefaultPicoContainer;

public class PicoFilterHolder extends FilterHolder {

    private final PicoContainer parentContainer;

    public PicoFilterHolder(PicoContainer parentContainer) {
        this.parentContainer = parentContainer;
    }

    public PicoFilterHolder(Class filterClass, PicoContainer parentContainer) {
        super(filterClass);
        this.parentContainer = parentContainer;
    }

    public synchronized Object newInstance() throws InstantiationException, IllegalAccessException {
        DefaultPicoContainer child = new DefaultPicoContainer(parentContainer);
        child.addComponent(Filter.class, _class);
        return child.getComponent(Filter.class);
    }

}
