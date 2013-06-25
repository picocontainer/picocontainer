/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/
package com.picocontainer.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.annotations.Nullable;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.parameters.AccessibleObjectParameterSet;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

/**
 * A {@link com.picocontainer.InjectionType} for methods.
 * The factory creates {@link MethodInjector}.
 *
 *  @author Paul Hammant
 */
@SuppressWarnings("serial")
public class MethodInjection extends AbstractInjectionType {

    private final AbstractInjectionType delegate;

    public MethodInjection(final String injectionMethodName) {
        delegate = new MethodInjectionByName(injectionMethodName);
    }

    public MethodInjection() {
        this("inject");
    }

    public MethodInjection(final Method injectionMethod) {
        delegate = new MethodInjectionByReflectionMethod(injectionMethod);
    }

    public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps, final Object key,
                                                   final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        return delegate.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
    }

    public class MethodInjectionByName extends AbstractInjectionType {
        private final String injectionMethodName;

        public MethodInjectionByName(final String injectionMethodName) {
            this.injectionMethodName = injectionMethodName;
        }

        public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
            boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
            boolean requireConsumptionOfAllParameters = !(AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.ALLOW_UNUSED_PARAMETERS, false));
            return wrapLifeCycle(new MethodInjector(key, impl, monitor, injectionMethodName, useNames, requireConsumptionOfAllParameters, methodParams), lifecycle);
        }
    }

    public class MethodInjectionByReflectionMethod extends AbstractInjectionType {
        private final Method injectionMethod;

        public MethodInjectionByReflectionMethod(final Method injectionMethod) {
            this.injectionMethod = injectionMethod;
        }

        public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
            boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
            boolean requireConsumptionOfAllParameters = !(AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.ALLOW_UNUSED_PARAMETERS, false));

            if (injectionMethod.getDeclaringClass().isAssignableFrom(impl)) {
                return wrapLifeCycle(monitor.newInjector(new SpecificMethodInjector(key, impl, monitor, useNames, requireConsumptionOfAllParameters, methodParams, injectionMethod)), lifecycle);
            } else {
                throw new PicoCompositionException("method [" + injectionMethod + "] not on impl " + impl.getName());
            }
        }
    }

    /**
     * Injection will happen through a single method for the component.
     *
     * Most likely it is a method called 'inject', though that can be overridden.
     *
     * @author Paul Hammant
     * @author Aslak Helles&oslash;y
     * @author Jon Tirs&eacute;n
     * @author Zohar Melamed
     * @author J&ouml;rg Schaible
     * @author Mauro Talevi
     */
    public static class MethodInjector<T> extends MultiArgMemberInjector<T> {
        private transient ThreadLocalCyclicDependencyGuard<Object> instantiationGuard;
        private final String methodNamePrefix;

        /**
         * Creates a MethodInjector
         *
         * @param key            the search key for this implementation
         * @param impl the concrete implementation
         * @param monitor                 the component monitor used by this addAdapter
         * @param methodName              the method name
         * @param useNames                use argument names when looking up dependencies
         * @param parameters              the parameters to use for the initialization
         * @throws com.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
         *                              if the implementation is not a concrete class.
         * @throws NullPointerException if one of the parameters is <code>null</code>
         */
        public MethodInjector(final Object key, final Class<T> impl,
        					final ComponentMonitor monitor, final String methodName,
        					final boolean useNames, final boolean useAllParameters,
                              final MethodParameters... parameters) throws NotConcreteRegistrationException {
            super(key, impl, parameters, monitor, useNames, useAllParameters);
            this.methodNamePrefix = methodName;
        }



        protected List<Method> getInjectorMethods() {
        	Class<?> toIntrospect = null;
            //Method[] methods = new Method[0];
            try {
                //methods = super.getComponentImplementation().getMethods();
            	toIntrospect = super.getComponentImplementation();
            } catch (AmbiguousComponentResolutionException e) {
                e.setComponent(getComponentImplementation());
                throw e;
            }

            if (toIntrospect == null) {
            	throw new NullPointerException("No implementation class defined for " + this);
            }


            HashMap<String, Set<Method>> allMethodsAnalyzed = new HashMap<String,Set<Method>>();
            List<Method> methodz = new ArrayList<Method>();
            recursiveCheckInjectorMethods(toIntrospect, toIntrospect, methodz, allMethodsAnalyzed);

            //Inject in JSR330 compliant order.
            Collections.sort(methodz, new JSRAccessibleObjectOrderComparator());

            return methodz;
        }

        /**
         * Goes through all methods (including private base classes) to look for potential injection methods.
         * @param originalType
         * @param type
         * @param receiver
         * @param allMethodsAnalyzed
         */
        protected void recursiveCheckInjectorMethods(final Class<?> originalType, final Class<?> type, final List<Method> receiver, final HashMap<String, Set<Method>> allMethodsAnalyzed) {
        	if (type.isAssignableFrom(Object.class)) {
        		return;
        	}

        	for (Method eachMethod : type.getDeclaredMethods()) {
        		if(alreadyAnalyzedChildClassMethod(eachMethod, allMethodsAnalyzed)) {
        			//This method was defined in a child class, what the child class says, goes.
        			continue;
        		}

        		addToMethodsAnalyzed(allMethodsAnalyzed, eachMethod);

        		if (isInjectorMethod(originalType, eachMethod)) {
        			receiver.add(eachMethod);
        		}
        	}

        	recursiveCheckInjectorMethods(originalType, type.getSuperclass(), receiver, allMethodsAnalyzed);
        }


        private void addToMethodsAnalyzed(final HashMap<String, Set<Method>> allMethodsAnalyzed, final Method eachMethod) {
        	if (!allMethodsAnalyzed.containsKey(eachMethod.getName())) {
        		allMethodsAnalyzed.put(eachMethod.getName(), new HashSet<Method>());
        	}

        	allMethodsAnalyzed.get(eachMethod.getName()).add(eachMethod);
		}

		private boolean alreadyAnalyzedChildClassMethod(final Method eachMethod,
				final HashMap<String, Set<Method>> allMethodsAnalyzed) {

			Set<Method> methodsByName = allMethodsAnalyzed.get(eachMethod.getName());
			if (methodsByName == null) {
				return false;
			}

			return false;
		}

		protected boolean isInjectorMethod(final Class<?> originalType, final Method method) {
            return method.getName().startsWith(methodNamePrefix);
        }

		@Override
        @SuppressWarnings("unchecked")
        public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
        	boolean i_Instantiated = false;
        	T result;
        	try {
	            if (instantiationGuard == null) {
	            	i_Instantiated = true;
	                instantiationGuard = new ThreadLocalCyclicDependencyGuard<Object>() {
	                    @Override
	                    @SuppressWarnings("synthetic-access")
	                    public Object run(final Object instance) {
	                        List<Method> methods = getInjectorMethods();
	                        T inst = null;
	                        ComponentMonitor monitor = currentMonitor();
	                        Method lastMethod = null;
	                        try {
	                            monitor.instantiating(container, MethodInjector.this, null);
	                            long startTime = System.currentTimeMillis();
	                            Object[] methodParameters = null;
	                            inst = getComponentImplementation().newInstance();
	                            for (Method method : methods) {
	                                lastMethod = method;
	                                methodParameters = getMemberArguments(guardedContainer, method, into);
	                                invokeMethod(method, methodParameters, inst, container);
	                            }
	                            monitor.instantiated(container, MethodInjector.this,
	                                                          null, inst, methodParameters, System.currentTimeMillis() - startTime);
	                            return inst;
	                        } catch (InstantiationException e) {
	                            return caughtInstantiationException(monitor, null, e, container);
	                        } catch (IllegalAccessException e) {
	                            return caughtIllegalAccessException(monitor, lastMethod, inst, e);

	                        }
	                    }
	                };
	            }
	            instantiationGuard.setGuardedContainer(container);
	            result =  (T) instantiationGuard.observe(getComponentImplementation(), null);
        	} finally {
	            if (i_Instantiated) {
	            	instantiationGuard.remove();
	            	instantiationGuard = null;
	            }
        	}
            return result;
        }

        protected Object[] getMemberArguments(final PicoContainer container, final Method method, final Type into) {
            return super.getMemberArguments(container, method, method.getGenericParameterTypes(), getBindings(method.getParameterAnnotations()), into);
        }

        @Override
        public Object decorateComponentInstance(final PicoContainer container, final Type into, final T instance) {
        	return partiallyDecorateComponentInstance(container, into, instance, null);
        }



		@Override
		@SuppressWarnings("unchecked")
		public Object partiallyDecorateComponentInstance(final PicoContainer container, final Type into, final T instance,
				final Class<?> injectionTypeFilter) {
			boolean iInstantiated = false;
			Object o;
			try {
	            if (instantiationGuard == null) {
	            	iInstantiated = true;
	                instantiationGuard = new ThreadLocalCyclicDependencyGuard<Object>() {
	                    @Override
	                    @SuppressWarnings("synthetic-access")
	                    public Object run(final Object inst) {
	                        List<Method> methods = getInjectorMethods();
	                        Object lastReturn = null;
	                        for (Method method : methods) {
	                        	Class<?> methodClass = method.getDeclaringClass();
	                        	Class<?> filterClass = injectionTypeFilter;
	                        	if (!allowedMethodBasedOnFilter(filterClass, method)) {
	                        		continue;
	                        	}

	                            if (methodClass.isAssignableFrom(inst.getClass())) {
	                                Object[] methodParameters = getMemberArguments(guardedContainer, method, into);
	                                lastReturn = invokeMethod(method, methodParameters, (T) inst, container);
	                            }
	                        }
	                        return lastReturn;
	                    }

	                };
	            }
	            instantiationGuard.setGuardedContainer(container);
	            o = instantiationGuard.observe(getComponentImplementation(), instance);
			} finally {
	            if (iInstantiated) {
	            	instantiationGuard.remove();
	            	instantiationGuard = null;
	            }
			}

            return o;
        }

		/**
		 * Method injection filter sometimes decorates based on one or two specific methods.
		 * Filtering isn't appropriate for those cases, but it is needed for JSR injection.
		 * @param injectionTypeFilter
		 * @param method
		 * @return
		 */
		protected boolean allowedMethodBasedOnFilter(final Class<?> injectionTypeFilter, final Method method) {
			return true;
		}

        Object invokeMethod(final Method method, final Object[] methodParameters, final T instance, final PicoContainer container) {
            try {
                Object rv = currentMonitor().invoking(container, MethodInjector.this, (Member) method, instance, methodParameters);
                if (rv == ComponentMonitor.KEEP) {
                    long str = System.currentTimeMillis();
                    makeAccessibleIfDesired(method);
                    rv = method.invoke(instance, methodParameters);
                    currentMonitor().invoked(container, MethodInjector.this, method, instance, System.currentTimeMillis() - str, rv, methodParameters);
                }
                return rv;
            } catch (IllegalAccessException e) {
                return caughtIllegalAccessException(currentMonitor(), method, instance, e);
            } catch (InvocationTargetException e) {
                currentMonitor().invocationFailed(method, instance, e);
                if (e.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException) e.getTargetException();
                } else if (e.getTargetException() instanceof Error) {
                    throw (Error) e.getTargetException();
                }
                throw new PicoCompositionException(e);
            }
        }


        /**
         * Allow override to make methods accessible as per jsr.
         * @param method
         */
        protected void makeAccessibleIfDesired(final Method method) {

        }


		@Override
		@SuppressWarnings("unchecked")
        public void verify(final PicoContainer container) throws PicoCompositionException {
			boolean i_created = false;
			try {
	            if (verifyingGuard == null) {
	            	i_created = true;
	                verifyingGuard = new ThreadLocalCyclicDependencyGuard<Void>() {
	                    @Override
	                    public Void run(final Object inst) {
	                        final List<Method> methods = getInjectorMethods();
	                        for (Method method : methods) {

	                            final Class[] parameterTypes = method.getParameterTypes();

	                            AccessibleObjectParameterSet paramsForMethod = getParameterToUseForObject(method, parameters);


	                            final Parameter[] currentParameters = paramsForMethod != null ? paramsForMethod.getParams() : createDefaultParameters(parameterTypes.length);
	                            for (int i = 0; i < currentParameters.length; i++) {
	                                currentParameters[i].verify(container, MethodInjector.this, parameterTypes[i],
	                                        new ParameterNameBinding(getParanamer(), method, i), useNames(),
	                                        getBindings(method.getParameterAnnotations())[i]);
	                            }

	                        }
	                        return null;
	                    }
	                };
	            }
	            verifyingGuard.setGuardedContainer(container);
	            verifyingGuard.observe(getComponentImplementation(), null);
			} finally {
	            if (i_created) {
	            	verifyingGuard.remove();
	            	verifyingGuard = null;
	            }
			}
        }



        @Override
        public String getDescriptor() {
            StringBuilder mthds = new StringBuilder();
            for (Method method : getInjectorMethods()) {
                mthds.append(",").append(method.getName());
            }
            return "MethodInjector["+mthds.substring(1)+"]-";
        }

        @Override
        protected boolean isNullParamAllowed(final AccessibleObject member, final int i) {
            Annotation[] annotations = ((Method) member).getParameterAnnotations()[i];
            for (Annotation annotation : annotations) {
                if (annotation instanceof Nullable) {
                    return true;
                }
            }
            return false;
        }



    }
}
