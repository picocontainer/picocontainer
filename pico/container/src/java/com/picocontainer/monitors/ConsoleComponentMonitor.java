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
import static com.picocontainer.monitors.ComponentMonitorHelper.parmsToString;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
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
 * A {@link ComponentMonitor} which writes to a {@link OutputStream}.
 * This is typically used to write to a console.
 * (TODO  After serialization, the output printstream is null)
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class ConsoleComponentMonitor implements ComponentMonitor, Serializable {

	/**
	 * The outgoing print stream.
	 */
    private final transient PrintStream out;

    /**
     * Delegate component monitor (for component monitor chains).
     */
    private final ComponentMonitor delegate;

    /**
     * Constructs a console component monitor that sends output to <tt>System.out</tt>.
     */
    public ConsoleComponentMonitor() {
        this(System.out);
    }

    /**
     * Constructs a console component monitor that sends output to the specified output stream.
     *
     * @param out  the designated output stream.  Options include System.out, Socket streams, File streams,
     * etc.
     */
    public ConsoleComponentMonitor(final OutputStream out) {
        this(out, new NullComponentMonitor());
    }

    /**
     * Constructs a console component monitor chain that sends output to the specified output stream
     * and then sends all events to the delegate component monitor.
     * @param out the output stream of choice.
     * @param delegate the next monitor in the component monitor chain to receive event information.
     */
    public ConsoleComponentMonitor(final OutputStream out, final ComponentMonitor delegate) {
        this.out = new PrintStream(out);
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
                             final Object[] parameters,
                             final long duration) {
        out.println(format(ComponentMonitorHelper.INSTANTIATED, ctorToString(constructor), duration, instantiated.getClass().getName(), parmsToString(parameters)));
        delegate.instantiated(container, componentAdapter, constructor, instantiated, parameters, duration);
    }

    public <T> void instantiationFailed(final PicoContainer container,
                                    final ComponentAdapter<T> componentAdapter,
                                    final Constructor<T> constructor,
                                    final Exception cause) {
        out.println(format(ComponentMonitorHelper.INSTANTIATION_FAILED, ctorToString(constructor), cause.getMessage()));
        delegate.instantiationFailed(container, componentAdapter, constructor, cause);
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
                        final long duration,
                        final Object retVal, final Object... args) {
        out.println(format(ComponentMonitorHelper.INVOKED, memberToString(member), instance, duration));
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
        out.println(format(ComponentMonitorHelper.LIFECYCLE_INVOCATION_FAILED, memberToString(method), instance, cause.getMessage()));
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
