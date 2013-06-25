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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.Emjection;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.NameBinding;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.parameters.AccessibleObjectParameterSet;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

/**
 * A {@link com.picocontainer.InjectionType} for constructor injection.
 * The factory creates {@link ConstructorInjector}.
 *
 * If there is more than one constructor for the component, the one with the
 * most satisfiable parameters will be used.  By default, the choice of
 * constructor for the component in question will be remembered between usages.
 *
 * @author Paul Hammant
 * @author Jon Tirs&eacute;n
 */
@SuppressWarnings("serial")
public class ConstructorInjection extends AbstractInjectionType {

    protected final boolean rememberChosenConstructor;

    /**
     *
     * @param rememberChosenConstructor whether 'which constructor?' should be remembered
     *                                  from use to use for the associated injector.
     */
    public ConstructorInjection(final boolean rememberChosenConstructor) {
        this.rememberChosenConstructor = rememberChosenConstructor;
    }

    /**
     * Will remember which constructor to use between usages on the associated
     * Injector.
     */
    public ConstructorInjection() {
        this(true);
    }

    public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties properties, final Object key,
                                                   final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        boolean useNames = AbstractBehavior.arePropertiesPresent(properties, Characteristics.USE_NAMES, true);
        ConstructorInjector<T> injector = newConstructorInjector(monitor, key, impl, useNames, constructorParams);
        injector.enableEmjection(AbstractBehavior.removePropertiesIfPresent(properties, Characteristics.EMJECTION_ENABLED));
        return wrapLifeCycle(monitor.newInjector(injector), lifecycle);
    }

    protected <T>ConstructorInjector<T> newConstructorInjector(final ComponentMonitor monitor, final Object key, final Class<T> impl, final boolean useNames, final ConstructorParameters parameters) {
        return new ConstructorInjector<T>(monitor, useNames, rememberChosenConstructor, key, impl, parameters);
    }

    /**
     * Injection will happen through a constructor for the component.
     *
     * @author Paul Hammant
     * @author Aslak Helles&oslash;y
     * @author Jon Tirs&eacute;n
     * @author Zohar Melamed
     * @author J&ouml;rg Schaible
     * @author Mauro Talevi
     */
    public static class ConstructorInjector<T> extends MultiArgMemberInjector<T> {

        private transient List<Constructor<T>> sortedMatchingConstructors;
        private transient ThreadLocalCyclicDependencyGuard<T> instantiationGuard;
        private boolean rememberChosenConstructor = true;
        private transient CtorAndAdapters<T> chosenConstructor;
        private boolean enableEmjection = false;
        private boolean allowNonPublicClasses = false;



        /***
         * Convenience method that allows creation of a constructor injector with specific, optional parameters.
         * @param key
         * @param impl
         * @param params
         */
        public ConstructorInjector(final Object key, final Class<T> impl, final Parameter... params) {
        	this(key, impl, (params == null || params.length == 0) ? (ConstructorParameters)null :  new ConstructorParameters(params));
        }

        /**
         * Constructor injector that uses no monitor and no lifecycle adapter.  This is a more
         * convenient constructor for use when instantiating a constructor injector directly.
         * @param key the search key for this implementation
         * @param impl the concrete implementation
         * @param parameters the parameters used for initialization
         */
        public ConstructorInjector(final Object key, final Class<T> impl, final ConstructorParameters parameters) {
            this(new NullComponentMonitor(), false, key, impl, parameters);
        }

        /**
         * Creates a ConstructorInjector
         *
         * @param monitor                 the component monitor used by this addAdapter
         * @param useNames                use argument names when looking up dependencies
         * @param key            the search key for this implementation
         * @param impl the concrete implementation
         * @param parameters              the parameters to use for the initialization
         * @throws com.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
         *                              if the implementation is not a concrete class.
         * @throws NullPointerException if one of the parameters is <code>null</code>
         */
        public ConstructorInjector(final ComponentMonitor monitor, final boolean useNames, final Object key, final Class<T> impl,
        		final ConstructorParameters parameters) throws  NotConcreteRegistrationException {
            super(key, impl,
            		parameters != null ?  new AccessibleObjectParameterSet[] { parameters } : null,
            		monitor, useNames,

            		/**
            		 * Constructor Injection should always be using all parameters provided to it that
            		 * don't have a member name attached.
            		 */
            		true);
        }

        /**
         * Creates a ConstructorInjector
         *
         * @param monitor                 the component monitor used by this addAdapter
         * @param useNames                use argument names when looking up dependencies
         * @param rememberChosenCtor      remember the chosen constructor (to speed up second/subsequent calls)
         * @param key            the search key for this implementation
         * @param impl the concrete implementation
         * @param parameters              the parameters to use for the initialization
         * @throws com.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
         *                              if the implementation is not a concrete class.
         * @throws NullPointerException if one of the parameters is <code>null</code>
         */
        public ConstructorInjector(final ComponentMonitor monitor, final boolean useNames, final boolean rememberChosenCtor, final Object key, final Class<T> impl,
                                   final ConstructorParameters constructorParams) throws  NotConcreteRegistrationException {
            super(key, impl, toAccessibleObjectParameterSetArray(constructorParams), monitor, useNames, true);
            this.rememberChosenConstructor = rememberChosenCtor;
        }


        private CtorAndAdapters<T> getGreediestSatisfiableConstructor(final PicoContainer guardedContainer, final Class<? extends T> impl) {
            CtorAndAdapters<T> ctor = null;
            try {
                if (chosenConstructor == null) {
                    ctor = getGreediestSatisfiableConstructor(guardedContainer);
                }
                if (rememberChosenConstructor) {
                    if (chosenConstructor == null) {
                        chosenConstructor = ctor;
                    } else {
                        ctor = chosenConstructor;
                    }
                }
            } catch (AmbiguousComponentResolutionException e) {
                e.setComponent(getComponentImplementation());
                throw e;
            }
            return ctor;
        }

        @SuppressWarnings({ "synthetic-access", "rawtypes" })
        protected CtorAndAdapters<T> getGreediestSatisfiableConstructor(final PicoContainer container) throws PicoCompositionException {
            final Set<Constructor<?>> conflicts = new HashSet<Constructor<?>>();
            final Set<Type> unsatisfiableDependencyTypes = new HashSet<Type>();
            final Map<ResolverKey, Parameter.Resolver> resolvers = new HashMap<ResolverKey, Parameter.Resolver>();
            if (sortedMatchingConstructors == null) {
                sortedMatchingConstructors = getSortedMatchingConstructors();
            }
            Constructor<T> greediestConstructor = null;
            Parameter[] greediestConstructorsParameters = null;
            ComponentAdapter[] greediestConstructorsParametersComponentAdapters = null;
            int lastSatisfiableConstructorSize = -1;
            Type unsatisfiedDependency = null;
            Constructor<?> unsatisfiedConstructor = null;
            int lastParameterTested = 0;
            for (final Constructor<T> sortedMatchingConstructor : sortedMatchingConstructors) {
            	try {
	                boolean failedDependency = false;
	                Type[] parameterTypes = sortedMatchingConstructor.getGenericParameterTypes();
	                fixGenericParameterTypes(sortedMatchingConstructor, parameterTypes);
	                Annotation[] bindings = getBindings(sortedMatchingConstructor.getParameterAnnotations());

	                final ConstructorParameters constructorParameters = (ConstructorParameters) (parameters != null && parameters.length > 0 ? parameters[0] : new ConstructorParameters());
	                final Parameter[] currentParameters = constructorParameters.getParams() != null ? constructorParameters.getParams() : createDefaultParameters(parameterTypes.length);

	                final ComponentAdapter<?>[] currentAdapters = new ComponentAdapter<?>[currentParameters.length];

	                //For debug messages if something fails since we swap parameters part way through the function
	                List<Parameter> parametersUsed = new ArrayList<Parameter>(Arrays.asList(currentParameters));

	                // remember: all constructors with less arguments than the given parameters are filtered out already
	                for (int j = 0; j < currentParameters.length; j++) {
	                	lastParameterTested = j;
	                    // check whether this constructor is satisfiable
	                    Type expectedType = box(parameterTypes[j]);
	                    NameBinding expectedNameBinding = new ParameterNameBinding(getParanamer(), sortedMatchingConstructor, j);
	                    Parameter parameterToUse = getParameterToUse(sortedMatchingConstructor,j, currentParameters[j]);
	                    parametersUsed.set(j, parameterToUse);
	                    ResolverKey resolverKey = new ResolverKey(expectedType, useNames() ? expectedNameBinding.getName() : null, useNames(), bindings[j], parameterToUse);
	                    Parameter.Resolver resolver = resolvers.get(resolverKey);
	                    if (resolver == null) {
	                        Parameter currentParameter = parameterToUse;
	                        Annotation annotation = bindings[j];
	                        boolean b = useNames();
	                        resolver = currentParameter.resolve(container, this, null, expectedType, expectedNameBinding, b, annotation);
	                        resolvers.put(resolverKey, resolver);
	                    }
	                    if (resolver.isResolved()) {
	                        currentAdapters[j] = resolver.getComponentAdapter();
	                        continue;
	                    }
	                    unsatisfiableDependencyTypes.add(expectedType);
	                    unsatisfiedDependency = box(parameterTypes[j]);
	                    unsatisfiedConstructor = sortedMatchingConstructor;
	                    failedDependency = true;
	                }

	                if (greediestConstructor != null && parameterTypes.length != lastSatisfiableConstructorSize) {
	                    if (conflicts.isEmpty()) {
	                        // we found our match [aka. greedy and satisfied]
	                        return new CtorAndAdapters<T>(greediestConstructor, greediestConstructorsParameters, greediestConstructorsParametersComponentAdapters);
	                    }
	                    // fits although not greedy
	                    conflicts.add(sortedMatchingConstructor);
	                } else if (!failedDependency && lastSatisfiableConstructorSize == parameterTypes.length) {
	                    // satisfied and same size as previous one?
	                    conflicts.add(sortedMatchingConstructor);
	                    conflicts.add(greediestConstructor);
	                } else if (!failedDependency) {
	                    greediestConstructor = sortedMatchingConstructor;
	                    greediestConstructorsParameters = parametersUsed.toArray(new Parameter[parametersUsed.size()]);
	                    greediestConstructorsParametersComponentAdapters = currentAdapters;
	                    lastSatisfiableConstructorSize = parameterTypes.length;
	                }

            	} catch (AmbiguousComponentResolutionException e) {
                    // embellish with the constructor being injected into and
            		// parameter # causing the problem
                    e.setMember(sortedMatchingConstructor);
                    e.setParameterNumber(lastParameterTested);
                    throw e;
                }
            }
            if (!conflicts.isEmpty()) {
                throw new PicoCompositionException(conflicts.size() + " satisfiable constructors is too many for '"+getComponentImplementation()+"'. Constructor List:" + conflicts.toString().replace(getComponentImplementation().getName(),"<init>").replace("public <i","<i"));
            } else if (greediestConstructor == null && !unsatisfiableDependencyTypes.isEmpty()) {
                throw new UnsatisfiableDependenciesException(this.getComponentImplementation().getName()
                        + " has unsatisfied dependency '" + unsatisfiedDependency
                        + "' for constructor '" + unsatisfiedConstructor + "'" + " from " + container);
            } else if (greediestConstructor == null) {
                // be nice to the user, show all constructors that were filtered out
                final Set<Constructor> nonMatching = new HashSet<Constructor>();
                nonMatching.addAll(Arrays.asList(getConstructors()));
                throw new PicoCompositionException("Either the specified parameters do not match any of the following constructors: " + nonMatching.toString() + "; OR the constructors were not accessible for '" + getComponentImplementation().getName() + "'");
            }
            return new CtorAndAdapters<T>(greediestConstructor, greediestConstructorsParameters, greediestConstructorsParametersComponentAdapters);
        }


        /**
         * Allows for subclasses to override the {@link com.picocontainer.Parameter} for a given constructor argument.
         * @param constructorToExamine the current constructor candidate.
         * @param constructorParameterIndex the current index of the constructor arguments array.
         * @param parameter the currently defined parameter.
         * @return
         */
    	protected Parameter getParameterToUse(final Constructor<?> constructorToExamine, final int constructorParameterIndex, final Parameter parameter) {
    		return parameter;
    	}


        public void enableEmjection(final boolean enableEmjection) {
            this.enableEmjection = enableEmjection;
        }

        public ConstructorInjector<T> withNonPublicConstructors() {
            allowNonPublicClasses = true;
            return this;
        }

        private static final class ResolverKey {
            private final Type expectedType;
            private final String pName;
            private final boolean useNames;
            private final Annotation binding;
            private final Parameter currentParameter;

            private ResolverKey(final Type expectedType, final String pName, final boolean useNames, final Annotation binding, final Parameter currentParameter) {
                this.expectedType = expectedType;
                this.pName = pName;
                this.useNames = useNames;
                this.binding = binding;
                this.currentParameter = currentParameter;
            }

            // Generated by IDEA
            @Override
            public boolean equals(final Object o) {
                if (this == o) {
					return true;
				}
                if (o == null || getClass() != o.getClass()) {
					return false;
				}

                ResolverKey that = (ResolverKey) o;

                return useNames == that.useNames
                        && !(binding != null ? !binding.equals(that.binding) : that.binding != null)
                        && currentParameter.equals(that.currentParameter)
                        && expectedType.equals(that.expectedType)
                        && !(pName != null ? !pName.equals(that.pName) : that.pName != null);


            }

            @Override
            public int hashCode() {
                int result;
                result = expectedType.hashCode();
                result = 31 * result + (pName != null ? pName.hashCode() : 0);
                result = 31 * result + (useNames ? 1 : 0);
                result = 31 * result + (binding != null ? binding.hashCode() : 0);
                result = 31 * result + currentParameter.hashCode();
                return result;
            }
        }

        private void fixGenericParameterTypes(final Constructor<T> ctor, final Type[] parameterTypes) {
            for (int i = 0; i < parameterTypes.length; i++) {
                Type parameterType = parameterTypes[i];
                if (parameterType instanceof TypeVariable) {
                    parameterTypes[i] = ctor.getParameterTypes()[i];
                }
            }
        }

        protected class CtorAndAdapters<TYPE> {
            private final Constructor<TYPE> ctor;
            private final Parameter[] constructorParameters;
            private final ComponentAdapter<?>[] injecteeAdapters;

            @SuppressWarnings("rawtypes")
			public CtorAndAdapters(final Constructor<TYPE> ctor, final Parameter[] parameters, final ComponentAdapter[] injecteeAdapters) {
                this.ctor = ctor;
                this.constructorParameters = parameters;
                this.injecteeAdapters = injecteeAdapters;
            }

            public Constructor<TYPE> getConstructor() {
                return ctor;
            }

            public Object[] getParameterArguments(final PicoContainer container, final Type into) {
                Type[] parameterTypes = ctor.getGenericParameterTypes();
                // as per fixParameterType()
                for (int i = 0; i < parameterTypes.length; i++) {
                    Type parameterType = parameterTypes[i];
                    if (parameterType instanceof TypeVariable) {
                        parameterTypes[i] = ctor.getParameterTypes()[i];
                    }
                }
                boxParameters(parameterTypes);
                Object[] result = new Object[constructorParameters.length];
                Annotation[] bindings = getBindings(ctor.getParameterAnnotations());
                for (int i = 0; i < constructorParameters.length; i++) {

                    result[i] = getParameter(container, ctor, i, parameterTypes[i],
                            bindings[i], constructorParameters[i], injecteeAdapters[i], into);


                    //Shouldn't be possible for CDI.
                    assert result[i] != Parameter.NULL_RESULT;
                }
                return result;
            }

            @SuppressWarnings("rawtypes")
			public ComponentAdapter[] getInjecteeAdapters() {
                return injecteeAdapters;
            }

            public Parameter[] getParameters() {
                return constructorParameters;
            }
        }

        @Override
        public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
        	boolean i_Instantiated = false;
        	T inst;
        	try {
	            if (instantiationGuard == null) {
	            	i_Instantiated = true;
	                instantiationGuard = new ThreadLocalCyclicDependencyGuard<T>() {
	                    @Override
	                    @SuppressWarnings("synthetic-access")
	                    public T run(final Object instance) {
	                        CtorAndAdapters<T> ctorAndAdapters = getGreediestSatisfiableConstructor(guardedContainer, getComponentImplementation());
	                        ComponentMonitor monitor = currentMonitor();
	                        Constructor<T> ctor = ctorAndAdapters.getConstructor();
	                        try {
	                            Object[] ctorParameters = ctorAndAdapters.getParameterArguments(guardedContainer, into);
	                            ctor = monitor.instantiating(container, ConstructorInjector.this, ctor);
	                            if(ctor == null) {
	                                throw new NullPointerException("Component Monitor " + monitor
	                                                + " returned a null constructor from method 'instantiating' after passing in " + ctorAndAdapters);
	                            }
	                            long startTime = System.currentTimeMillis();
	                            changeAccessToModifierifNeeded(ctor);
	                            T inst = newInstance(ctor, ctorParameters);
	                            monitor.instantiated(container, ConstructorInjector.this,
	                                    ctor, inst, ctorParameters, System.currentTimeMillis() - startTime);
	                            return inst;
	                        } catch (InvocationTargetException e) {
	                            monitor.instantiationFailed(container, ConstructorInjector.this, ctor, e);
	                            if (e.getTargetException() instanceof RuntimeException) {
	                                throw (RuntimeException) e.getTargetException();
	                            } else if (e.getTargetException() instanceof Error) {
	                                throw (Error) e.getTargetException();
	                            }
	                            throw new PicoCompositionException(e.getTargetException());
	                        } catch (InstantiationException e) {
	                            return caughtInstantiationException(monitor, ctor, e, container);
	                        } catch (IllegalAccessException e) {
	                            return caughtIllegalAccessException(monitor, ctor, e, container);

	                        }
	                    }
	                };
	            }
	            instantiationGuard.setGuardedContainer(container);
	            inst = instantiationGuard.observe(getComponentImplementation(), null);
	            decorate(inst, container);
        	} finally {
	            if (i_Instantiated) {
	            	instantiationGuard.remove();
	            	instantiationGuard = null;
	            }
        	}
            return inst;
        }

        private void decorate(final T inst, final PicoContainer container) {
            if (enableEmjection) {
                Emjection.setupEmjection(inst, container);
            }
        }

        private List<Constructor<T>> getSortedMatchingConstructors() {
            List<Constructor<T>> matchingConstructors = new ArrayList<Constructor<T>>();
            Constructor<T>[] allConstructors = getConstructors();
            // filter out all constructors that will definitely not match
            final Parameter[] paramsToUse = (parameters != null && parameters.length > 0) ? parameters[0].getParams() : null;

            for (Constructor<T> constructor : allConstructors) {
                if ((paramsToUse == null || constructor.getParameterTypes().length == paramsToUse.length)
                        && hasApplicableConstructorModifiers(constructor.getModifiers())) {
                    matchingConstructors.add(constructor);
                }
            }
            // optimize list of constructors moving the longest at the beginning
            if (paramsToUse == null) {
                Collections.sort(matchingConstructors, new Comparator<Constructor>() {
                    public int compare(final Constructor arg0, final Constructor arg1) {
                        return arg1.getParameterTypes().length - arg0.getParameterTypes().length;
                    }
                });
            }
            return matchingConstructors;
        }

        /**
         * Tests to see if modifier is public
         * @param modifiers the modifiers to test
         * @return true if public
         */
        protected boolean hasApplicableConstructorModifiers(final int modifiers) {
            return allowNonPublicClasses || ( (modifiers & Modifier.PUBLIC) != 0);
        }

        /**
         * Not by default, but some constructors may need to be setAccessible(true)
         * @param ctor the ctor to change
         */
        protected void changeAccessToModifierifNeeded(final Constructor<T> ctor) {
        }

        private Constructor<T>[] getConstructors() {
            return AccessController.doPrivileged(new PrivilegedAction<Constructor<T>[]>() {
                @SuppressWarnings("unchecked")
				public Constructor<T>[] run() {
                    return (Constructor<T>[]) getComponentImplementation().getDeclaredConstructors();
                }
            });
        }

		@Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void verify(final PicoContainer container) throws PicoCompositionException {
			boolean i_instantiated = false;
			try {
	            if (verifyingGuard == null) {
	            	i_instantiated = true;
	                verifyingGuard = new ThreadLocalCyclicDependencyGuard() {
	                    @Override
	                    public Object run(final Object inst) {
	                        final Constructor constructor = getGreediestSatisfiableConstructor(guardedContainer).getConstructor();
	                        final Class[] parameterTypes = constructor.getParameterTypes();

	    	                final ConstructorParameters constructorParameters = (ConstructorParameters) (parameters != null && parameters.length > 0 ? parameters[0] : new ConstructorParameters());
	    	                final Parameter[] currentParameters = constructorParameters.getParams() != null ? constructorParameters.getParams() : createDefaultParameters(parameterTypes.length);


	                        for (int i = 0; i < currentParameters.length; i++) {
	                            currentParameters[i].verify(container, ConstructorInjector.this, box(parameterTypes[i]),
	                                new ParameterNameBinding(getParanamer(),  constructor, i),
	                                    useNames(), getBindings(constructor.getParameterAnnotations())[i]);
	                        }
	                        return null;
	                    }
	                };
	            }
	            verifyingGuard.setGuardedContainer(container);
	            verifyingGuard.observe(getComponentImplementation(), null);

			} finally {
	            if (i_instantiated) {
	            	verifyingGuard.remove();
	            	verifyingGuard = null;
	            }
			}
	      }

        @Override
        public String getDescriptor() {
            return "ConstructorInjector-";
        }


    }

}
