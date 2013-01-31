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
@SuppressWarnings("serial")
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
        getDelegate().addComponent(key, implOrInstance, parameters);
        return this;
    }

    public MutablePicoContainer addComponent(Object implOrInstance) throws PicoCompositionException {
	     getDelegate().addComponent(implOrInstance);
	     return this;
    }

    public MutablePicoContainer addConfig(String name, Object val) {
        getDelegate().addConfig(name, val);
        return this;
    }

    public MutablePicoContainer addAdapter(ComponentAdapter<?> componentAdapter) throws PicoCompositionException {
        getDelegate().addAdapter(componentAdapter);
        return this;
    }

    public MutablePicoContainer addProvider(javax.inject.Provider<?> provider) {
        getDelegate().addProvider(provider);
        return this;
    }
    
    public MutablePicoContainer addProvider(Object key, javax.inject.Provider<?> provider) {
    	getDelegate().addProvider(key, provider);
    	return this;
    }


    public <T> ComponentAdapter<T> removeComponent(Object key) {
        return getDelegate().removeComponent(key);
    }

    public <T> ComponentAdapter<T> removeComponentByInstance(T componentInstance) {
        return getDelegate().removeComponentByInstance(componentInstance);
    }

    public MutablePicoContainer addChildContainer(PicoContainer child) {
        getDelegate().addChildContainer(child);
        return this;
    }

    public boolean removeChildContainer(PicoContainer child) {
        return getDelegate().removeChildContainer(child);
    }

	public MutablePicoContainer change(Properties... properties) {
	    getDelegate().change(properties);
	    return this;
	}

	public MutablePicoContainer as(Properties... properties) {
	    getDelegate().as(properties);
	    return this;
	}
	
	public void dispose() {
		getDelegate().dispose();
	}

	abstract public MutablePicoContainer makeChildContainer();
	
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
