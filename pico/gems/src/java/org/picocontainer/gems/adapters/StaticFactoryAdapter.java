/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Leo Simmons & J&ouml;rg Schaible                         *
 *****************************************************************************/
package org.picocontainer.gems.adapters;

import org.picocontainer.PicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoVerificationException;
import org.picocontainer.adapters.AbstractAdapter;

import java.lang.reflect.Type;


/**
 * Component adapter that wrapps a static factory with the help of {@link StaticFactory}.
 *
 * @author J&ouml;rg Schaible
 * @author Leo Simmons
 */
@SuppressWarnings("serial")
public final class StaticFactoryAdapter<T> extends AbstractAdapter<T> {
    
	
	private final StaticFactory<T> staticFactory;

    /**
     * Construct a ComponentAdapter accessing a static factory creating the component.
     *
     * @param type The type of the created component.
     * @param staticFactory Wrapper instance for the static factory.
     */
    public StaticFactoryAdapter(final Class<T> type, final StaticFactory<T> staticFactory) {

        this(type, type, staticFactory);
    }

    /**
     * Construct a ComponentAdapter accessing a static factory creating the component using a special key for addComponent
     * registration.
     *
     * @param componentKey The key of the created component.
     * @param type The type of the created component.
     * @param staticFactory Wrapper instance for the static factory.
     */
    public StaticFactoryAdapter(final Object componentKey, final Class<T> type, final StaticFactory<T> staticFactory) {
        super(componentKey, type);
        this.staticFactory = staticFactory;
    }

    /**
     * @return Returns the component created by the static factory.
     * @see org.picocontainer.ComponentAdapter#getComponentInstance(org.picocontainer.PicoContainer, java.lang.Class into)
     */
    public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
        return staticFactory.get();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.picocontainer.ComponentAdapter#verify(org.picocontainer.PicoContainer)
     */
    public void verify(final PicoContainer container) throws PicoVerificationException {
    }

    public String getDescriptor() {
        return "StaticFactory";
    }
}