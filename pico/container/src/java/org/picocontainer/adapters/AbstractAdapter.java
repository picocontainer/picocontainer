/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.adapters;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.ComponentMonitorStrategy;
import org.picocontainer.PicoVisitor;
import org.picocontainer.injectors.Provider;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.monitors.AbstractComponentMonitor;
import org.picocontainer.monitors.NullComponentMonitor;

import java.io.Serializable;

/**
 * Base class for a ComponentAdapter with general functionality.
 * This implementation provides basic checks for a healthy implementation of a ComponentAdapter.
 * It does not allow to use <code>null</code> for the component key or the implementation,
 * ensures that the implementation is a concrete class and that the key is assignable from the
 * implementation if the key represents a type.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Jon Tirs&eacute;n
 */
public abstract class AbstractAdapter<T> implements ComponentAdapter<T>, ComponentMonitorStrategy, Serializable {
    private Object key;
    private Class<T> impl;
    private ComponentMonitor monitor;

    /**
     * Constructs a new ComponentAdapter for the given key and implementation.
     * @param key the search key for this implementation
     * @param impl the concrete implementation
     */
    public AbstractAdapter(Object key, Class impl) {
        this(key, impl, new AbstractComponentMonitor());
        this.monitor = new NullComponentMonitor();
    }

    /**
     * Constructs a new ComponentAdapter for the given key and implementation.
     * @param key the search key for this implementation
     * @param impl the concrete implementation
     * @param monitor the component monitor used by this ComponentAdapter
     */
    public AbstractAdapter(Object key, Class impl, ComponentMonitor monitor) {
        if (monitor == null) {
            throw new NullPointerException("ComponentMonitor==null");
        }
        this.monitor = monitor;
        if (impl == null) {
            throw new NullPointerException("impl");
        }
        this.key = key;
        this.impl = impl;
        checkTypeCompatibility();
    }

    /**
     * {@inheritDoc}
     * @see org.picocontainer.ComponentAdapter#getComponentKey()
     */
    public Object getComponentKey() {
        if (key == null) {
            throw new NullPointerException("key");
        }
        return key;
    }

    /**
     * {@inheritDoc}
     * @see org.picocontainer.ComponentAdapter#getComponentImplementation()
     */
    public Class<? extends T> getComponentImplementation() {
        return impl;
    }

    protected void checkTypeCompatibility() {
        if (key instanceof Class) {
            Class<?> componentType = (Class) key;
            if (Provider.class.isAssignableFrom(impl)) {
                if (!componentType.isAssignableFrom(ProviderAdapter.getProvideMethod(impl).getReturnType())) {
                    throw newCCE(componentType);
                }
            } else {
                if (!componentType.isAssignableFrom(impl)) {
                    throw newCCE(componentType);
                }
            }
        }
    }

    private ClassCastException newCCE(Class<?> componentType) {
        return new ClassCastException(impl.getName() + " is not a " + componentType.getName());
    }

    /**
     * @return Returns the ComponentAdapter's class name and the component's key.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getDescriptor() + getComponentKey();
    }

    public void accept(PicoVisitor visitor) {
        visitor.visitComponentAdapter(this);
    }

    public void changeMonitor(ComponentMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Returns the monitor currently used
     * @return The ComponentMonitor currently used
     */
    public ComponentMonitor currentMonitor() {
        return monitor;
    }

    public final ComponentAdapter<T> getDelegate() {
        return null;
    }

    public final <U extends ComponentAdapter> U findAdapterOfType(Class<U> adapterType) {
        if (adapterType.isAssignableFrom(this.getClass())) {
            return (U) this;
        } else if (getDelegate() != null) {
            return getDelegate().findAdapterOfType(adapterType);
        }
        return null;
    }



}
