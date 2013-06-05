/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Properties;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.lifecycle.NullLifecycleStrategy;

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
    private final Object key;
    private final Type providerReturnType;
    private Properties properties;
    private LifecycleStrategy lifecycle;

    protected ProviderAdapter() {
        provider = this;
        provideMethod = getProvideMethod(this.getClass());
        key = provideMethod.getReturnType();
        providerReturnType = provideMethod.getReturnType();
        setUseNames(useNames());
        this.lifecycle = new NullLifecycleStrategy();
    }
    
    public ProviderAdapter(Provider theProvider) {
        this(null, theProvider);
    }

    public ProviderAdapter(javax.inject.Provider<?> theProvider) {
    	this(null, theProvider);
    }
    
    
    public ProviderAdapter(LifecycleStrategy lifecycle, Provider provider) {
    	this(null, lifecycle, provider);
    }
    
    public ProviderAdapter(Object key, LifecycleStrategy lifecycle, Provider provider) {
        this(lifecycle, key, (Object) provider, false);
    }

    public ProviderAdapter(Object key, javax.inject.Provider<?>  provider) {
        this(new NullLifecycleStrategy(), key, (Object) provider, false);
    }


    public ProviderAdapter(javax.inject.Provider<?>  provider, boolean useNames) {
    	this(null, provider, useNames);
    }
    
    public ProviderAdapter(Object key, javax.inject.Provider<?>  provider, boolean useNames) {
        this(new NullLifecycleStrategy(), key, (Object) provider, useNames);
    }

    
    public ProviderAdapter(Object key, LifecycleStrategy lifecycle, javax.inject.Provider<?>  provider, boolean useNames) {
        this(lifecycle, key, provider, useNames);
    }
    
    public ProviderAdapter(LifecycleStrategy lifecycle, javax.inject.Provider<?>  provider, boolean useNames) {
    	this((Object)null, lifecycle, provider, useNames);
    }

    private ProviderAdapter(LifecycleStrategy lifecycle, Object providerKey, Object provider, boolean useNames) {
        this.lifecycle = lifecycle;
        this.provider = provider;
        provideMethod = getProvideMethod(provider.getClass());
        this.providerReturnType = determineProviderReturnType(provider);
        if (providerKey == null) {
        	key = determineProviderReturnType(provider);
        } else {
        	key = providerKey;
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
    
    
    /**
     * @throws ClassCastException if the provider isn't a javax.inject.Provider (the only time this method
     * is called in the codebase.)
     * @return
     */
	public javax.inject.Provider<?> getProvider() {
    	return (javax.inject.Provider<?>) provider;
    }
    
    /**
     * The return type that the provider creates.
     * @return
     */
    public Class<?> getProviderReturnType() {
    	if (providerReturnType instanceof Class<?>) {
    		return (Class<?>)providerReturnType;
    	}
    	
    	throw new PicoCompositionException("Unexpected condition, Provider Return type was not a class type, instead it was a : " + providerReturnType);
    	
    }
    
    public Class<?> getProviderImplementation() {
    	return provider.getClass();
    }

    public Object getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
        if (provideMethod == AT_INJECT_GET) {
        	try {
				return provideMethod.invoke(provider);
			} catch (Exception e) {
				throw new PicoCompositionException("Error invoking provider " + provider + " to inject into " + into, e);
			}
//            return provider;
        } else {
            return new Reinjector(container).reinject(key, provider.getClass(), provider, properties, new MethodInjection(provideMethod));
        }
    }
    
    public static Type determineProviderReturnType(Object provider) {
        Method provideMethod = getProvideMethod(provider.getClass());
        Type key;
        if (provideMethod == AT_INJECT_GET) {
        	Type paramType = provider.getClass().getGenericInterfaces()[0];
        	if (paramType instanceof Class<?>) {
        		key = paramType.getClass();
        	} else {
        		key = ((ParameterizedType)paramType).getActualTypeArguments()[0];	
        	}
        	
        } else {
            key = provideMethod.getReturnType();
        }
       
        return key;
    }

    public static Method getProvideMethod(Class<?> clazz) {
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
    	visitor.visitComponentAdapter(this);
    }

    /**
     * Last one in the chain, no delegate.
     * @return null always.
     */
    public ComponentAdapter<?> getDelegate() {
        return null;
    }


    public String getDescriptor() {
        return "ProviderAdapter";
    }

    
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		result.append(getClass().getName());
		result.append("@");
		result.append(System.identityHashCode(this));
		result.append(" (key = ");
		result.append(getComponentKey());
		result.append(" ; implementation = ");
		result.append(getProviderImplementation());
		result.append(" ; provided type = ");
		result.append(providerReturnType);
		result.append(" )");
		
		return result.toString();
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

    /**
     * Providers don't decorate component instances.
     */
	public Object decorateComponentInstance(PicoContainer container, Type into, Object instance) {
		return null;
	}

    /**
     * Providers don't decorate component instances.
     */
	@SuppressWarnings("rawtypes")
	public Object partiallyDecorateComponentInstance(PicoContainer container, Type into, Object instance, Class superclassPortion) {
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public ComponentAdapter findAdapterOfType(Class adapterType) {
		if (getClass().isAssignableFrom(adapterType)) {
			return this;
		}
		
		return null;
	}


}
