/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.adapters;

import org.picocontainer.*;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;

import java.lang.reflect.Type;

/**
 * <p>
 * Component adapter which wraps a component instance.
 * </p>
 * <p>
 * This component adapter supports both a {@link org.picocontainer.ChangedBehavior Behavior} and a
 * {@link org.picocontainer.LifecycleStrategy LifecycleStrategy} to control the lifecycle of the component.
 * The lifecycle manager methods simply delegate to the lifecycle strategy methods
 * on the component instance.
 * </p>
 *
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public final class InstanceAdapter<T> extends AbstractAdapter<T> implements ComponentLifecycle<T>, LifecycleStrategy {

    /**
     * The actual instance of the component.
     */
    private final T componentInstance;

    /**
     * Lifecycle Strategy for the component adpater.
     */
    private final LifecycleStrategy lifecycle;
    private boolean started;


    public InstanceAdapter(Object key, T componentInstance, LifecycleStrategy lifecycle, ComponentMonitor monitor) throws PicoCompositionException {
        super(key, getInstanceClass(componentInstance), monitor);
        this.componentInstance = componentInstance;
        this.lifecycle = lifecycle;
    }

    public InstanceAdapter(Object key, T componentInstance) {
        this(key, componentInstance, new NullLifecycleStrategy(), new NullComponentMonitor());
    }

    public InstanceAdapter(Object key, T componentInstance, LifecycleStrategy lifecycle) {
        this(key, componentInstance, lifecycle, new NullComponentMonitor());
    }

    public InstanceAdapter(Object key, T componentInstance, ComponentMonitor monitor) {
        this(key, componentInstance, new NullLifecycleStrategy(), monitor);
    }

    private static Class getInstanceClass(Object componentInstance) {
        if (componentInstance == null) {
            throw new NullPointerException("componentInstance cannot be null");
        }
        return componentInstance.getClass();
    }

    public T getComponentInstance(PicoContainer container, Type into) {
        return componentInstance;
    }

    public void verify(PicoContainer container) {
    }

    public String getDescriptor() {
        return "Instance-";
    }

    public void start(PicoContainer container) {
        start(componentInstance);
    }

    public void stop(PicoContainer container) {
        stop(componentInstance);
    }

    public void dispose(PicoContainer container) {
        dispose(componentInstance);
    }

    public boolean componentHasLifecycle() {
        return hasLifecycle(componentInstance.getClass());
    }

    public boolean isStarted() {
        return started;
    }

    // ~~~~~~~~ LifecycleStrategy ~~~~~~~~

    public void start(Object component) {
        lifecycle.start(componentInstance);
        started = true;
    }

    public void stop(Object component) {
        lifecycle.stop(componentInstance);
        started = false;
    }

    public void dispose(Object component) {
        lifecycle.dispose(componentInstance);
    }

    public boolean hasLifecycle(Class<?> type) {
        return lifecycle.hasLifecycle(type);
    }

    public boolean isLazy(ComponentAdapter<?> adapter) {
        return lifecycle.isLazy(adapter);
    }
}
