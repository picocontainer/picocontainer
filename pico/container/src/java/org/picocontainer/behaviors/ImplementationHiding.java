/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Characteristics;
import org.picocontainer.PicoContainer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Properties;

/**
 * @author Aslak Helles&oslash;y
 * @see org.picocontainer.gems.adapters.HotSwappingComponentFactory for a more feature-rich version of the class
 */
@SuppressWarnings("serial")
public class ImplementationHiding extends AbstractBehavior {

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy, Properties componentProps, Object key, Class<T> impl, Parameter... parameters) throws PicoCompositionException {

        removePropertiesIfPresent(componentProps, Characteristics.ENABLE_CIRCULAR);

        ComponentAdapter<T> componentAdapter = super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                                                                         componentProps, key, impl, parameters);
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_HIDE_IMPL)) {
            return componentAdapter;
        }
        removePropertiesIfPresent(componentProps, Characteristics.HIDE_IMPL);
        return componentMonitor.newBehavior(new HiddenImplementation<T>(componentAdapter));

    }

    public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor componentMonitor,
                                                LifecycleStrategy lifecycleStrategy,
                                                Properties componentProps,
                                                ComponentAdapter<T> adapter) {
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_HIDE_IMPL)) {
            return adapter;
        }
        removePropertiesIfPresent(componentProps, Characteristics.HIDE_IMPL);
        return componentMonitor.newBehavior(new HiddenImplementation<T>(super.addComponentAdapter(componentMonitor,
                                                                          lifecycleStrategy,
                                                                          componentProps,
                                                                          adapter)));

    }

    /**
     * This component adapter makes it possible to hide the implementation
     * of a real subject (behind a proxy) provided the key is an interface.
     * <p/>
     * This class exists here, because a) it has no deps on external jars, b) dynamic proxy is quite easy.
     * The user is prompted to look at picocontainer-gems for alternate and bigger implementations.
     *
     * @author Aslak Helles&oslash;y
     * @author Paul Hammant
     * @see org.picocontainer.gems.adapters.HotSwappingComponentAdapter for a more feature-rich version of this class.
     */
    @SuppressWarnings("serial")
    public static class HiddenImplementation<T> extends AbstractChangedBehavior<T> {

        /**
         * Creates an ImplementationHidingComponentAdapter with a delegate
         * @param delegate the component adapter to which this adapter delegates
         */
        public HiddenImplementation(ComponentAdapter<T> delegate) {
            super(delegate);
        }

        public T getComponentInstance(final PicoContainer container, Type into) throws PicoCompositionException {

            ComponentAdapter<T> delegate = getDelegate();
            Object key = delegate.getComponentKey();
            Class<?>[] classes;
            if (key instanceof Class && ((Class<?>) delegate.getComponentKey()).isInterface()) {
                classes = new Class[]{(Class<?>) delegate.getComponentKey()};
            } else if (key instanceof Class[]) {
                classes = (Class[]) key;
            } else {
                return delegate.getComponentInstance(container, into);
            }

            verifyInterfacesOnly(classes);
            return createProxy(classes, container, delegate.getComponentImplementation().getClassLoader());
        }

        public String getDescriptor() {
            return "Hidden";
        }


        @SuppressWarnings("unchecked")
        protected T createProxy(Class[] interfaces, final PicoContainer container, final ClassLoader classLoader) {
            return (T) Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
                public synchronized Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    return invokeMethod(getDelegate().getComponentInstance(container, NOTHING.class), method, args, container);
                }
            });
        }

        protected Object invokeMethod(Object componentInstance, Method method, Object[] args, PicoContainer container) throws Throwable {
            ComponentMonitor componentMonitor = currentMonitor();
            try {
                componentMonitor.invoking(container, this, method, componentInstance, args);
                long startTime = System.currentTimeMillis();
                Object rv = method.invoke(componentInstance, args);
                componentMonitor.invoked(container, this,
                                         method, componentInstance, System.currentTimeMillis() - startTime, args, rv);
                return rv;
            } catch (final InvocationTargetException ite) {
                componentMonitor.invocationFailed(method, componentInstance, ite);
                throw ite.getTargetException();
            }
        }

        private void verifyInterfacesOnly(Class<?>[] classes) {
            for (Class<?> clazz : classes) {
                if (!clazz.isInterface()) {
                    throw new PicoCompositionException(
                        "Class keys must be interfaces. " + clazz + " is not an interface.");
                }
            }
        }

    }
}
