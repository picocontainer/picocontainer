/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.gems.jndi;

import java.io.IOException;
import java.io.Serializable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import com.picocontainer.ObjectReference;
import com.picocontainer.PicoCompositionException;

/**
 * object reference to store and retrieve objects from JNDI
 *
 * @author ko5tik
 *
 */
@SuppressWarnings("serial")
public class JNDIObjectReference<T> implements ObjectReference<T> , Serializable{


	String name;

	transient Context context;

	public JNDIObjectReference(final String name, final Context ctx) {
		super();
		this.name = name;
		this.context = ctx;
	}

	public JNDIObjectReference(final String jndiName) throws NamingException {
		this(jndiName,new InitialContext());
	}

	/**
	 * retrieve object from JNDI if possible
	 */
	public T get() {
		try {
			return (T) context.lookup(name);
		} catch(NameNotFoundException e) {
			// this is not error, but normal situation - nothing
			// was stored yet
			return null;
		} catch (NamingException e) {
			throw new PicoCompositionException("unable to resolve jndi name:"
					+ name, e);
		}
	}

	/**
	 * store object in JNDI under specified name
	 */
	public void set(final T item) {
		try {
			if (item == null) {
				context.unbind(name);
			} else {

				Context ctx = context;
				Name n = ctx.getNameParser("").parse(name);
				while (n.size() > 1) {
					String ctxName = n.get(0);
					try {
						ctx = (Context) ctx.lookup(ctxName);
					} catch (NameNotFoundException e) {
						ctx = ctx.createSubcontext(ctxName);
					}
					n = n.getSuffix(1);
				}
				// unbind name just in case
				try {
					if (ctx.lookup(n) != null) {
						ctx.unbind(n);
					}
				} catch (NameNotFoundException e) {
					// that's ok
				}
				ctx.bind(n, item);
			}
		} catch (NamingException e) {
			throw new PicoCompositionException("unable to bind to  jndi name:"
					+ name, e);
		}
	}

	/**
	 * name of this reference
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * here we try to capture (eventual) deserealisation of this reference by
	 * some container (notably JBoss)  and restore context as initial context
	 * I hope this will be sufficient for most puproses
	 *
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final java.io.ObjectInputStream in)throws IOException, ClassNotFoundException {
		try {
			context = new InitialContext();
		} catch (NamingException e) {
			throw new IOException("unable to create initial context");
		}
		in.defaultReadObject();
	}


	@Override
	public String toString() {
		return "(" + getName() + ")";
	}
}
