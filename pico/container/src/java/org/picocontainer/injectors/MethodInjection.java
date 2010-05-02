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

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.Characteristics;
import org.picocontainer.behaviors.AbstractBehavior;

import java.util.Properties;
import java.lang.reflect.Method;

/**
 * A {@link org.picocontainer.InjectionType} for methods.
 * The factory creates {@link MethodInjector}.
 * 
 *  @author Paul Hammant 
 */
@SuppressWarnings("serial")
public class MethodInjection extends AbstractInjectionType {

    private final AbstractInjectionType delegate;

    public MethodInjection(String injectionMethodName) {
        delegate = new MethodInjectionByName(injectionMethodName);
    }

    public MethodInjection() {
        this("inject");
    }

    public MethodInjection(Method injectionMethod) {
        delegate = new MethodInjectionByReflectionMethod(injectionMethod);
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy, Properties componentProperties, Object key,
                                                   Class<T> impl, Parameter... parameters) throws PicoCompositionException {
        return delegate.createComponentAdapter(componentMonitor, lifecycleStrategy, componentProperties, key, impl, parameters);
    }

    public class MethodInjectionByName extends AbstractInjectionType {
        private final String injectionMethodName;

        public MethodInjectionByName(String injectionMethodName) {
            this.injectionMethodName = injectionMethodName;
        }

        public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycleStrategy, Properties componentProperties, Object key, Class<T> impl, Parameter... parameters) throws PicoCompositionException {
            boolean useNames = AbstractBehavior.arePropertiesPresent(componentProperties, Characteristics.USE_NAMES, true);
            return wrapLifeCycle(new MethodInjector(key, impl, parameters, monitor, injectionMethodName, useNames), lifecycleStrategy);
        }
    }

    public class MethodInjectionByReflectionMethod extends AbstractInjectionType {
        private final Method injectionMethod;

        public MethodInjectionByReflectionMethod(Method injectionMethod) {
            this.injectionMethod = injectionMethod;
        }

        public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycleStrategy, Properties componentProperties, Object key, Class<T> impl, Parameter... parameters) throws PicoCompositionException {
            boolean useNames = AbstractBehavior.arePropertiesPresent(componentProperties, Characteristics.USE_NAMES, true);
            if (injectionMethod.getDeclaringClass().isAssignableFrom(impl)) {
                return wrapLifeCycle(monitor.newInjector(new SpecificReflectionMethodInjector(key, impl, parameters, monitor, injectionMethod, useNames)), lifecycleStrategy);
            } else {
                throw new PicoCompositionException("method [" + injectionMethod + "] not on impl " + impl.getName());
            }
        }
    }

}
