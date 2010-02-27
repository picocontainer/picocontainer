/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.lifecycle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.PicoLifecycleException;

import javax.annotation.PreDestroy;
import javax.annotation.PostConstruct;

/**
 * Java EE 5 has some annotations PreDestroy and PostConstruct that map to start() and dispose() in our world
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public final class JavaEE5LifecycleStrategy extends AbstractMonitoringLifecycleStrategy {

    /**
     * Construct a JavaEE5LifecycleStrategy.
     *
     * @param monitor the monitor to use
     * @throws NullPointerException if the monitor is <code>null</code>
     */
    public JavaEE5LifecycleStrategy(final ComponentMonitor monitor) {
        super(monitor);
    }

    /** {@inheritDoc} **/
    public void start(final Object component) {
        doLifecycleMethod(component, PostConstruct.class);
    }

	/** {@inheritDoc} **/
    public void stop(final Object component) {
    }

    /** {@inheritDoc} **/
    public void dispose(final Object component) {
        doLifecycleMethod(component, PreDestroy.class);
    }

    private void doLifecycleMethod(final Object component, Class<? extends Annotation> annotation) {
        Method[] methods = component.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.isAnnotationPresent(annotation)) {
                try {
                    long str = System.currentTimeMillis();
                    currentMonitor().invoking(null, null, method, component, new Object[0]);
                    method.invoke(component);
                    currentMonitor().invoked(null, null, method, component, System.currentTimeMillis() - str, new Object[0], null);
                } catch (IllegalAccessException e) {
                    throw new PicoLifecycleException(method, component, e);
                } catch (InvocationTargetException e) {
                    throw new PicoLifecycleException(method, component, e);
                }
            }
        }
    }


    /**
     * {@inheritDoc} The component has a lifecycle PreDestroy or PostConstruct are on a method
     */
    public boolean hasLifecycle(final Class<?> type) {
        Method[] methods = type.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.isAnnotationPresent(PreDestroy.class) || method.isAnnotationPresent(PostConstruct.class)) {
                return true;
            }
        }
        return false;
    }

}