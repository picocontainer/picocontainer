/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.gems.jndi;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.behaviors.Stored;

/**
 * exposes component to JNDI basically does same thing as cached, but uses JNDI
 * reference instead. Maybe Cached shall be refactored? as there is little new
 * functionality.
 * 
 * @author Konstantin Pribluda
 * 
 */
@SuppressWarnings("serial")
public class JNDIExposed<T> extends Stored<T> {


	/**
	 * construct reference itself using vanilla initial context.
	 * JNDI name is stringified component key
	 * @param delegate
	 *            delegate adapter

	 * @throws NamingException
	 */
	public JNDIExposed(final ComponentAdapter<T> delegate) throws NamingException {
		super(delegate, new JNDIObjectReference<Stored.Instance<T>>(delegate.getComponentKey()
				.toString(), new InitialContext()));
	}

	/**
	 * create with provided reference
	 * 
	 * @param delegate
	 * @param instanceReference
	 */
	public JNDIExposed(final ComponentAdapter<T> delegate,
			final JNDIObjectReference<Stored.Instance<T>> instanceReference) {
		super(delegate, instanceReference);
	}

	/**
	 * create adapter with desired name
	 * @param delegate
	 * @param name
	 * @throws NamingException
	 */
	public JNDIExposed(final ComponentAdapter<T> delegate, final String name) throws NamingException {
		super(delegate, new JNDIObjectReference<Stored.Instance<T>>(name, new InitialContext()));
	}
	
	@Override
	public String toString() {
		return "JNDI" + getDelegate().toString();
	}
}
