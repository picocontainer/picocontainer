package com.picocontainer.script;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.containers.AbstractDelegatingMutablePicoContainer;

@SuppressWarnings("serial")
public class FooDecoratingPicoContainer extends AbstractDelegatingMutablePicoContainer {
    public FooDecoratingPicoContainer(final MutablePicoContainer delegate) {
        super(delegate);
    }
    @Override
	public MutablePicoContainer makeChildContainer() {
        return null;
    }

    @Override
	public MutablePicoContainer addComponent(final Object key, final Object implOrInstance, final Parameter... parameters) throws PicoCompositionException {
        Assert.assertEquals(HashMap.class, implOrInstance);
        return super.addComponent(ArrayList.class, ArrayList.class, parameters);
    }

}
