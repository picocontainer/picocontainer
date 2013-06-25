/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.injectors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;


import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.ComponentMonitorStrategy;
import com.picocontainer.InjectionType;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.parameters.MethodParameters;

/**
 * A Reinjector allows methods on pre-instantiated classes to be invoked,
 * with appropriately scoped parameters.
 */
public class Reinjector {

    private final PicoContainer parent;
    private final ComponentMonitor monitor;
    private static NullLifecycleStrategy NO_LIFECYCLE = new NullLifecycleStrategy();
    private static Properties NO_PROPERTIES = new Properties();

    /**
     * Make a reinjector with a parent container from which to pull components to be reinjected to.
     * With this constructor, a NullComponentMonitor is used.
     * @param parentContainer the parent container
     */
    public Reinjector(final PicoContainer parentContainer) {
        this(parentContainer, parentContainer instanceof ComponentMonitorStrategy
                ? ((ComponentMonitorStrategy) parentContainer).currentMonitor()
                : new NullComponentMonitor());
    }

    /**
     * Make a reinjector with a parent container from which to pull components to be reinjected to
     * @param parentContainer the parent container
     * @param monitor the monitor to use for 'instantiating' events
     */
    public Reinjector(final PicoContainer parentContainer, final ComponentMonitor monitor) {
        this.parent = parentContainer;
        this.monitor = monitor;
    }

    /**
     * Reinjecting into a method.
     * @param key the component-key from the parent set of components to inject into
     * @param reinjectionMethod the reflection method to use for injection.
     * @return the result of the reinjection-method invocation.
     */
    public Object reinject(final Class<?> key, final Method reinjectionMethod) {
        return reinject(key, key, parent.getComponentInto(Generic.get(key), ComponentAdapter.NOTHING.class), NO_PROPERTIES, new MethodInjection(reinjectionMethod));
    }

    /**
     * Reinjecting into a method.
     * @param key the component-key from the parent set of components to inject into
     * @param reinjectionMethodEnum the enum for the reflection method to use for injection.
     * @return the result of the reinjection-method invocation.
     */
    public Object reinject(final Class<?> key, final Enum reinjectionMethodEnum) {
        return reinject(key, key, parent.getComponentInto(Generic.get(key), ComponentAdapter.NOTHING.class), NO_PROPERTIES, new MethodInjection(toMethod(reinjectionMethodEnum)));
    }

    private Method toMethod(final Enum reinjectionMethodEnum) {
        Object methodOrException = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    return reinjectionMethodEnum.getClass().getMethod("toMethod").invoke(reinjectionMethodEnum);
                } catch (IllegalAccessException e) {
                    return new PicoCompositionException("Illegal access to " + reinjectionMethodEnum.name());
                } catch (InvocationTargetException e) {
                    return new PicoCompositionException("Invocation Target Exception " + reinjectionMethodEnum.name(), e.getCause());
                } catch (NoSuchMethodException e) {
                    return new PicoCompositionException("Expected generated method toMethod() on enum");
                }
            }
        });
        if (methodOrException instanceof Method) {
            return (Method) methodOrException;
        } else {
            throw (PicoCompositionException) methodOrException;
        }
    }

    /**
     * Reinjecting into a method.
     * @param key the component-key from the parent set of components to inject into (key and impl are the same)
     * @param reinjectionType the InjectionFactory to use for reinjection.
     * @return the result of the reinjection-method invocation.
     */
    public Object reinject(final Class<?> key, final InjectionType reinjectionType) {
        Object o = reinject(key, key, parent.getComponentInto(Generic.get( key), ComponentAdapter.NOTHING.class), NO_PROPERTIES, reinjectionType);
        return o;
    }

    /**
     * Reinjecting into a method.
     * @param key the component-key from the parent set of components to inject into
     * @param impl the implementation of the component that is going to result.
     * @param reinjectionType the InjectionFactory to use for reinjection.
     * @return
     */
    public Object reinject(final Class<?> key, final Class<?> impl, final InjectionType reinjectionType) {
        return reinject(key, impl, parent.getComponentInto(key, ComponentAdapter.NOTHING.class), NO_PROPERTIES, reinjectionType);
    }

    /**
     * Reinjecting into a method.
     * @param key the component-key from the parent set of components to inject into
     * @param implementation the implementation of the component that is going to result.
     * @param instance the object that has the provider method to be invoked
     * @param reinjectionType the InjectionFactory to use for reinjection.
     * @return the result of the reinjection-method invocation.
     */
    public Object reinject(final Class<?> key, final Class<?> implementation, final Object instance, final InjectionType reinjectionType) {
        return reinject(key, implementation, instance, NO_PROPERTIES, reinjectionType);
    }

    /**
     * Reinjecting into a method.
     * @param key the component-key from the parent set of components to inject into
     * @param implementation the implementation of the component that is going to result.
     * @param instance the object that has the provider method to be invoked
     * @param properties for reinjection
     * @param reinjectionType the InjectionFactory to use for reinjection.
     * @return the result of the reinjection-method invocation.
     */
    public Object reinject(final Object key, final Class<?> implementation, final Object instance, final Properties properties,
                           final InjectionType reinjectionType, final MethodParameters... methodParams) {
        Reinjection reinjection = new Reinjection(reinjectionType, parent);
        com.picocontainer.Injector injector = (com.picocontainer.Injector) reinjection.createComponentAdapter(
                monitor, NO_LIFECYCLE, properties, key, implementation, null, null,
                (methodParams != null && methodParams.length > 0) ? methodParams : null);
        return injector.decorateComponentInstance(parent, ComponentAdapter.NOTHING.class, instance);
    }

}
