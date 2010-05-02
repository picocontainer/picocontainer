/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Mauro Talevi                                             *
 *****************************************************************************/

package org.picocontainer.monitors;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.picocontainer.ChangedBehavior;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.ComponentMonitorStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.Injector;

/**
 * <p>
 * A {@link ComponentMonitor monitor} which delegates to another monitor.
 * It provides a {@link NullComponentMonitor default ComponentMonitor},
 * but does not allow to use <code>null</code> for the delegate.
 * </p>
 * <p>
 * It also supports a {@link org.picocontainer.ComponentMonitorStrategy monitor strategy}
 * that allows to change the delegate.
 * </p>
 * 
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class AbstractComponentMonitor implements ComponentMonitor, ComponentMonitorStrategy, Serializable {

	
	/**
	 * Delegate monitor to allow for component monitor chaining.
	 */
	private  ComponentMonitor delegate;
    
    /**
     * Creates a AbstractComponentMonitor with a given delegate
     * @param delegate the ComponentMonitor to which this monitor delegates
     */
    public AbstractComponentMonitor(ComponentMonitor delegate) {
        checkMonitor(delegate);
        this.delegate = delegate;
    }

    /**
     * Creates a AbstractComponentMonitor with an instance of
     * {@link NullComponentMonitor}.
     */
    public AbstractComponentMonitor() {
        this(new NullComponentMonitor());
    }
    
    public <T> Constructor<T> instantiating(PicoContainer container, ComponentAdapter<T> componentAdapter,
                                     Constructor<T> constructor) {
        return delegate.instantiating(container, componentAdapter, constructor);
    }

    public <T> void instantiated(PicoContainer container, ComponentAdapter<T> componentAdapter,
                             Constructor<T> constructor,
                             Object instantiated,
                             Object[] injected,
                             long duration) {
        delegate.instantiated(container, componentAdapter, constructor, instantiated, injected, duration);
    }

    public <T> void instantiationFailed(PicoContainer container,
                                    ComponentAdapter<T> componentAdapter,
                                    Constructor<T> constructor,
                                    Exception e) {
        delegate.instantiationFailed(container, componentAdapter, constructor, e);
    }

    public Object invoking(PicoContainer container,
                           ComponentAdapter<?> componentAdapter,
                           Member member,
                           Object instance, Object[] args) {
        return delegate.invoking(container, componentAdapter, member, instance, args);
    }

    public void invoked(PicoContainer container,
                        ComponentAdapter<?> componentAdapter,
                        Member member,
                        Object instance,
                        long duration, Object[] args, Object retVal) {
        delegate.invoked(container, componentAdapter, member, instance, duration, args, retVal);
    }

    public void invocationFailed(Member member, Object instance, Exception e) {
        delegate.invocationFailed(member, instance, e);
    }

    public void lifecycleInvocationFailed(MutablePicoContainer container,
                                          ComponentAdapter<?> componentAdapter, Method method,
                                          Object instance,
                                          RuntimeException cause) {
        delegate.lifecycleInvocationFailed(container, componentAdapter, method,instance, cause);
    }

    public Object noComponentFound(MutablePicoContainer container, Object key) {
        return delegate.noComponentFound(container, key);
    }

    public Injector newInjector(Injector injector) {
        return injector;
    }

    public ChangedBehavior newBehavior(ChangedBehavior changedBehavior) {
        return changedBehavior;
    }

    /**
     * If the delegate supports a {@link ComponentMonitorStrategy monitor strategy},
     * this is used to changed the monitor while keeping the same delegate.
     * Else the delegate is replaced by the new monitor.
     * {@inheritDoc}
     */
    public void changeMonitor(ComponentMonitor monitor) {
        checkMonitor(monitor);
        if ( delegate instanceof ComponentMonitorStrategy ){
            ((ComponentMonitorStrategy)delegate).changeMonitor(monitor);
        } else {
            delegate = monitor;
        }
    }

    public ComponentMonitor currentMonitor() {
        if ( delegate instanceof ComponentMonitorStrategy ){
            return ((ComponentMonitorStrategy)delegate).currentMonitor();
        } else {
            return delegate;
        }
    }
    
    private void checkMonitor(ComponentMonitor monitor) {
        if ( monitor == null ){
            throw new NullPointerException("monitor");
        }
    }

}
