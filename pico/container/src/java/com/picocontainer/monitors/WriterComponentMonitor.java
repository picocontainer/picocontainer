/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammaant                                            *
 *****************************************************************************/

package com.picocontainer.monitors;

import static com.picocontainer.monitors.ComponentMonitorHelper.ctorToString;
import static com.picocontainer.monitors.ComponentMonitorHelper.format;
import static com.picocontainer.monitors.ComponentMonitorHelper.memberToString;
import static com.picocontainer.monitors.ComponentMonitorHelper.methodToString;
import static com.picocontainer.monitors.ComponentMonitorHelper.parmsToString;

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.picocontainer.ChangedBehavior;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.Injector;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;

/**
 * A {@link ComponentMonitor} which writes to a {@link Writer}.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public class WriterComponentMonitor implements ComponentMonitor {

    private final PrintWriter out;
    private final ComponentMonitor delegate;

    public WriterComponentMonitor(final Writer out) {
        this(out, new NullComponentMonitor());
    }

    public WriterComponentMonitor(final Writer out, final ComponentMonitor delegate) {
        this.out = new PrintWriter(out);
        this.delegate = delegate;
    }

    public <T> Constructor<T> instantiating(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                                     final Constructor<T> constructor) {
        out.println(format(ComponentMonitorHelper.INSTANTIATING, ctorToString(constructor)));
        return delegate.instantiating(container, componentAdapter, constructor);
    }

    public <T> void instantiated(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                             final Constructor<T> constructor,
                             final Object instantiated,
                             final Object[] injected,
                             final long duration) {
        out.println(format(ComponentMonitorHelper.INSTANTIATED, ctorToString(constructor), duration, instantiated.getClass().getName(), parmsToString(injected)));
        delegate.instantiated(container, componentAdapter, constructor, instantiated, injected, duration);
    }

    public <T> void instantiationFailed(final PicoContainer container,
                                    final ComponentAdapter<T> componentAdapter,
                                    final Constructor<T> constructor,
                                    final Exception cause) {
        out.println(format(ComponentMonitorHelper.INSTANTIATION_FAILED, ctorToString(constructor), cause.getMessage()));
        delegate.instantiationFailed(container, null, constructor, cause);
    }

    public Object invoking(final PicoContainer container,
                           final ComponentAdapter<?> componentAdapter,
                           final Member member,
                           final Object instance, final Object... args) {
        out.println(format(ComponentMonitorHelper.INVOKING, memberToString(member), instance));
        return delegate.invoking(container, componentAdapter, member, instance, args);
    }

    public void invoked(final PicoContainer container,
                        final ComponentAdapter<?> componentAdapter,
                        final Member member,
                        final Object instance,
                        final long duration, final Object retVal, final Object[] args) {
        out.println(format(ComponentMonitorHelper.INVOKED, methodToString(member), instance, duration));
        delegate.invoked(container, componentAdapter, member, instance, duration, retVal, args);
    }

    public void invocationFailed(final Member member, final Object instance, final Exception cause) {
        out.println(format(ComponentMonitorHelper.INVOCATION_FAILED, memberToString(member), instance, cause.getMessage()));
        delegate.invocationFailed(member, instance, cause);
    }

    public void lifecycleInvocationFailed(final MutablePicoContainer container,
                                          final ComponentAdapter<?> componentAdapter, final Method method,
                                          final Object instance,
                                          final RuntimeException cause) {
        out.println(format(ComponentMonitorHelper.LIFECYCLE_INVOCATION_FAILED, methodToString(method), instance, cause.getMessage()));
        delegate.lifecycleInvocationFailed(container, componentAdapter, method, instance, cause);
    }

    public Object noComponentFound(final MutablePicoContainer container, final Object key) {
        out.println(format(ComponentMonitorHelper.NO_COMPONENT, key));
        return delegate.noComponentFound(container, key);
    }

    public Injector newInjector(final Injector injector) {
        return delegate.newInjector(injector);
    }

    /** {@inheritDoc} **/
    public ChangedBehavior changedBehavior(final ChangedBehavior changedBehavior) {
        return delegate.changedBehavior(changedBehavior);
    }


}
