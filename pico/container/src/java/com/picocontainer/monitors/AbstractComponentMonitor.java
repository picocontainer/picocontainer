/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Mauro Talevi                                             *
 *****************************************************************************/

package com.picocontainer.monitors;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.picocontainer.ChangedBehavior;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.ComponentMonitorStrategy;
import com.picocontainer.Injector;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;

/**
 * <p>
 * A {@link ComponentMonitor monitor} which delegates to another monitor.
 * It provides a {@link NullComponentMonitor default ComponentMonitor},
 * but does not allow to use <code>null</code> for the delegate.
 * </p>
 * <p>
 * It also supports a {@link com.picocontainer.ComponentMonitorStrategy monitor strategy}
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
    public AbstractComponentMonitor(final ComponentMonitor delegate) {
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

    public <T> Constructor<T> instantiating(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                                     final Constructor<T> constructor) {
        return delegate.instantiating(container, componentAdapter, constructor);
    }

    public <T> void instantiated(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                             final Constructor<T> constructor,
                             final Object instantiated,
                             final Object[] injected,
                             final long duration) {
        delegate.instantiated(container, componentAdapter, constructor, instantiated, injected, duration);
    }

    public <T> void instantiationFailed(final PicoContainer container,
                                    final ComponentAdapter<T> componentAdapter,
                                    final Constructor<T> constructor,
                                    final Exception e) {
        delegate.instantiationFailed(container, componentAdapter, constructor, e);
    }

    public Object invoking(final PicoContainer container,
                           final ComponentAdapter<?> componentAdapter,
                           final Member member,
                           final Object instance, final Object... args) {
        return delegate.invoking(container, componentAdapter, member, instance, args);
    }

    public void invoked(final PicoContainer container,
                        final ComponentAdapter<?> componentAdapter,
                        final Member member,
                        final Object instance,
                        final long duration, final Object retVal, final Object[] args) {
        delegate.invoked(container, componentAdapter, member, instance, duration, retVal, args);
    }

    public void invocationFailed(final Member member, final Object instance, final Exception e) {
        delegate.invocationFailed(member, instance, e);
    }

    public void lifecycleInvocationFailed(final MutablePicoContainer container,
                                          final ComponentAdapter<?> componentAdapter, final Method method,
                                          final Object instance,
                                          final RuntimeException cause) {
        delegate.lifecycleInvocationFailed(container, componentAdapter, method,instance, cause);
    }

    public Object noComponentFound(final MutablePicoContainer container, final Object key) {
        return delegate.noComponentFound(container, key);
    }

    public Injector newInjector(final Injector injector) {
        return injector;
    }

    public ChangedBehavior changedBehavior(final ChangedBehavior changedBehavior) {
        return changedBehavior;
    }

    /**
     * If the delegate supports a {@link ComponentMonitorStrategy monitor strategy},
     * this is used to changed the monitor while keeping the same delegate.
     * Else the delegate is replaced by the new monitor.
     * {@inheritDoc}
     */
    public void changeMonitor(final ComponentMonitor monitor) {
        checkMonitor(monitor);
        if (delegate instanceof ComponentMonitorStrategy) {
            ((ComponentMonitorStrategy)delegate).changeMonitor(monitor);
        } else {
            delegate = monitor;
        }
    }

    public ComponentMonitor currentMonitor() {
        if (delegate instanceof ComponentMonitorStrategy) {
            return ((ComponentMonitorStrategy)delegate).currentMonitor();
        } else {
            return delegate;
        }
    }

    private void checkMonitor(final ComponentMonitor monitor) {
        if (monitor == null) {
            throw new NullPointerException("monitor");
        }
    }

}
