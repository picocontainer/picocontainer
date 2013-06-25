/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.behaviors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Properties;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

/**
 * @author Aslak Helles&oslash;y
 * @see com.picocontainer.gems.adapters.HotSwappingComponentFactory for a more feature-rich version of the class
 */
@SuppressWarnings("serial")
public class ImplementationHiding extends AbstractBehavior {

    @Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps,
                                    final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {

        removePropertiesIfPresent(componentProps, Characteristics.ENABLE_CIRCULAR);

        ComponentAdapter<T> componentAdapter = super.createComponentAdapter(monitor, lifecycle,
                                                                         componentProps, key, impl, constructorParams, fieldParams, methodParams);
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_HIDE_IMPL)) {
            return componentAdapter;
        }
        removePropertiesIfPresent(componentProps, Characteristics.HIDE_IMPL);
        return monitor.changedBehavior(new HiddenImplementation<T>(componentAdapter));

    }

    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                                                final Properties componentProps, final ComponentAdapter<T> adapter) {
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_HIDE_IMPL)) {
            return adapter;
        }
        removePropertiesIfPresent(componentProps, Characteristics.HIDE_IMPL);
        return monitor.changedBehavior(new HiddenImplementation<T>(super.addComponentAdapter(monitor,
                                                                          lifecycle,
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
     * @see com.picocontainer.gems.adapters.HotSwappingComponentAdapter for a more feature-rich version of this class.
     */
    @SuppressWarnings("serial")
    public static class HiddenImplementation<T> extends AbstractChangedBehavior<T> {

        /**
         * Creates an ImplementationHidingComponentAdapter with a delegate
         * @param delegate the component adapter to which this adapter delegates
         */
        public HiddenImplementation(final ComponentAdapter<T> delegate) {
            super(delegate);
        }

        @Override
		public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {

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
        protected T createProxy(final Class[] interfaces, final PicoContainer container, final ClassLoader classLoader) {
            final PicoContainer container1 = container;
            return (T) Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
                private final PicoContainer container = container1;
                private volatile Object instance;

                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    if (instance == null) {
                        synchronized (HiddenImplementation.this) {
                            if (instance == null) {
                                instance = getDelegate().getComponentInstance(container, NOTHING.class);
                            }
                        }
                    }
                    return invokeMethod(instance, method, args, container);
                }
            });        }

        protected Object invokeMethod(final Object componentInstance, final Method method, final Object[] args, final PicoContainer container) throws Throwable {
            ComponentMonitor monitor = currentMonitor();
            try {
                monitor.invoking(container, this, method, componentInstance, args);
                long startTime = System.currentTimeMillis();
                Object rv = method.invoke(componentInstance, args);
                monitor.invoked(container, this,
                                         method, componentInstance, System.currentTimeMillis() - startTime, rv, args);
                return rv;
            } catch (final InvocationTargetException ite) {
                monitor.invocationFailed(method, componentInstance, ite);
                throw ite.getTargetException();
            }
        }

        private void verifyInterfacesOnly(final Class<?>[] classes) {
            for (Class<?> clazz : classes) {
                if (!clazz.isInterface()) {
                    throw new PicoCompositionException(
                        "Class keys must be interfaces. " + clazz + " is not an interface.");
                }
            }
        }

    }
}
