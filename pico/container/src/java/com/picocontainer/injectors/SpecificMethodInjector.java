/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.injectors;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.parameters.JSR330ComponentParameter;
import com.picocontainer.parameters.MethodParameters;

/**
 * Injection will happen through a specific single reflection method for the component.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class SpecificMethodInjector<T> extends MethodInjection.MethodInjector<T> implements StaticInjector<T> {
    private final List<Method> injectionMethods;
	private boolean isStaticInjection;
    private transient ThreadLocalCyclicDependencyGuard<Object> instantiationGuard;
	private StaticsInitializedReferenceSet initializedReferenceSet;


    /**
     * Simple testable constructor
     * @param key
     * @param impl
     */
    public SpecificMethodInjector(final Object key, final Class<T> impl, final Method... injectionMethods) {
    	this(key, impl, new NullComponentMonitor(), true, true, null, injectionMethods);
    }

    /**
     * Typical constructor used in deployments
     * @param key
     * @param impl
     * @param monitor
     * @param useNames
     * @param useAllParameters
     * @param parameters
     * @param injectionMethods
     * @throws NotConcreteRegistrationException
     */
    public SpecificMethodInjector(final Object key, final Class<T> impl, final ComponentMonitor monitor, final boolean useNames, final boolean useAllParameters, final MethodParameters[] parameters, final Method... injectionMethods) throws NotConcreteRegistrationException {
        super(key, impl, monitor, null, useNames, useAllParameters, parameters);


        this.injectionMethods = Arrays.asList(injectionMethods);
		this.isStaticInjection = isStaticInjection(injectionMethods);

    }

    @Override
    protected List<Method> getInjectorMethods() {
        return injectionMethods;
    }

    @Override
    public String getDescriptor() {
        StringBuilder mthds = new StringBuilder();
        for (Method method : injectionMethods) {
            mthds.append(",").append(method.getDeclaringClass().getName()).append(".").append(method.getName());
        }
        return "SpecificReflectionMethodInjector" + (isStaticInjection ? "_static_" : "") + "[" +  mthds.substring(1) + "]-";
    }



	public void injectStatics(final PicoContainer container, final Type into, final StaticsInitializedReferenceSet initializedReferenceSet) {
		this.initializedReferenceSet = initializedReferenceSet;
		if (!isStaticInjection) {
			throw new PicoCompositionException(Arrays.deepToString(injectionMethods.toArray()) + " are non static fields, injectStatics should not be called.");
		}

    	boolean i_Instantiated = false;
    	try {
            if (instantiationGuard == null) {
            	i_Instantiated = true;
                instantiationGuard = new ThreadLocalCyclicDependencyGuard<Object>() {
                    @Override
                    @SuppressWarnings("synthetic-access")
                    public Object run(final Object instance) {
                        List<Method> methods = getInjectorMethods();
                        Object[] methodParameters = null;
                        for (Method method : methods) {
                            methodParameters = getMemberArguments(guardedContainer, method, into);
                            invokeMethod(method, methodParameters, null, container);
                        }
                        return null;
                    }
                };
            }
            instantiationGuard.setGuardedContainer(container);
            instantiationGuard.observe(getComponentImplementation(), null);
    	} finally {
            if (i_Instantiated) {
            	instantiationGuard.remove();
            	instantiationGuard = null;
            }
    	}

	}

	@Override
	public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
		if (isStaticInjection) {
			throw new PicoCompositionException(Arrays.deepToString(injectionMethods.toArray()) + " are static methods, getComponentInstance() on this adapter should not be called.");
		}

		return super.getComponentInstance(container, into);
	}

	@Override
	Object invokeMethod(final Method method, final Object[] methodParameters, final T instance, final PicoContainer container) {
		AnnotationInjectionUtils.setMemberAccessible(method);
        if (initializedReferenceSet != null) {
        	//Static injection = threading issues
        	//have to lock at the method's class level
            synchronized(method.getDeclaringClass()) {
        		if (!this.initializedReferenceSet.isMemberAlreadyInitialized(method)) {
        			Object result = super.invokeMethod(method, methodParameters, instance, container);
        			initializedReferenceSet.markMemberInitialized(method);
        			return result;
        		}
        	}
            //Already initialized statics -- skipping.
            return null;
        }  else {
            return super.invokeMethod(method, methodParameters, instance, container);
        }

	}


	/**
	 * Allows for annotation-based key swapping.
	 */
	@Override
	protected Parameter[] interceptParametersToUse(final Parameter[] currentParameters, final AccessibleObject member) {
		return AnnotationInjectionUtils.interceptParametersToUse(currentParameters, member);
	}


    /**
     * Allows Different swapping of types.
     * @return
     */
    @Override
    protected Parameter constructDefaultComponentParameter() {
    	return JSR330ComponentParameter.DEFAULT;
    }

}
