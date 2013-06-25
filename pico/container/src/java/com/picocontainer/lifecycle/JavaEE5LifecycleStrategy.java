/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package com.picocontainer.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.PicoLifecycleException;
import com.picocontainer.injectors.AnnotationInjectionUtils;

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
        doLifecycleMethod(component, PostConstruct.class, true);
    }

	/** {@inheritDoc} **/
    public void stop(final Object component) {
    }

    /** {@inheritDoc} **/
    public void dispose(final Object component) {
        doLifecycleMethod(component, PreDestroy.class, false);
    }

    private void doLifecycleMethod(final Object component, final Class<? extends Annotation> annotation, final boolean superFirst) {
    	doLifecycleMethod(component, annotation, component.getClass(), superFirst, new HashSet<String>());
    }

    private void doLifecycleMethod(final Object component, final Class<? extends Annotation> annotation, final Class<? extends Object> clazz, final boolean superFirst, final Set<String> doneAlready) {
        Class<?> parent = clazz.getSuperclass();
        if (superFirst && parent != Object.class) {
            doLifecycleMethod(component, annotation, parent, superFirst, doneAlready);
        }

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            String signature = signature(method);

            if (method.isAnnotationPresent(annotation) && !doneAlready.contains(signature)) {
                try {
                    long str = System.currentTimeMillis();
                    currentMonitor().invoking(null, null, method, component, new Object[0]);
                    AnnotationInjectionUtils.setMemberAccessible(method);
                    method.invoke(component);
                    doneAlready.add(signature);
                    currentMonitor().invoked(null, null, method, component, System.currentTimeMillis() - str, null, new Object[0]);
                } catch (IllegalAccessException e) {
                    throw new PicoLifecycleException(method, component, e);
                } catch (InvocationTargetException e) {
                    throw new PicoLifecycleException(method, component, e);
                }
            }
        }

        if (!superFirst && parent != Object.class) {
            doLifecycleMethod(component, annotation, parent, superFirst, doneAlready);
        }
    }

    private static String signature(final Method method) {
        StringBuilder sb = new StringBuilder(method.getName());
        Class<?>[] pt = method.getParameterTypes();
        for (Class<?> objectClass : pt) {
            sb.append(objectClass.getName());
        }
        return sb.toString();
    }
    /**
     * {@inheritDoc} The component has a lifecycle PreDestroy or PostConstruct are on a method
     */
    public boolean hasLifecycle(final Class<?> type) {
        Method[] methods = type.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(PreDestroy.class) || method.isAnnotationPresent(PostConstruct.class)) {
                return true;
            }
        }
        return false;
    }

}