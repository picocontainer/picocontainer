/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by the committers                                           *
 *****************************************************************************/
package org.picocontainer.containers;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.lifecycle.LifecycleState;

import javax.inject.Provider;
import java.util.Properties;

/**
 * abstract base class for delegating to mutable containers
 * @author Paul Hammant
 */
public abstract class AbstractDelegatingMutablePicoContainer extends AbstractDelegatingPicoContainer implements MutablePicoContainer {

    public AbstractDelegatingMutablePicoContainer(MutablePicoContainer delegate) {
		super(delegate);
	}

    public <T> BindWithOrTo<T> bind(Class<T> type) {
        return getDelegate().bind(type);
    }

	public MutablePicoContainer addComponent(Object key,
                                             Object implOrInstance,
                                             Parameter... parameters) throws PicoCompositionException {
        return getDelegate().addComponent(key, implOrInstance, parameters);
    }

    public MutablePicoContainer addComponent(Object implOrInstance) throws PicoCompositionException {
        return getDelegate().addComponent(implOrInstance);
    }

    public MutablePicoContainer addConfig(String name, Object val) {
        return getDelegate().addConfig(name, val); 
    }

    public MutablePicoContainer addAdapter(ComponentAdapter<?> componentAdapter) throws PicoCompositionException {
        return getDelegate().addAdapter(componentAdapter);
    }

    public MutablePicoContainer addProvider(Provider<?> provider) {
        return getDelegate().addAdapter(new ProviderAdapter(provider));
    }


    public <T> ComponentAdapter<T> removeComponent(Object key) {
        return getDelegate().removeComponent(key);
    }

    public <T> ComponentAdapter<T> removeComponentByInstance(T componentInstance) {
        return getDelegate().removeComponentByInstance(componentInstance);
    }

    public MutablePicoContainer addChildContainer(PicoContainer child) {
        return getDelegate().addChildContainer(child);
    }

    public boolean removeChildContainer(PicoContainer child) {
        return getDelegate().removeChildContainer(child);
    }

	public MutablePicoContainer change(Properties... properties) {
	    return getDelegate().change(properties);
	}

	public MutablePicoContainer as(Properties... properties) {
	    return getDelegate().as(properties);
	}
	
	public void dispose() {
		getDelegate().dispose();
	}

	public MutablePicoContainer makeChildContainer() {

		return null;
	}

	public void start() {
		getDelegate().start();
	}

	public void stop() {
		getDelegate().stop();
	}

	public MutablePicoContainer getDelegate() {
		return (MutablePicoContainer) super.getDelegate();
	}

    public void setName(String name) {
        getDelegate().setName(name);
    }

    public void setLifecycleState(LifecycleState lifecycleState) {
        getDelegate().setLifecycleState(lifecycleState);
    }
    
    /** {@inheritDoc} **/
    public LifecycleState getLifecycleState() {
        return getDelegate().getLifecycleState();
    }
    
    /** {@inheritDoc} **/
    public String getName() {
        return getDelegate().getName();
    }

    public void changeMonitor(ComponentMonitor monitor) {
        getDelegate().changeMonitor(monitor);
    }
}
