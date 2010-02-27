/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Jon Tirsen                                               *
 *****************************************************************************/

package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.ComponentMonitorStrategy;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * <p>
 * Component adapter which decorates another adapter.
 * </p>
 * <p>
 * This adapter supports a {@link org.picocontainer.ComponentMonitorStrategy component monitor strategy}
 * and will propagate change of monitor to the delegate if the delegate itself
 * support the monitor strategy.
 * </p>
 * <p>
 * This adapter also supports a {@link Behavior lifecycle manager} and a
 * {@link org.picocontainer.LifecycleStrategy lifecycle strategy} if the delegate does.
 * </p>
 * 
 * @author Jon Tirsen
 * @author Aslak Hellesoy
 * @author Mauro Talevi
 */
public abstract class AbstractBehavior<T> implements org.picocontainer.Behavior<T>, ComponentMonitorStrategy,
                                                  LifecycleStrategy, Serializable {

    protected final ComponentAdapter<T> delegate;

    public AbstractBehavior(ComponentAdapter<T> delegate) {
        this.delegate = delegate;
    }
    
    public Object getComponentKey() {
        return delegate.getComponentKey();
    }

    public Class<? extends T> getComponentImplementation() {
        return delegate.getComponentImplementation();
    }

    public T getComponentInstance(PicoContainer container) throws PicoCompositionException {
        return getComponentInstance(container, NOTHING.class);
    }

    public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
        return (T) delegate.getComponentInstance(container, into);
    }

    public void verify(PicoContainer container) throws PicoCompositionException {
        delegate.verify(container);
    }

    public final ComponentAdapter<T> getDelegate() {
        return delegate;
    }

    @SuppressWarnings("unchecked")
    public final <U extends ComponentAdapter> U findAdapterOfType(Class<U> adapterType) {
        if (adapterType.isAssignableFrom(this.getClass())) {
            return (U) this;
        } else {
            return delegate.findAdapterOfType(adapterType);
        }
    }

    public void accept(PicoVisitor visitor) {
        visitor.visitComponentAdapter(this);
        delegate.accept(visitor);
    }

    /**
     * Delegates change of monitor if the delegate supports 
     * a component monitor strategy.
     * {@inheritDoc}
     */
    public void changeMonitor(ComponentMonitor monitor) {
        if (delegate instanceof ComponentMonitorStrategy ) {
            ((ComponentMonitorStrategy)delegate).changeMonitor(monitor);
        }
    }

    /**
     * Returns delegate's current monitor if the delegate supports 
     * a component monitor strategy.
     * {@inheritDoc}
     * @throws PicoCompositionException if no component monitor is found in delegate
     */
    public ComponentMonitor currentMonitor() {
        if (delegate instanceof ComponentMonitorStrategy ) {
            return ((ComponentMonitorStrategy)delegate).currentMonitor();
        }
        throw new PicoCompositionException("No component monitor found in delegate");
    }

    /**
     * Invokes delegate start method if the delegate is a Behavior
     * {@inheritDoc}
     */
    public void start(PicoContainer container) {
        if (delegate instanceof org.picocontainer.Behavior) {
            ((org.picocontainer.Behavior<?>)delegate).start(container);
        }
    }

    /**
     * Invokes delegate stop method if the delegate is a Behavior
     * {@inheritDoc}
     */
    public void stop(PicoContainer container) {
        if (delegate instanceof org.picocontainer.Behavior) {
            ((org.picocontainer.Behavior<?>)delegate).stop(container);
        }
    }
    
    /**
     * Invokes delegate dispose method if the delegate is a Behavior
     * {@inheritDoc}
     */
    public void dispose(PicoContainer container) {
        if (delegate instanceof org.picocontainer.Behavior) {
            ((org.picocontainer.Behavior<?>)delegate).dispose(container);
        }
    }

    /**
     * Invokes delegate hasLifecycle method if the delegate is a Behavior
     * {@inheritDoc}
     */
    public boolean componentHasLifecycle() {
        if (delegate instanceof org.picocontainer.Behavior) {
            return ((org.picocontainer.Behavior<?>)delegate).componentHasLifecycle();
        }
        return false;
    }

    public boolean isStarted() {
        if (delegate instanceof org.picocontainer.Behavior) {
            return ((org.picocontainer.Behavior<?>)delegate).isStarted();
        }
        return false;
    }

// ~~~~~~~~ LifecycleStrategy ~~~~~~~~

    /**
     * Invokes delegate start method if the delegate is a LifecycleStrategy
     * {@inheritDoc}
     */
    public void start(Object component) {
        if (delegate instanceof LifecycleStrategy ) {
            ((LifecycleStrategy)delegate).start(component);
        }
    }

    /**
     * Invokes delegate stop method if the delegate is a LifecycleStrategy
     * {@inheritDoc}
     */
    public void stop(Object component) {
        if (delegate instanceof LifecycleStrategy ) {
            ((LifecycleStrategy)delegate).stop(component);
        }
    }

    /**
     * Invokes delegate dispose method if the delegate is a LifecycleStrategy
     * {@inheritDoc}
     */
    public void dispose(Object component) {
        if (delegate instanceof LifecycleStrategy ) {
            ((LifecycleStrategy)delegate).dispose(component);
        }
    }

    /**
     * Invokes delegate hasLifecycle(Class) method if the delegate is a LifecycleStrategy
     * {@inheritDoc}
     */
    public boolean hasLifecycle(Class<?> type) {
        return delegate instanceof LifecycleStrategy && ((LifecycleStrategy) delegate).hasLifecycle(type);
    }

    public boolean isLazy(ComponentAdapter<?> adapter) {
        return delegate instanceof LifecycleStrategy && ((LifecycleStrategy) delegate).isLazy(adapter);
    }

    public String toString() {
        return getDescriptor() + ":" + delegate.toString();
    }
}

