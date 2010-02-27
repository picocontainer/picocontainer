package org.picocontainer.script;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.containers.AbstractDelegatingMutablePicoContainer;

@SuppressWarnings("serial")
public class FooDecoratingPicoContainer extends AbstractDelegatingMutablePicoContainer {
    public FooDecoratingPicoContainer(MutablePicoContainer delegate) {
        super(delegate);
    }
    public MutablePicoContainer makeChildContainer() {
        return null;
    }

    public MutablePicoContainer addComponent(Object componentKey, Object componentImplementationOrInstance, Parameter... parameters) throws PicoCompositionException {
        Assert.assertEquals(HashMap.class, componentImplementationOrInstance);
        return super.addComponent(ArrayList.class, ArrayList.class, parameters);
    }

}
