/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant & Obie Fernandez & Aslak Helles&oslash;y    *
 *****************************************************************************/

package org.picocontainer.monitors;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.picocontainer.ChangedBehavior;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoLifecycleException;
import org.picocontainer.Injector;

/**
 * A {@link ComponentMonitor} which does nothing. 
 * 
 * @author Paul Hammant
 * @author Obie Fernandez
 */
@SuppressWarnings("serial")
public class NullComponentMonitor implements ComponentMonitor, Serializable {

    public <T> Constructor<T> instantiating(PicoContainer container, ComponentAdapter<T> componentAdapter,
                                     Constructor<T> constructor) {
        return constructor;
    }

    public <T> void instantiationFailed(PicoContainer container,
                                    ComponentAdapter<T> componentAdapter,
                                    Constructor<T> constructor,
                                    Exception e) {
    }

    public <T> void instantiated(PicoContainer container, ComponentAdapter<T>  componentAdapter,
                             Constructor<T>  constructor,
                             Object instantiated,
                             Object[] injected,
                             long duration) {
    }

    public Object invoking(PicoContainer container,
                           ComponentAdapter<?> componentAdapter,
                           Member member,
                           Object instance, Object[] args) {
        return KEEP;
    }

    public void invoked(PicoContainer container,
                        ComponentAdapter<?> componentAdapter,
                        Member member,
                        Object instance,
                        long duration, Object[] args, Object retVal) {
    }

    public void invocationFailed(Member member, Object instance, Exception e) {
    }

    public void lifecycleInvocationFailed(MutablePicoContainer container,
                                          ComponentAdapter<?> componentAdapter, Method method,
                                          Object instance,
                                          RuntimeException cause) {
        if (cause instanceof PicoLifecycleException) {
            throw cause;
        }
        throw new PicoLifecycleException(method, instance, cause);
    }

    public Object noComponentFound(MutablePicoContainer container, Object componentKey) {
        return null;
    }

    public Injector newInjector(Injector injector) {
        return injector;
    }

    /** {@inheritDoc} **/
    public ChangedBehavior newBehavior(ChangedBehavior changedBehavior) {
        return changedBehavior;
    }
    

}
