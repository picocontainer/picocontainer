/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.annotations.Bind;
import org.picocontainer.parameters.AccessibleObjectParameterSet;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * Injection will happen iteratively after component instantiation.  This class deals with injection types that only
 * have one argument.  Examples would be single-argument methods such as setters, or an object's fields.
 */
@SuppressWarnings("serial")
public abstract class IterativeInjector<T> extends AbstractInjector<T> {

    private static final Object[] NONE = new Object[0];

    private transient ThreadLocalCyclicDependencyGuard<T> instantiationGuard;

    protected volatile transient List<AccessibleObject> injectionMembers;
    protected transient Type[] injectionTypes;
    protected transient Annotation[] bindings;

    private transient Paranamer paranamer;
    private volatile transient boolean initialized;

	private boolean requireConsumptionOfAllParameters;



	/**
     * Constructs a IterativeInjector
     *
     * @param key            the search key for this implementation
     * @param impl the concrete implementation
     * @param monitor                 the component monitor used by this addAdapter
     * @param useNames                use argument names when looking up dependencies
     * @param staticsInitializedReferenceSet (Optional) A data structure that keeps track of
     * 		static intializations.  If null, then static members will not be injected.
     * @param parameters              the parameters to use for the initialization
     * @throws org.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
     *                              if the implementation is not a concrete class.
     * @throws NullPointerException if one of the parameters is <code>null</code>
     */
    public IterativeInjector(final Object key, final Class<?> impl, final ComponentMonitor monitor, final boolean useNames,
                             final StaticsInitializedReferenceSet staticsInitializedReferenceSet, final AccessibleObjectParameterSet... parameters) throws  NotConcreteRegistrationException {
        this(key, impl, monitor, useNames, true, parameters);
    }

    /**
     * Constructs a IterativeInjector for use in a composite injection environment.
     *
     * @param key            the search key for this implementation
     * @param impl the concrete implementation
     * @param monitor                 the component monitor used by this addAdapter
     * @param useNames                use argument names when looking up dependencies
     * @param requireConsumptionOfAllParameters If set to true, then all parameters (ie: ComponentParameter/ConstantParameter) must be
     * 		used by this injector.  If set to false, then no error occurs if all parameters don't match this type of injection.  It is assumed
     * 		that another type of injection will be using them.
     * @param parameters              the parameters to use for the initialization
     * @throws org.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
     *                              if the implementation is not a concrete class.
     * @throws NullPointerException if one of the parameters is <code>null</code>
     */
    public IterativeInjector(final Object key, final Class<?> impl, final ComponentMonitor monitor, final boolean useNames, final boolean requireConsumptionOfAllParameters,
    		final AccessibleObjectParameterSet... parameters) throws  NotConcreteRegistrationException {
    	super(key, impl, monitor, useNames, parameters);
		this.requireConsumptionOfAllParameters = requireConsumptionOfAllParameters;
    }


    protected Constructor<?> getConstructor()  {
        Object retVal = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    return getComponentImplementation().getConstructor((Class[])null);
                } catch (NoSuchMethodException e) {
                    return new PicoCompositionException(e);
                } catch (SecurityException e) {
                    return new PicoCompositionException(e);
                }
            }
        });
        if (retVal instanceof Constructor) {
            return (Constructor<?>) retVal;
        } else {
            throw (PicoCompositionException) retVal;
        }
    }



    /**
     * Key-Value Paired parameter/accessible object
     * @author Michael Rimov
     *
     */
    public static class ParameterToAccessibleObjectPair {
		private final AccessibleObject accessibleObject;

    	private final AccessibleObjectParameterSet parameter;


    	/**
    	 *
    	 * @param accessibleObject
    	 * @param parameter set to null if there was no resolution for this accessible object.
    	 */
    	public ParameterToAccessibleObjectPair(final AccessibleObject accessibleObject, final AccessibleObjectParameterSet parameter) {
			super();
			this.accessibleObject = accessibleObject;
			this.parameter = parameter;
		}

		public AccessibleObject getAccessibleObject() {
			return accessibleObject;
		}

		public AccessibleObjectParameterSet getAccessibleObjectParameters() {
			return parameter;
		}

    	public boolean isResolved() {
    		return parameter != null && parameter.getParams() != null;
    	}

    }


    ParameterToAccessibleObjectPair[] getMatchingParameterListForMembers(final PicoContainer container) throws PicoCompositionException {
        if (initialized == false) {
        	synchronized(this) {
        		if (initialized == false) {
        			initializeInjectionMembersAndTypeLists();
        		}
        	}
        }

        final List<Object> matchingParameterList = new ArrayList<Object>(Collections.nCopies(injectionMembers.size(), null));

        final Set<AccessibleObjectParameterSet> notMatchingParameters = matchParameters(container, matchingParameterList, parameters);

        final Set<Type> unsatisfiableDependencyTypes = new HashSet<Type>();
        final List<AccessibleObject> unsatisfiableDependencyMembers = new ArrayList<AccessibleObject>();

        for (int i = 0; i < matchingParameterList.size(); i++) {
        	ParameterToAccessibleObjectPair param = (ParameterToAccessibleObjectPair)matchingParameterList.get(i);
            if (param == null ||  !param.isResolved()) {
                unsatisfiableDependencyTypes.add(injectionTypes[i]);
                unsatisfiableDependencyMembers.add(injectionMembers.get(i));
            }
        }
        if (unsatisfiableDependencyTypes.size() > 0) {
        	unsatisfiedDependencies(container, unsatisfiableDependencyTypes, unsatisfiableDependencyMembers);
        } else if (notMatchingParameters.size() > 0 && this.requireConsumptionOfAllParameters) {
            throw new PicoCompositionException("Following parameters do not match any of the injectionMembers for " + getComponentImplementation() + ": " + notMatchingParameters.toString());
        }
        return matchingParameterList.toArray(new ParameterToAccessibleObjectPair[matchingParameterList.size()]);
    }




	/**
     * Returns a set of integers that point to where in the Parameter array unmatched parameters exist.
     * @param container
     * @param matchingParameterList
     * @param assignedParameters {@link org.picocontainer.Parameter} for the current object being instantiated.
     * @return set of integers pointing to the index in the parameter array things went awry.
     */
    private Set<AccessibleObjectParameterSet> matchParameters(final PicoContainer container, final List<Object> matchingParameterList, final AccessibleObjectParameterSet... assignedParameters) {

        Set<AccessibleObjectParameterSet> unmatchedParameters = new HashSet<AccessibleObjectParameterSet>();

        for (AccessibleObject eachObject : injectionMembers) {
        	AccessibleObjectParameterSet currentParameter =   getParameterToUseForObject(eachObject, assignedParameters);

        	if (currentParameter == null) {
        		currentParameter = this.constructAccessibleObjectParameterSet(eachObject,new Parameter[] {constructDefaultComponentParameter()});
        	}

        	if (!matchParameter(container, matchingParameterList, currentParameter)) {
        		unmatchedParameters.add(currentParameter);
        	}
        }

        return unmatchedParameters;
    }

    private boolean matchParameter(final PicoContainer container, final List<Object> matchingParameterList, final AccessibleObjectParameterSet parameter) {
        for (int j = 0; j < injectionTypes.length; j++) {


            Object o = matchingParameterList.get(j);
            AccessibleObject targetInjectionMember = getTargetInjectionMember(injectionMembers, j, parameter.getParams()[0]);
            if (targetInjectionMember == null) {
            	return false;
            }

            AccessibleObjectParameterSet paramToUse = getParameterToUseForObject(targetInjectionMember, parameter);
            if (paramToUse == null) {
            	paramToUse = constructAccessibleObjectParameterSet(targetInjectionMember);
            }

            try {
                if (o == null
                        && paramToUse.getParams()[0].resolve(container, this, null, injectionTypes[j],
                                                   makeParameterNameImpl(targetInjectionMember),
                                                   useNames(), bindings[j]).isResolved()) {
                    matchingParameterList.set(j, new ParameterToAccessibleObjectPair(targetInjectionMember, paramToUse));
                    return true;
                }
            } catch (AmbiguousComponentResolutionException e) {
                e.setComponent(getComponentImplementation());
                e.setMember(injectionMembers.get(j));
                throw e;
            }
        }
        return false;
    }



	abstract protected boolean isAccessibleObjectEqualToParameterTarget(AccessibleObject testObject, Parameter currentParameter);

	/**
	 * Retrieves the appropriate injection member or null if the parameter doesn't match anything we know about and {@linkplain #requireConsumptionOfAllParameters}
	 * is set to false.
	 * @param injectionMembers
	 * @param currentIndex
	 * @param parameter
	 * @return Might return null if the parameter doesn't apply to this target.
	 */
	private AccessibleObject getTargetInjectionMember(final List<AccessibleObject> injectionMembers, final int currentIndex,
			final Parameter parameter) {

		if (parameter.getTargetName() == null) {
			return injectionMembers.get(currentIndex);
		}

		for (AccessibleObject eachObject : injectionMembers) {
			if (isAccessibleObjectEqualToParameterTarget(eachObject, parameter)) {
				return eachObject;
			}
		}

		if (this.requireConsumptionOfAllParameters) {
			throw new PicoCompositionException("There was no matching target field/method for target name "
						+ parameter.getTargetName()
						+ " using injector " + this.getDescriptor());
		}

		return null;
	}

	protected NameBinding makeParameterNameImpl(final AccessibleObject member) {
		if (member == null) {
			throw new NullPointerException("member");
		}

        if (paranamer == null) {
            paranamer = new CachingParanamer(new AnnotationParanamer(new AdaptiveParanamer()));
        }
        return new ParameterNameBinding(paranamer,  member, 0);
    }

    protected abstract void unsatisfiedDependencies(PicoContainer container, Set<Type> unsatisfiableDependencyTypes, List<AccessibleObject> unsatisfiableDependencyMembers);

    @Override
	public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
        final Constructor<?> constructor = getConstructor();
        boolean iInstantiated = false;
        T result;
        try {
	        if (instantiationGuard == null) {
	        	iInstantiated = true;
	            instantiationGuard = new ThreadLocalCyclicDependencyGuard<T>() {
	                @Override
					public T run(final Object instance) {
	                    final ParameterToAccessibleObjectPair[] matchingParameters = getMatchingParameterListForMembers(guardedContainer);
	                    Object componentInstance = makeInstance(container, constructor, currentMonitor());
	                    return  decorateComponentInstance(matchingParameters, currentMonitor(), componentInstance, container, guardedContainer, into, null);
	                }
	            };
	        }
	        instantiationGuard.setGuardedContainer(container);
	        result = instantiationGuard.observe(getComponentImplementation(), null);
        } finally {
	        if (iInstantiated) {
	        	instantiationGuard.remove();
	        	instantiationGuard = null;
	        }
        }
        return result;
    }

    T decorateComponentInstance(final ParameterToAccessibleObjectPair[] matchingParameters, final ComponentMonitor monitor, final Object componentInstance, final PicoContainer container, final PicoContainer guardedContainer, final Type into, final Class<?> partialDecorationFilter) {
        AccessibleObject member = null;
        Object injected[] = new Object[injectionMembers.size()];
        Object lastReturn = null;
        try {
            for (int i = 0; i < matchingParameters.length; i++) {
            	if (matchingParameters[i] != null) {
            		member = matchingParameters[i].getAccessibleObject();
            	}


            	//Skip it, we're only doing a partial injection
            	if (partialDecorationFilter != null && !partialDecorationFilter.equals( ((Member)member).getDeclaringClass() )) {
            		continue;
            	}

                if (matchingParameters[i] != null && matchingParameters[i].isResolved()) {
                	//Again, interative injector only supports 1 parameter
                	//per method to inject.
                    Object toInject = matchingParameters[i].getAccessibleObjectParameters().getParams()[0].resolve(guardedContainer, this, null, injectionTypes[i],
                                                                            makeParameterNameImpl(injectionMembers.get(i)),
                                                                            useNames(), bindings[i]).resolveInstance(into);
                    Object rv = monitor.invoking(container, this, (Member) member, componentInstance, new Object[] {toInject});
                    if (rv == ComponentMonitor.KEEP) {
                        long str = System.currentTimeMillis();
                        lastReturn = injectIntoMember(member, componentInstance, toInject);
                        monitor.invoked(container, this, (Member) member, componentInstance, System.currentTimeMillis() - str, lastReturn, new Object[] {toInject});
                    } else {
                        lastReturn = rv;
                    }
                    injected[i] = toInject;
                }
            }
            return (T) memberInvocationReturn(lastReturn, member, componentInstance);
        } catch (InvocationTargetException e) {
            return caughtInvocationTargetException(monitor, (Member) member, componentInstance, e);
        } catch (IllegalAccessException e) {
            return caughtIllegalAccessException(monitor, (Member) member, componentInstance, e);
        }
    }

    protected abstract Object memberInvocationReturn(Object lastReturn, AccessibleObject member, Object instance);

    private Object makeInstance(final PicoContainer container, final Constructor constructor, final ComponentMonitor monitor) {
        long startTime = System.currentTimeMillis();
        Constructor constructorToUse = monitor.instantiating(container,
                                                                      IterativeInjector.this, constructor);
        Object componentInstance;
        try {
            componentInstance = newInstance(constructorToUse, null);
        } catch (InvocationTargetException e) {
            monitor.instantiationFailed(container, IterativeInjector.this, constructorToUse, e);
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException)e.getTargetException();
            } else if (e.getTargetException() instanceof Error) {
                throw (Error)e.getTargetException();
            }
            throw new PicoCompositionException(e.getTargetException());
        } catch (InstantiationException e) {
            return caughtInstantiationException(monitor, constructor, e, container);
        } catch (IllegalAccessException e) {
            return caughtIllegalAccessException(monitor, constructor, e, container);
        }
        monitor.instantiated(container,
                                      IterativeInjector.this,
                                      constructorToUse,
                                      componentInstance,
                                      NONE,
                                      System.currentTimeMillis() - startTime);
        return componentInstance;
    }

    @Override
    public Object decorateComponentInstance(final PicoContainer container, final Type into, final T instance) {
    	return partiallyDecorateComponentInstance(container, into, instance, null);
    }


    @Override
    public Object partiallyDecorateComponentInstance(final PicoContainer container, final Type into, final T instance, final Class<?> superclassPortion) {
    	boolean iInstantiated = false;
    	T result;
    	try {
	        if (instantiationGuard == null) {
	        	iInstantiated = true;
	            instantiationGuard = new ThreadLocalCyclicDependencyGuard<T>() {
	                @Override
					public T run(final Object inst) {
	                    final ParameterToAccessibleObjectPair[] matchingParameters = getMatchingParameterListForMembers(guardedContainer);
	                    return decorateComponentInstance(matchingParameters, currentMonitor(), inst, container, guardedContainer, into, superclassPortion);
	                }
	            };
	        }
        	instantiationGuard.setGuardedContainer(container);
        	result =  instantiationGuard.observe(getComponentImplementation(), instance);
    	} finally {
	        if (iInstantiated) {
	        	instantiationGuard.remove();
	        	instantiationGuard = null;
	        }
    	}
        return result;
    }



    protected abstract Object injectIntoMember(AccessibleObject member, Object componentInstance, Object toInject) throws IllegalAccessException, InvocationTargetException;

	@Override
    @SuppressWarnings("unchecked")
    public void verify(final PicoContainer container) throws PicoCompositionException {
    	boolean i_Instantiated = false;
    	try {
	        if (verifyingGuard == null) {
	        	i_Instantiated = true;
	            verifyingGuard = new ThreadLocalCyclicDependencyGuard<T>() {
	                @Override
					public T run(final Object inst) {
	                    final ParameterToAccessibleObjectPair[] currentParameters = getMatchingParameterListForMembers(guardedContainer);
	                    for (int i = 0; i < currentParameters.length; i++) {
	                        currentParameters[i].getAccessibleObjectParameters().getParams()[0].verify(container, IterativeInjector.this, injectionTypes[i],
	                                                    makeParameterNameImpl(currentParameters[i].getAccessibleObject()), useNames(), bindings[i]);
	                    }
	                    return null;
	                }
	            };
	        }
	        verifyingGuard.setGuardedContainer(container);
	        verifyingGuard.observe(getComponentImplementation(), null);
    	} finally {
	        if (i_Instantiated) {
	        	verifyingGuard.remove();
	        	verifyingGuard = null;
	        }
    	}
    }

    protected void initializeInjectionMembersAndTypeLists() {
        injectionMembers = new ArrayList<AccessibleObject>();
        Set<String> injectionMemberNames = new HashSet<String>();
        List<Annotation> bingingIds = new ArrayList<Annotation>();
        final List<String> nameList = new ArrayList<String>();
        final List<Type> typeList = new ArrayList<Type>();
        final Method[] methods = getMethods();
        for (final Method method : methods) {
            final Type[] parameterTypes = method.getGenericParameterTypes();
            fixGenericParameterTypes(method, parameterTypes);

            String methodSignature = crudeMethodSignature(method);

            // We're only interested if there is only one parameter and the method name is bean-style.
            if (parameterTypes.length == 1) {
                boolean isInjector = isInjectorMethod(method);
                // ... and the method name is bean-style.
                // We're also not interested in dupes from parent classes (not all JDK impls)
                if (isInjector && !injectionMemberNames.contains(methodSignature)) {
                    injectionMembers.add(method);
                    injectionMemberNames.add(methodSignature);
                    nameList.add(getName(method));
                    typeList.add(box(parameterTypes[0]));
                    bingingIds.add(getBindings(method, 0));
                }
            }
        }
        injectionTypes = typeList.toArray(new Type[0]);
        bindings = bingingIds.toArray(new Annotation[0]);
        initialized = true;
    }

    public static String crudeMethodSignature(final Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getName());
        sb.append(method.getName());
        for (Class<?> pType : method.getParameterTypes()) {
            sb.append(pType.getName());
        }
        return sb.toString();
    }


    protected String getName(final Method method) {
        return null;
    }

    private void fixGenericParameterTypes(final Method method, final Type[] parameterTypes) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Type parameterType = parameterTypes[i];
            if (parameterType instanceof TypeVariable) {
                parameterTypes[i] = method.getParameterTypes()[i];
            }
        }
    }


    private Annotation getBindings(final Method method, final int i) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterAnnotations.length >= i +1) {
            Annotation[] o = parameterAnnotations[i];
            for (Annotation annotation : o) {
                if (annotation.annotationType().getAnnotation(Bind.class) != null) {
                    return annotation;
                }
            }
            return null;

        }
        //TODO - what's this ?
        if (parameterAnnotations != null) {
            //return ((Bind) method.getAnnotation(Bind.class)).id();
        }
        return null;

    }

    protected boolean isInjectorMethod(final Method method) {
        return false;
    }

    private Method[] getMethods() {
        return  AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
            public Method[] run() {
                return getComponentImplementation().getMethods();
            }
        });
    }


}
