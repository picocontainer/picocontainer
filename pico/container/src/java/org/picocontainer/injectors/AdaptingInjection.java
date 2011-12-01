/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.injectors;

import java.lang.reflect.AccessibleObject;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.annotations.Inject;
import org.picocontainer.behaviors.AbstractBehavior;

import static org.picocontainer.injectors.AnnotatedMethodInjection.getInjectionAnnotation;

/**
 * Creates injector instances, depending on the injection characteristics of the component class. 
 * It will attempt to create a component adapter with - in order of priority:
 * <ol>
 *  <li>Annotated field injection: if annotation {@link org.picocontainer.annotations.Inject} is found for field</li>
 *  <li>Annotated method injection: if annotation {@link org.picocontainer.annotations.Inject} is found for method</li>
 *  <li>Setter injection: if {@link Characteristics.SDI} is found</li>
 *  <li>Method injection: if {@link Characteristics.METHOD_INJECTION} if found</li>
 *  <li>Constructor injection (the default, must find {@link Characteristics.CDI})</li>
 * </ol>
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 * @see AnnotatedFieldInjection
 * @see AnnotatedMethodInjection
 * @see SetterInjection
 * @see MethodInjection
 * @see ConstructorInjection
 */
@SuppressWarnings("serial")
public class AdaptingInjection extends AbstractInjectionType {

	public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle,
                                                   Properties componentProps, Object key, Class<T> impl,
                                                   Parameter... parameters) throws PicoCompositionException {
        ComponentAdapter<T> componentAdapter = null;

        componentAdapter = fieldAnnotatedInjectionAdapter(impl, monitor, lifecycle, componentProps,
                               key, componentAdapter, parameters);

        if (componentAdapter != null) {
            return componentAdapter;
        }

        componentAdapter = methodAnnotatedInjectionAdapter(impl, monitor, lifecycle, componentProps,
                                                           key, componentAdapter, parameters);

        if (componentAdapter != null) {
            return componentAdapter;
        }

        componentAdapter = setterInjectionAdapter(componentProps, monitor, lifecycle, key, impl, componentAdapter, parameters);

        if (componentAdapter != null) {
            return componentAdapter;
        }

        componentAdapter = methodInjectionAdapter(componentProps, monitor, lifecycle, key, impl, componentAdapter, parameters);

        if (componentAdapter != null) {
            return componentAdapter;
        }


        return defaultInjectionAdapter(componentProps, monitor, lifecycle, key, impl, parameters);
    }

    private <T> ComponentAdapter<T> defaultInjectionAdapter(Properties componentProps, ComponentMonitor monitor,
                                                  LifecycleStrategy lifecycle, Object key, Class<T> impl, Parameter... parameters) {
        AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.CDI);
        return new ConstructorInjection().createComponentAdapter(monitor, lifecycle, componentProps, key, impl, parameters);
    }

    private <T> ComponentAdapter<T> setterInjectionAdapter(Properties componentProps, ComponentMonitor monitor, LifecycleStrategy lifecycle,
                                                   Object key, Class<T> impl, ComponentAdapter<T> componentAdapter, Parameter... parameters) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.SDI)) {
            componentAdapter = new SetterInjection().createComponentAdapter(monitor, lifecycle, componentProps, key, impl, parameters);
        }
        return componentAdapter;
    }

    private <T> ComponentAdapter<T> methodInjectionAdapter(Properties componentProps, ComponentMonitor monitor, LifecycleStrategy lifecycle,
                                                   Object key, Class<T> impl, ComponentAdapter<T> componentAdapter, Parameter... parameters) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.METHOD_INJECTION)) {
            componentAdapter = new MethodInjection().createComponentAdapter(monitor, lifecycle, componentProps, key, impl, parameters);
        }
        return componentAdapter;
    }


    private <T> ComponentAdapter<T> methodAnnotatedInjectionAdapter(Class<T> impl, ComponentMonitor monitor, LifecycleStrategy lifecycle,
                                       Properties componentProps, Object key, ComponentAdapter<T> componentAdapter, Parameter... parameters) {
        if (injectionMethodAnnotated(impl)) {
            componentAdapter =
                new AnnotatedMethodInjection().createComponentAdapter(monitor, lifecycle, componentProps, key, impl, parameters);
        }
        return componentAdapter;
    }

    private <T> ComponentAdapter<T> fieldAnnotatedInjectionAdapter(Class<T> impl, ComponentMonitor monitor, LifecycleStrategy lifecycle,
                                 Properties componentProps, Object key, ComponentAdapter<T> componentAdapter, Parameter... parameters) {
        if (injectionFieldAnnotated(impl)) {
             componentAdapter = new AnnotatedFieldInjection().createComponentAdapter(monitor, lifecycle, componentProps, key, impl, parameters);
        }
        return componentAdapter;
    }

    private boolean injectionMethodAnnotated(final Class<?> impl) {
        return (Boolean) AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @SuppressWarnings("synthetic-access")
            public Object run() {
                return injectionAnnotated(impl.getDeclaredMethods());
            }
        });
    }

    private boolean injectionFieldAnnotated(final Class<?> impl) {
        return (Boolean) AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @SuppressWarnings("synthetic-access")
            public Object run() {
                if (impl.isInterface()) {
                    return false;
                }
                Class impl2 = impl;
                while (impl2 != Object.class) {
                    if (injectionAnnotated(impl2.getDeclaredFields())) {
                        return true;
                    }
                    impl2 = impl2.getSuperclass();
                }
                return false;
            }
        });
    }
    
    private boolean injectionAnnotated(AccessibleObject[] objects) {
        for (AccessibleObject object : objects) {
            if (object.getAnnotation(Inject.class) != null
                    || object.getAnnotation(getInjectionAnnotation("javax.inject.Inject")) != null) {
                return true;
            }
        }
        return false;
    }

}
