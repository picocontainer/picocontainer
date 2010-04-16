/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
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


	public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor,
                                                   LifecycleStrategy lifecycleStrategy,
                                                   Properties componentProperties,
                                                   Object componentKey,
                                                   Class<T> componentImplementation,
                                                   Parameter... parameters) throws PicoCompositionException {
        ComponentAdapter<T> componentAdapter = null;
        
        componentAdapter = fieldAnnotatedInjectionAdapter(componentImplementation,
                               componentMonitor,
                               lifecycleStrategy,
                               componentProperties,
                               componentKey,
                               componentAdapter,
                               parameters);

        if (componentAdapter != null) {
            return componentAdapter;
        }


        componentAdapter = methodAnnotatedInjectionAdapter(componentImplementation,
                                                           componentMonitor,
                                                           lifecycleStrategy,
                                                           componentProperties,
                                                           componentKey,
                                                           componentAdapter,
                                                           parameters);

        if (componentAdapter != null) {
            return componentAdapter;
        }

        componentAdapter = setterInjectionAdapter(componentProperties,
                                                 componentMonitor,
                                                 lifecycleStrategy,
                                                 componentKey,
                                                 componentImplementation,
                                                 componentAdapter,
                                                 parameters);

        if (componentAdapter != null) {
            return componentAdapter;
        }

        componentAdapter = methodInjectionAdapter(componentProperties,
                                                 componentMonitor,
                                                 lifecycleStrategy,
                                                 componentKey,
                                                 componentImplementation,
                                                 componentAdapter,
                                                 parameters);

        if (componentAdapter != null) {
            return componentAdapter;
        }


        return defaultInjectionAdapter(componentProperties,
                                    componentMonitor,
                                    lifecycleStrategy,
                                    componentKey,
                                    componentImplementation,
                                    parameters);
    }

    private <T> ComponentAdapter<T> defaultInjectionAdapter(Properties componentProperties,
                                                  ComponentMonitor componentMonitor,
                                                  LifecycleStrategy lifecycleStrategy,
                                                  Object componentKey,
                                                  Class<T> componentImplementation, Parameter... parameters) {
        AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.CDI);
        return new ConstructorInjection().createComponentAdapter(componentMonitor,
                                                                        lifecycleStrategy,
                                                                        componentProperties,
                                                                        componentKey,
                                                                        componentImplementation,
                                                                        parameters);
    }

    private <T> ComponentAdapter<T> setterInjectionAdapter(Properties componentProperties,
                                                   ComponentMonitor componentMonitor,
                                                   LifecycleStrategy lifecycleStrategy,
                                                   Object componentKey,
                                                   Class<T> componentImplementation,
                                                   ComponentAdapter<T> componentAdapter,
                                                   Parameter... parameters) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.SDI)) {
            componentAdapter = new SetterInjection().createComponentAdapter(componentMonitor, lifecycleStrategy,
                                                                                                    componentProperties,
                                                                                                    componentKey,
                                                                                                    componentImplementation,
                                                                                                    parameters);
        }
        return componentAdapter;
    }

    private <T> ComponentAdapter<T> methodInjectionAdapter(Properties componentProperties,
                                                   ComponentMonitor componentMonitor,
                                                   LifecycleStrategy lifecycleStrategy,
                                                   Object componentKey,
                                                   Class<T> componentImplementation,
                                                   ComponentAdapter<T> componentAdapter,
                                                   Parameter... parameters) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.METHOD_INJECTION)) {
            componentAdapter = new MethodInjection().createComponentAdapter(componentMonitor, lifecycleStrategy,
                                                                                                    componentProperties,
                                                                                                    componentKey,
                                                                                                    componentImplementation,
                                                                                                    parameters);
        }
        return componentAdapter;
    }


    private <T> ComponentAdapter<T> methodAnnotatedInjectionAdapter(Class<T> componentImplementation,
                                                             ComponentMonitor componentMonitor,
                                                             LifecycleStrategy lifecycleStrategy,
                                                             Properties componentProperties,
                                                             Object componentKey,
                                                             ComponentAdapter<T> componentAdapter,
                                                             Parameter... parameters) {
        if (injectionMethodAnnotated(componentImplementation)) {
            componentAdapter =
                new AnnotatedMethodInjection().createComponentAdapter(componentMonitor,
                                                                              lifecycleStrategy,
                                                                              componentProperties,
                                                                              componentKey,
                                                                              componentImplementation,
                                                                              parameters);
        }
        return componentAdapter;
    }

    private <T> ComponentAdapter<T> fieldAnnotatedInjectionAdapter(Class<T> componentImplementation,
                                 ComponentMonitor componentMonitor,
                                 LifecycleStrategy lifecycleStrategy,
                                 Properties componentProperties,
                                 Object componentKey, ComponentAdapter<T> componentAdapter, Parameter... parameters) {
        if (injectionFieldAnnotated(componentImplementation)) {
             componentAdapter = new AnnotatedFieldInjection().createComponentAdapter(componentMonitor,
                                                                             lifecycleStrategy,
                                                                             componentProperties,
                                                                             componentKey,
                                                                             componentImplementation,
                                                                             parameters);
        }
        return componentAdapter;
    }

    private boolean injectionMethodAnnotated(final Class<?> componentImplementation) {
        return (Boolean) AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @SuppressWarnings("synthetic-access")
            public Object run() {
                return injectionAnnotated(componentImplementation.getDeclaredMethods());
            }
        });
    }

    private boolean injectionFieldAnnotated(final Class<?> componentImplementation) {
        return (Boolean) AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @SuppressWarnings("synthetic-access")
            public Object run() {
                if (componentImplementation.isInterface()) {
                    return false;
                }
                Class impl = componentImplementation;
                while (impl != Object.class) {
                    boolean injAnnotated = injectionAnnotated(impl.getDeclaredFields());
                    if (injAnnotated) {
                        return true;
                    }
                    impl = impl.getSuperclass();
                }
                return false;
            }
        });
    }
    
    private boolean injectionAnnotated(AccessibleObject[] objects) {
        for (AccessibleObject object : objects) {
            if (object.getAnnotation(Inject.class) != null) {
                return true;
            }
        }
        return false;
    }

}
