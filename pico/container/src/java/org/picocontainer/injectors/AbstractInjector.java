/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.ObjectReference;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.adapters.AbstractAdapter;
import org.picocontainer.parameters.AccessibleObjectParameterSet;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

import com.googlecode.jtype.Generic;

/**
 * This ComponentAdapter will instantiate a new object for each call to
 * {@link org.picocontainer.ComponentAdapter#getComponentInstance(PicoContainer, Type)}.
 * That means that when used with a PicoContainer, getComponent will
 * return a new object each time.
 *
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public abstract class AbstractInjector<T> extends AbstractAdapter<T> implements org.picocontainer.Injector<T> {

    /** The cycle guard for the verification. */
    protected transient ThreadLocalCyclicDependencyGuard verifyingGuard;
    
    /** The parameters to use for initialization. */
    protected final transient AccessibleObjectParameterSet[] parameters;
    
    /** The strategy used to control the lifecycle */
    private final boolean useNames;

    /**
     * Constructs a new ComponentAdapter for the given key and implementation.
     * @param key the search key for this implementation
     * @param impl the concrete implementation
     * @param monitor the component monitor used by this ComponentAdapter
     * @param parameters the parameters to use for the initialization
     * @throws org.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException if the implementation is not a concrete class
     * @throws NullPointerException if one of the parameters is <code>null</code>
     */
    protected AbstractInjector(final Object key, final Class<?> impl,
                               final ComponentMonitor monitor, final boolean useNames,
                               final AccessibleObjectParameterSet... parameters) {
        super(key, impl, monitor);
        this.useNames = useNames;
        checkConcrete();
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if(parameters[i] == null) {
                    throw new NullPointerException("Parameter " + i + " is null");
                }
                
                if (parameters[i].getParams() != null) {
                	Parameter[] nestedParams = parameters[i].getParams();
	                for (int j= 0; j < nestedParams.length; j++) {
	                	if (nestedParams[j] == null){
	                		throw new NullPointerException("Parameter " + j + " inside " + parameters[i] + " is null" );
	                	}
	                }
                }
            }
        }
        this.parameters = parameters;
    }
    
    
    protected static AccessibleObjectParameterSet[] toAccessibleObjectParameterSetArray(AccessibleObjectParameterSet singleParam) {
    	if (singleParam == null) {
    		return AccessibleObjectParameterSet.EMPTY;
    	}
    	
    	return new AccessibleObjectParameterSet[] {singleParam}; 
    }

    public boolean useNames() {
        return useNames;
    }

    private void checkConcrete() throws NotConcreteRegistrationException {
        // Assert that the component class is concrete.
        boolean isAbstract = (getComponentImplementation().getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT;
        if (getComponentImplementation().isInterface() || isAbstract) {
            throw new NotConcreteRegistrationException(getComponentImplementation());
        }
    }
    
    /**
     * Create default parameters for the given types.
     *
     * @param length parameter list length
     * @return the array with the default parameters.
     */
    protected Parameter[] createDefaultParameters(int length) {
    	Parameter[] componentParameters = new Parameter[length];
        for (int i = 0; i < length; i++) {
            componentParameters[i] = constructDefaultComponentParameter();
        }
        return componentParameters;
    }    
    
    /**
     * Allows Different swapping of types.
     * @return
     */
    protected Parameter constructDefaultComponentParameter() {
    	return ComponentParameter.DEFAULT;
    }
    

    /**
     * Constructs an appropriate {@link org.picocontainer.parameters.AccessibleObjectParameterSet} based on the type 
     * of {@link java.lang.reflect.AccessibleObject} sent.  If params are null or zero length then default parameter is used.
     * @param object
     * @param params
     * @return
     */
    protected AccessibleObjectParameterSet constructAccessibleObjectParameterSet(AccessibleObject object, Parameter... params) {
    	Parameter[] paramsToUse = params;
    	if (paramsToUse == null || paramsToUse.length == 0) {
    		paramsToUse = createDefaultParamsBasedOnTypeOfAccessibleObject(object);
    	}
    	
    	if (object instanceof Constructor) {
    		return new ConstructorParameters(paramsToUse);
    	} else if (object instanceof Field) {
    		return new FieldParameters( ((Field)object).getDeclaringClass(),  ((Field) object).getName(), paramsToUse);
    	} else if (object instanceof Method) {
    		return new MethodParameters( ((Method)object).getDeclaringClass(),  ((Method) object).getName(), paramsToUse);    		
    	} 
    	throw new IllegalArgumentException("Object " + object + " doesn't appear to be a constructor, a field, or a method.  Don't know how to proceed.");
    }

    
    
    @SuppressWarnings("rawtypes")
	private Parameter[] createDefaultParamsBasedOnTypeOfAccessibleObject(AccessibleObject object) {
    	if (object instanceof Constructor) {
    		return createDefaultParameters( ((Constructor)object).getParameterTypes().length );
    	}

    	if (object instanceof Field) {
    		return createDefaultParameters(1);
    	}
    	
    	if (object instanceof Method) {
    		return createDefaultParameters( ((Method)object).getParameterTypes().length );
    	}

    	throw new IllegalArgumentException("Object " + object + " doesn't appear to be a constructor, a field, or a method.  Don't know how to proceed.");
    }


	/**
     * @param targetInjectionMember
     * @param assignedParameters
     * @return null if no parameter set for the given accessible object has been defined.
     */
    protected AccessibleObjectParameterSet getParameterToUseForObject(AccessibleObject targetInjectionMember, AccessibleObjectParameterSet... assignedParameters) {
    	if (assignedParameters == null || assignedParameters.length == 0) {
    		return null;
    	}
    	
    	
    	for (AccessibleObjectParameterSet eachParameter : assignedParameters) {
    		
    		//if a target type is defined then we have to match against it. 
    		//This allows injection into private members of base classes of the same name
    		//as the subclasses.
    		Class<?> targetType = eachParameter.getTargetType();
    		if (targetType != null && !targetType.equals(getAccessibleObjectDefiningType(targetInjectionMember))) {
    			continue;
    		}
    		
    		if (eachParameter.getName().equals(getAccessibleObjectName(targetInjectionMember))) {
    			return eachParameter;
    		}
    	}
    	
    	return null;
	}

    
    /**
     * Retrieves the enclosing class of the accessible object. (Constructor, Method, and Field all 
     * supply the method "getDeclaringClass()", yet it isn't supplied in the AccessibleObject base class.
     * @param targetAccessibleObject
     * @return the enclosing type of the accessible object.
     */
    protected Class<?> getAccessibleObjectDefiningType(AccessibleObject targetAccessibleObject) {
    	if (targetAccessibleObject == null) {
    		throw new NullPointerException("targetAccessibleObject");
    	}

    	try {
			Method declaringClassMethod = targetAccessibleObject.getClass().getMethod("getDeclaringClass");
			return (Class<?>) declaringClassMethod.invoke(targetAccessibleObject);
		} catch (NoSuchMethodException e) {
			throw new PicoCompositionException("Target Type '" + targetAccessibleObject.getClass() 
					+ "' does not appear to suppot getDeclaringClass().  Please override getAccessibleObjectDefiningType() for your type of injection", e);
		} catch (IllegalAccessException e) {
			throw new PicoCompositionException("Error invoking 'getDeclaringClass()' in type " 
		+ targetAccessibleObject.getClass(), e);
		} catch (InvocationTargetException e) {
			throw new PicoCompositionException("Error invoking 'getDeclaringClass()' in type " 
		+ targetAccessibleObject.getClass(), e);
		}
    	
    }   
    
    /**
     * Retrieves the name of the accessible object or null if it doesn't have one (such as a constructor)
     * @param targetAccessibleObject
     * @return 
     */
    public String getAccessibleObjectName(final AccessibleObject targetAccessibleObject) {
    	if (targetAccessibleObject == null) {
    		throw new NullPointerException("targetAccessibleObject");
    	}
    	
    	if (targetAccessibleObject instanceof Constructor) {
    		return null;
    	}
    	
    	try {
			Method declaringClassMethod = targetAccessibleObject.getClass().getMethod("getName");
			return (String) declaringClassMethod.invoke(targetAccessibleObject);
		} catch (NoSuchMethodException e) {
			throw new PicoCompositionException("Target Type '" + targetAccessibleObject.getClass() 
					+ "' does not appear to suppot getName().  Please override getAccessibleObjectDefiningType() for your type of injection", e);
		} catch (IllegalAccessException e) {
			throw new PicoCompositionException("Error invoking 'getName()' in type " 
		+ targetAccessibleObject.getClass(), e);
		} catch (InvocationTargetException e) {
			throw new PicoCompositionException("Error invoking 'getName()' in type " 
		+ targetAccessibleObject.getClass(), e);
		}
    }
    
    public void verify(PicoContainer container) throws PicoCompositionException {
    }

    public abstract T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException;

    public Object decorateComponentInstance(PicoContainer container, Type into, T instance) {
        return null;
    }

    @Override
	public void accept(final PicoVisitor visitor) {
        super.accept(visitor);
        if (parameters != null) {
            for (AccessibleObjectParameterSet parameter : parameters) {
            	if (parameter.getParams() != null) {
	            	for (Parameter eachContainedParameter : parameter.getParams()) {
	            		eachContainedParameter.accept(visitor);
	            	}
            	}
            }
        }
    }


    public String getDescriptor() {
        return "Asbtract Injector";
    }

    /**
     * Instantiate an object with given parameters and respect the accessible flag.
     *
     * @param constructor the constructor to use
     * @param parameters the parameters for the constructor
     * @return the new object.
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    protected T newInstance(final Constructor<T> constructor, final Object[] parameters)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        try {
			return constructor.newInstance(parameters);
		} catch (IllegalArgumentException e) {
			//Chain it with the calling parameters to give us some real information.
			throw new IllegalArgumentException("Type mismatch calling constructor " 
						+ constructor 
						+ " with parameters " 
						+ Arrays.deepToString(parameters), e);
		}
    }
    /**
     * inform monitor about component instantiation failure
     * @param monitor
     * @param constructor
     * @param e
     * @param container
     * @return
     */
    protected T caughtInstantiationException(final ComponentMonitor monitor,
                                                final Constructor<T> constructor,
                                                final InstantiationException e, final PicoContainer container) {
        // can't get here because checkConcrete() will catch it earlier, but see PICO-191
        monitor.instantiationFailed(container, this, constructor, e);
        throw new PicoCompositionException("Should never get here");
    }

    /**
     * inform monitor about access exception.
     * @param monitor
     * @param constructor
     * @param e
     * @param container
     * @return
     */
    protected T caughtIllegalAccessException(final ComponentMonitor monitor,
                                                final Constructor<T> constructor,
                                                final IllegalAccessException e, final PicoContainer container) {
        // can't get here because either filtered or access mode set
        monitor.instantiationFailed(container, this, constructor, e);
        throw new PicoCompositionException(e);
    }

    /**
     * inform monitor about exception while instantiating component
     * @param monitor
     * @param member
     * @param componentInstance
     * @param e
     * @return 
     */
    protected T caughtInvocationTargetException(final ComponentMonitor monitor,
                                                   final Member member,
                                                   final Object componentInstance, final InvocationTargetException e) {
        monitor.invocationFailed(member, componentInstance, e);
        if (e.getTargetException() instanceof RuntimeException) {
            throw (RuntimeException) e.getTargetException();
        } else if (e.getTargetException() instanceof Error) {
            throw (Error) e.getTargetException();
        }
        throw new PicoCompositionException(e.getTargetException());
    }

    protected T caughtIllegalAccessException(final ComponentMonitor monitor,
                                                final Member member,
                                                final Object componentInstance, final IllegalAccessException e) {
        monitor.invocationFailed(member, componentInstance, e);
        throw new PicoCompositionException("Illegal Access Exception for Injector " 
        		+ this.getDescriptor() 
        		+ " and target member " + member != null ? member.toString() : " null",e);
    }

    @SuppressWarnings("rawtypes")
	protected Type box(Type parameterType) {
        if (parameterType instanceof Class && ((Class) parameterType).isPrimitive()) {
            String parameterTypeName = ((Class) parameterType).getName();
            if (parameterTypeName == "int") {
                return Integer.class;
            } else if (parameterTypeName == "boolean") {
                return Boolean.class;
            } else if (parameterTypeName == "long") {
                return Long.class;
            } else if (parameterTypeName == "float") {
                return Float.class;
            } else if (parameterTypeName == "double") {
                return Double.class;
            } else if (parameterTypeName == "char") {
                return Character.class;
            } else if (parameterTypeName == "byte") {
                return Byte.class;
            } else if (parameterTypeName == "short") {
                return Short.class;
            }
        }
        return parameterType;
    }

    /**
     * Abstract utility class to detect recursion cycles.
     * Derive from this class and implement {@link ThreadLocalCyclicDependencyGuard#run}.
     * The method will be called by  {@link ThreadLocalCyclicDependencyGuard#observe}. Select
     * an appropriate guard for your scope. Any {@link ObjectReference} can be
     * used as long as it is initialized with  <code>Boolean.FALSE</code>.
     *
     * @author J&ouml;rg Schaible
     */
    static abstract class ThreadLocalCyclicDependencyGuard<T> extends ThreadLocal<Boolean> {

        protected PicoContainer guardedContainer;

        @Override
		protected Boolean initialValue() {
            return Boolean.FALSE;
        }

        /**
         * Derive from this class and implement this function with the functionality
         * to observe for a dependency cycle.
         *
         * @return a value, if the functionality result in an expression,
         *      otherwise just return <code>null</code>
         */
        public abstract T run(Object instance);

        /**
         * Call the observing function. The provided guard will hold the {@link Boolean} value.
         * If the guard is already <code>Boolean.TRUE</code> a {@link CyclicDependencyException}
         * will be  thrown.
         *
         * @param stackFrame the current stack frame
         * @return the result of the <code>run</code> method
         */
        public final T observe(final Class<?> stackFrame, final Object instance) {
            if (Boolean.TRUE.equals(get())) {
                throw new CyclicDependencyException(stackFrame);
            }
            T result = null;
            try {
                set(Boolean.TRUE);
                result = run(instance);
            } catch (final CyclicDependencyException e) {
                e.push(stackFrame);
                throw e;
            } finally {
                set(Boolean.FALSE);
            }
            return result;
        }

        public void setGuardedContainer(final PicoContainer container) {
            this.guardedContainer = container;
        }

    }

	public static class CyclicDependencyException extends PicoCompositionException {
        private final List<Class> stack;

        /**
         * @param element
         */
        public CyclicDependencyException(final Class<?> element) {
            super((Throwable)null);
            this.stack = new LinkedList<Class>();
            push(element);
        }

        /**
         * @param element
         */
        public void push(final Class<?> element) {
            stack.add(element);
        }

        public Class[] getDependencies() {
            return stack.toArray(new Class[stack.size()]);
        }

        @Override
		public String getMessage() {
            return "Cyclic dependency: " + stack.toString();
        }
    }

    /**
     * Exception that is thrown as part of the introspection. Raised if a PicoContainer cannot resolve a
     * type dependency because the registered {@link org.picocontainer.ComponentAdapter}s are not
     * distinct.
     *
     * @author Paul Hammant
     * @author Aslak Helles&oslash;y
     * @author Jon Tirs&eacute;n
     */
    public static final class AmbiguousComponentResolutionException extends PicoCompositionException {


		private Class<?> component;
        private final Generic<?> ambiguousDependency;
        private final Object[] ambiguousComponentKeys;
		private AccessibleObject accessibleObject;
		
		/**
		 * Zero-based parameter #
		 */
		private int parameterNumber = -1;
		


        /**
         * Construct a new exception with the ambigous class type and the ambiguous component keys.
         *
         * @param ambiguousDependency the unresolved dependency type
         * @param keys the ambiguous keys.
         */
        public AmbiguousComponentResolutionException(final Generic<?> ambiguousDependency, final Object[] keys) {
            super("");
            this.ambiguousDependency = ambiguousDependency;
            this.ambiguousComponentKeys = new Object[keys.length];
            System.arraycopy(keys, 0, ambiguousComponentKeys, 0, keys.length);
        }

        /**
         * @return Returns a string containing the unresolved class type and the ambiguous keys.
         */
        @Override
		public String getMessage() {
            StringBuffer msg = new StringBuffer();
            msg.append(component != null ? component : "<not-specified>");
            msg.append(" needs a '");
            msg.append(ambiguousDependency.toString());
            msg.append("' injected");
            if (parameterNumber > -1) {
            	msg.append(" into parameter #" + parameterNumber);
            	msg.append(" (zero based index) of");
            } 
            
            if (parameterNumber == -1 && accessibleObject != null) {
            	msg.append(" into");            	
            }
            
            if (accessibleObject != null) {
            	if (accessibleObject instanceof Field) {
            		msg.append(" field '");
            	} else if (accessibleObject instanceof Constructor) {
            		msg.append(" constructor '");
            	} else if (accessibleObject instanceof Method) {
            		msg.append(" method '");
            	} else {
            		msg.append(" '");
            	}
            	msg.append(accessibleObject);
            } else {
            	msg.append(" through : <unknown>");
            }
            //msg.append(accessibleObject != null ? accessibleObject : "<unknown>");
            msg.append("', but there are too many choices to inject. These:");
            msg.append(Arrays.asList(getAmbiguousComponentKeys()));
            msg.append(", refer http://picocontainer.org/ambiguous-injectable-help.html");
            return msg.toString();
        }

        /**
         * @return Returns the ambiguous component keys as array.
         */
        public Object[] getAmbiguousComponentKeys() {
            return ambiguousComponentKeys;
        }

        public void setComponent(final Class<?> component) {
            this.component = component;
        }
        
        public void setMember(AccessibleObject accessibleObject) {
            this.accessibleObject = accessibleObject;
        }

		public void setParameterNumber(int parameterNumber) {
			this.parameterNumber = parameterNumber;
		}        
    }

    /**
     * Exception thrown when some of the component's dependencies are not satisfiable.
     *
     * @author Aslak Helles&oslash;y
     * @author Mauro Talevi
     */
    public static class UnsatisfiableDependenciesException extends PicoCompositionException {

        public UnsatisfiableDependenciesException(String message) {
        	super(message);
        }
    }

    /**
     * @author Aslak Hellesoy
     */
    public static class NotConcreteRegistrationException extends PicoCompositionException {
		
		private final Class<?> impl;

        public NotConcreteRegistrationException(final Class<?> impl) {
            super("Bad Access: '" + impl.getName() + "' is not instantiable");
            this.impl = impl;
        }

        public Class<?> getComponentImplementation() {
            return impl;
        }
    }
}
