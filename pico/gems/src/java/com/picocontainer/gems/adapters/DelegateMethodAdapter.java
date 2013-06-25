/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Centerline Computers, Inc.                               *
 *****************************************************************************/
package com.picocontainer.gems.adapters;

import java.lang.reflect.Type;

import com.picocontainer.gems.util.DelegateMethod;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;

/**
 * Object construction is sometimes expensive, especially when it is seldom used
 * object. The goal of this adapter is to use the
 * {@link com.picocontainer.gems.util.DelegateMethod} type to allow delayed
 * construction of objects.
 * <p>
 * For example, in a web application, to be able to have classes that depend on
 * the HttpSession object, you would have to call
 * HttpServletRequest.getSession(true). This is fine unless you don't want to
 * create a session until you absolutely have to.
 * </p>
 * <p>
 * Enter DelegateMethodAdapter:
 * </p>
 *
 * <pre>
 * //Assumed Variables: request == HttpServletRequest.
 * //Typical PicoContainer
 * MutablePicoContainer pico = new PicoBuilder().withLifecycle().withCaching()
 * 		.build();
 *
 * //Create a delegate method that will invoke HttpServletRequest.getSession(true) when invoke() is called.
 * DelegateMethod delegateMethod = new DelegateMethod(HttpServletRequest.class,
 * 		&quot;getSession&quot;, true);
 *
 * //Create the Adapter wrapping the delegate method.
 * DelegateMethodAdapter methodAdapter = new DelegateMethodAdapter(
 * 		HttpSession.class, request, delegateMethod);
 * pico.addAdapter(methodAdapter);
 *
 * //If only executing this code, the HttpSession should not be created yet.
 * assertNull(request.getSession(false));
 *
 * //Will get the session object by having the delegate method call HttpServletRequest.getSession(true)
 * HttpSession session = pico.getComponent(HttpSession.class);
 * assertNotNull(session);
 *
 * //Should demonstrate that the session has now been created.
 * assertNotNull(request.getSession(false));
 * </pre>
 *
 * <p>
 * With an adapter like this, you can write classes like:
 * </p>
 *
 * <pre>
 * public class SessionUser {
 * 	public SessionUser(HttpSession session) {
 * 		//.....
 * 	}
 * }
 * </pre>
 *
 * <p>
 * With impunity, and are guaranteed that the session would not be created
 * unless the SessionUser object was constructed.
 * </p>
 *
 * @author Michael Rimov
 */
public class DelegateMethodAdapter<T> implements ComponentAdapter<T> {

	/**
	 * The delegate method instance that will ultimately invoke via reflection some method
	 * on targetInstance.
	 */
	private final DelegateMethod factoryMethod;

	/**
	 * The target instance on which the delegate method's invoke() call will operate.
	 */
	private final Object targetInstance;

	/**
	 * Object key.
	 */
	private final Object key;

	/**
	 * @param key
	 * @param Component
	 *            Implementation will be the expected return type of the factory
	 *            method.
	 */
	public DelegateMethodAdapter(final Object key,
			final Object targetInstance, final DelegateMethod factoryMethod) {
		this.factoryMethod = factoryMethod;
		this.targetInstance = targetInstance;
		this.key = key;
	}

	/**
	 * @param key
	 * @param impl
	 * @param monitor
	 */
	public DelegateMethodAdapter(final Object key,
			final ComponentMonitor monitor, final Object targetInstance,
			final DelegateMethod factoryMethod) {
		this.factoryMethod = factoryMethod;
		this.targetInstance = targetInstance;
		this.key = key;
	}

	/**
	 * Returns the
	 */
	public T getComponentInstance(final PicoContainer container, final Type into)
			throws PicoCompositionException {
		try {
			return (T) factoryMethod.invoke(targetInstance);
		} catch (RuntimeException e) {
			throw new PicoCompositionException(
					"Error invoking delegate for object construction", e);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see com.picocontainer.ComponentAdapter#getDescriptor()
	 */
	public String getDescriptor() {
		return "Delegate Adapter.  Delegate: " + this.factoryMethod.toString();
	}

	/** {@inheritDoc} **/
	public void verify(final PicoContainer container)
			throws PicoCompositionException {
		// Currently does nothing.
	}

	/** {@inheritDoc} **/
	public void accept(final PicoVisitor visitor) {
		visitor.visitComponentAdapter(this);
	}

	/** {@inheritDoc} **/
	public ComponentAdapter<T> findAdapterOfType(final Class adapterType) {
		if (adapterType == null) {
			return null;
		}

		if (DelegateMethodAdapter.class.isAssignableFrom(adapterType)) {
			return this;
		}

		return null;
	}

	/** {@inheritDoc} **/
	@SuppressWarnings("unchecked")
	public Class<? extends T> getComponentImplementation() {
		return this.factoryMethod.getReturnType();
	}

	/** {@inheritDoc} **/
	public Object getComponentKey() {
		return key;
	}

	/**
	 * No delegates.
	 */
	public ComponentAdapter<T> getDelegate() {
		return null;
	}

}
