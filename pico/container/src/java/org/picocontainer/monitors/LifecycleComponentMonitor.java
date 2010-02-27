/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.monitors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoLifecycleException;
import org.picocontainer.Injector;
import org.picocontainer.Behavior;

/**
 * A {@link ComponentMonitor} which collects lifecycle failures
 * and rethrows them on demand after the failures.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public final class LifecycleComponentMonitor implements ComponentMonitor {

	/**
	 * Delegate for chained component monitors.
	 */
    private final ComponentMonitor delegate;
    
    private final List<RuntimeException> lifecycleFailures = new ArrayList<RuntimeException>();

    public LifecycleComponentMonitor(ComponentMonitor delegate) {
        this.delegate = delegate;
    }

    public LifecycleComponentMonitor() {
        this(new NullComponentMonitor());
    }

    public <T> Constructor<T> instantiating(PicoContainer container, ComponentAdapter<T> componentAdapter,
                                     Constructor<T> constructor) {
        return delegate.instantiating(container, componentAdapter, constructor);
    }

    public <T> void instantiated(PicoContainer container, ComponentAdapter<T> componentAdapter,
                             Constructor<T> constructor,
                             Object instantiated,
                             Object[] parameters,
                             long duration) {
        delegate.instantiated(container, componentAdapter, constructor, instantiated, parameters, duration);
    }

    public <T> void instantiationFailed(PicoContainer container,
                                    ComponentAdapter<T> componentAdapter,
                                    Constructor<T> constructor,
                                    Exception cause) {
        delegate.instantiationFailed(container, componentAdapter, constructor, cause);
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

    public void invocationFailed(Member member, Object instance, Exception cause) {
        delegate.invocationFailed(member, instance, cause);
    }

    public void lifecycleInvocationFailed(MutablePicoContainer container,
                                          ComponentAdapter<?> componentAdapter, Method method,
                                          Object instance,
                                          RuntimeException cause) {
        lifecycleFailures.add(cause);
        try {
            delegate.lifecycleInvocationFailed(container, componentAdapter, method, instance, cause);
        } catch (PicoLifecycleException e) {
            // do nothing, exception already logged for later rethrow.
        }
    }

    public Object noComponentFound(MutablePicoContainer container, Object componentKey) {
        return delegate.noComponentFound(container, componentKey);
    }

    public Injector newInjector(Injector injector) {
        return delegate.newInjector(injector);
    }

    /** {@inheritDoc} **/
    public Behavior newBehavior(Behavior behavior) {
        return delegate.newBehavior(behavior);
    }


    public void rethrowLifecycleFailuresException() {
        throw new LifecycleFailuresException(lifecycleFailures);
    }

    /**
     * Subclass of {@link PicoException} that is thrown when the collected
     * lifecycle failures need to be be collectively rethrown.
     * 
     * @author Paul Hammant
     * @author Mauro Talevi
     */
    public final class LifecycleFailuresException extends PicoException {

  		
		private final List<RuntimeException> lifecycleFailures;

        public LifecycleFailuresException(List<RuntimeException> lifecycleFailures) {
            this.lifecycleFailures = lifecycleFailures;
        }

        public String getMessage() {
            StringBuffer message = new StringBuffer();
            for (Object lifecycleFailure : lifecycleFailures) {
                Exception failure = (Exception)lifecycleFailure;
                message.append(failure.getMessage()).append(";  ");
            }
            return message.toString();
        }

        public Collection<RuntimeException> getFailures() {
            return lifecycleFailures;
        }
    }
}
