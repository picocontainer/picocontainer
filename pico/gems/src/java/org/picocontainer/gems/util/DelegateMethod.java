/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Centerline Computers, Inc.                               *
 *****************************************************************************/
package org.picocontainer.gems.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * The DelegateMethod class has been designed in the hope of providing easier
 * access to methods invoked via reflection. Sample:
 * 
 * <pre>
 * //Sample Map
 * HashMap&lt;String, String&gt; testMap = new HashMap&lt;String, String&gt;();
 * testMap.put(&quot;a&quot;, &quot;A&quot;);
 * 
 * //Create delegate method that calls the 'clear' method for HashMap.
 * DelegateMethod&lt;Map, Void&gt; method = new DelegateMethod&lt;Map, Void&gt;(Map.class,
 * 		&quot;clear&quot;);
 * 
 * //Invokes clear() on the HashMap.
 * method.invoke(testMap);
 * </pre>
 * 
 * <p>
 * Good uses of this object are for lazy invocation of a method and integrating
 * reflection with a vistor pattern.
 * </p>
 * 
 * @author Michael Rimov
 */
public class DelegateMethod<TARGET_TYPE, RETURN_TYPE> {

	/**
	 * Arguments for the method invocation.
	 */
	private final Object[] args;

	/**
	 * The method to be invoked.
	 */
	private final Method method;

	/**
	 * Constructs a delegate method object that will invoke method
	 * <em>methodName</em> on class <em>type</em> with the parameters
	 * specified. The object automatically searches for a suitable object to be
	 * invoked.
	 * <p>
	 * Note that this version simply grabs the
	 * <em>first<em> method that fits the parameter criteria with
	 * the specific name.  You may need to be careful if use extensive overloading.</p>
	 * <p>To specify the exact types in the method. 
	 * @param type the class of the object that should be invoked.
	 * @param methodName the name of the method that will be invoked.
	 * @param parameters the parameters to be used.
	 * @throws NoSuchMethodRuntimeException if the method is not found or parameters that match cannot be found.
	 */
	public DelegateMethod(final Class<TARGET_TYPE> type,
			final String methodName, final Object... parameters)
			throws NoSuchMethodRuntimeException {
		this.args = parameters;
		this.method = findMatchingMethod(type.getMethods(), methodName,
				parameters);

		if (method == null) {
			throw new NoSuchMethodRuntimeException("Could not find method "
					+ methodName + " in type " + type.getName());
		}
	}

	/**
	 * Constructs a DelegateMethod object with very specific argument types.
	 * 
	 * @param type
	 *            the type of the class to be examined for reflection.
	 * @param methodName
	 *            the name of the method to be invoked.
	 * @param paramTypes
	 *            specific parameter types for the method to be found.
	 * @param parameters
	 *            the parameters for method invocation.
	 * @throws NoSuchMethodRuntimeException
	 *             if the method is not found.
	 */
	public DelegateMethod(final Class<?> type, final String methodName,
			final Class<?>[] paramTypes, final Object... parameters)
			throws NoSuchMethodRuntimeException {
		this.args = parameters;
		try {
			this.method = type.getMethod(methodName, paramTypes);
		} catch (NoSuchMethodException e) {
			throw new NoSuchMethodRuntimeException("Could not find method "
					+ methodName + " in type " + type.getName());
		}
	}

	/**
	 * Constructs a method delegate with an explicit Method object.
	 * 
	 * @param targetMethod
	 * @param parameters
	 */
	public DelegateMethod(final Method targetMethod, final Object... parameters) {
		this.args = parameters;
		this.method = targetMethod;
	}

	/**
	 * Locates a method that fits the given parameter types.
	 * 
	 * @param methods
	 * @param methodName
	 * @param parameters
	 * @return
	 */
	private Method findMatchingMethod(final Method[] methods,
			final String methodName, final Object[] parameters) {

		// Get parameter types.
		Class<?>[] paramTypes = new Class[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] == null) {
				paramTypes[i] = NullType.class;
			} else {
				paramTypes[i] = parameters[i].getClass();
			}
		}

		for (Method eachMethod : methods) {
			if (eachMethod.getName().equals(methodName)) {
				if (isPotentialMatchingArguments(eachMethod, paramTypes)) {
					return eachMethod;
				}
			}
		}

		return null;
	}

	/**
	 * Returns true if all parameter types are assignable to the argument type.
	 * 
	 * @param eachMethod
	 *            the method we're checking.
	 * @param paramTypes
	 *            the parameter types provided as constructor arguments.
	 * @return true if the given method is a match given the parameter types.
	 */
	private boolean isPotentialMatchingArguments(final Method eachMethod,
			final Class<?>[] paramTypes) {
		Class<?>[] argParameters = eachMethod.getParameterTypes();
		if (argParameters.length != paramTypes.length) {
			return false;
		}

		for (int i = 0; i < paramTypes.length; i++) {
			if (paramTypes[i].getName().equals(NullType.class.getName())) {
				// Nulls are allowed for any parameter.
				continue;
			}

			if (!argParameters[i].isAssignableFrom(paramTypes[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Used for invoking static methods on the type passed into the constructor.
	 * 
	 * @return the result of the invocation. May be null if the return type is
	 *         void.
	 * @throws IllegalArgumentException
	 *             if the method being invoked is not static.
	 * @throws IllegalAccessRuntimeException
	 *             if the method being invoked is not public.
	 * @throws InvocationTargetRuntimeException
	 *             if an exception is thrown within the method being invoked.
	 */
	public RETURN_TYPE invoke() throws IllegalArgumentException,
			IllegalAccessRuntimeException, InvocationTargetRuntimeException {
		if (!Modifier.isStatic(method.getModifiers())) {
			throw new IllegalArgumentException("Method "
					+ method.toGenericString()
					+ " is not static.  Use invoke(Object) instead.");
		}

		return invoke(null);
	}

	@SuppressWarnings("unchecked")
	private RETURN_TYPE cast(final Object objectToCast) {
		return (RETURN_TYPE) objectToCast;
	}

	/**
	 * Invokes the method specified in the constructor against the target
	 * specified.
	 * 
	 * @param <V>
	 *            a subclass of the type specified by the object declaration.
	 *            This allows Map delegates to operate on HashMaps etc.
	 * @param target
	 *            the target object instance to be operated upon. Unless
	 *            invoking a static method, this should not be null.
	 * @return the result of the invocation. May be null if the return type is
	 *         void.
	 * @throws IllegalArgumentException
	 *             if the method being invoked is not static and parameter
	 *             target null.
	 * @throws IllegalAccessRuntimeException
	 *             if the method being invoked is not public.
	 * @throws InvocationTargetRuntimeException
	 *             if an exception is thrown within the method being invoked.
	 */
	public <V extends TARGET_TYPE> RETURN_TYPE invoke(final V target)
			throws IllegalAccessRuntimeException,
			InvocationTargetRuntimeException {
		assert args != null;

		if (!Modifier.isStatic(method.getModifiers()) && target == null) {
			throw new IllegalArgumentException("Method "
					+ method.toGenericString()
					+ " is not static.  Use invoke(Object) instead.");
		}

		RETURN_TYPE result;
		try {
			result = cast(method.invoke(target, args));
		} catch (IllegalAccessException e) {
			throw new IllegalAccessRuntimeException("Method "
					+ method.toGenericString() + " is not public.", e);
		} catch (InvocationTargetException e) {
			// Unwrap the exception. Should save confusing duplicate traces.
			throw new InvocationTargetRuntimeException(
					"There was an error invoking " + method.toGenericString(),
					e.getCause());
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DelegateMethod " + method.toGenericString()
				+ " with arguments: " + Arrays.deepToString(args);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(args);
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DelegateMethod other = (DelegateMethod) obj;
		if (!Arrays.equals(args, other.args)) {
			return false;
		}
		if (method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!method.equals(other.method)) {
			return false;
		}
		return true;
	}

	/**
	 * Retrieves the expected return type of the delegate method.
	 * @return
	 */
	public Class<?> getReturnType() {
		return method.getReturnType();
	}
	
	/**
	 * Placeholder type used for comparing null parameter values.
	 * 
	 * @author Michael Rimov
	 */
	private static final class NullType {

		/**
		 * This type should never be constructed.
		 */
		private NullType() {

		}
	}

}
