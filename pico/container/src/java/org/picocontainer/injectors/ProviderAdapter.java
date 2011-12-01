/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.lifecycle.NullLifecycleStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Properties;

/**
 * Providers are a type of Injector that can participate in Injection via a custom method.
 *
 * Implementers of this class must implement a single method called provide.  That method must return
 * the component type intended to be provided.  The method can accept parameters that PicoContainer
 * will satisfy.
 */
public class ProviderAdapter implements org.picocontainer.Injector, Provider, LifecycleStrategy {

    private static Method AT_INJECT_GET = javax.inject.Provider.class.getDeclaredMethods()[0];    

    private final Object provider;
    private final Method provideMethod;
    private final Type key;
    private Properties properties;
    private LifecycleStrategy lifecycle;

    protected ProviderAdapter() {
        provider = this;
        provideMethod = getProvideMethod(this.getClass());
        key = provideMethod.getReturnType();
        setUseNames(useNames());
        this.lifecycle = new NullLifecycleStrategy();
    }

    public ProviderAdapter(LifecycleStrategy lifecycle, Provider provider) {
        this(lifecycle, (Object) provider, false);
    }

    public ProviderAdapter(Provider provider) {
        this(new NullLifecycleStrategy(), (Object) provider, false);
    }

    public ProviderAdapter(javax.inject.Provider provider) {
        this(new NullLifecycleStrategy(), (Object) provider, false);
    }

    public ProviderAdapter(Provider provider, boolean useNames) {
        this(new NullLifecycleStrategy(), (Object) provider, useNames);
    }

    public ProviderAdapter(LifecycleStrategy lifecycle, Provider provider, boolean useNames) {
        this(lifecycle, (Object) provider, useNames);
    }

    private ProviderAdapter(LifecycleStrategy lifecycle, Object provider, boolean useNames) {
        this.lifecycle = lifecycle;
        this.provider = provider;
        provideMethod = getProvideMethod(provider.getClass());
        if (provideMethod == AT_INJECT_GET) {
            key = provider.getClass().getGenericInterfaces()[0];

        } else {
            key = provideMethod.getReturnType();
        }
        setUseNames(useNames);
    }

    private void setUseNames(boolean useNames) {
        if (useNames) {
            properties = Characteristics.USE_NAMES;
        } else {
            properties = Characteristics.NONE;
        }
    }

    protected boolean useNames() {
        return false;
    }

    public Object decorateComponentInstance(PicoContainer container, Type into, Object instance) {
        return null;
    }

    public Object getComponentKey() {
        return key;
    }

    public Class getComponentImplementation() {
        if (provider instanceof javax.inject.Provider) {
            return provider.getClass();
        } else {
            return (Class) key;
        }
    }

    public Object getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
        if (provideMethod == AT_INJECT_GET) {
            return provider;
        } else {
            return new Reinjector(container).reinject(key, provider.getClass(), provider, properties, new MethodInjection(provideMethod));
        }
    }

    public static Method getProvideMethod(Class clazz) {
        Method provideMethod = null;
        if (javax.inject.Provider.class.isAssignableFrom(clazz)) {
            return AT_INJECT_GET;
        }
        // TODO doPrivileged
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals("provide")) {
                if (provideMethod != null) {
                    throw newProviderMethodException("only one");
                }
                provideMethod = method;
            }
        }
        if (provideMethod == null) {
            throw newProviderMethodException("a");
        }
        if (provideMethod.getReturnType() == void.class) {
            throw newProviderMethodException("a non void returning");
        }
        return provideMethod;
    }

    private static PicoCompositionException newProviderMethodException(String str) {
        return new PicoCompositionException("There must be "+ str +" method named 'provide' in the AbstractProvider implementation");
    }

    public void verify(PicoContainer container) throws PicoCompositionException {
    }

    public void accept(PicoVisitor visitor) {
    }

    public ComponentAdapter getDelegate() {
        return null;
    }

    public ComponentAdapter findAdapterOfType(Class adapterType) {
        return null;
    }

    public String getDescriptor() {
        return "ProviderAdapter";
    }

    public void start(Object component) {
        lifecycle.start(component);
    }

    public void stop(Object component) {
        lifecycle.stop(component);
    }

    public void dispose(Object component) {
        lifecycle.dispose(component);
    }

    public boolean hasLifecycle(Class<?> type) {
        return lifecycle.hasLifecycle(type);
    }

    public boolean isLazy(ComponentAdapter<?> adapter) {
        return lifecycle.isLazy(adapter);
    }
}
