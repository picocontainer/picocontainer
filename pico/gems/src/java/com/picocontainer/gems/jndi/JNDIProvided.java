/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.gems.jndi;

import java.io.Serializable;
import java.lang.reflect.Type;

import javax.naming.NamingException;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;

/**
 * Represents dependency provided via JNDI. This dependency is not
 * to be managed by container at all, so there is no lifecycle, no
 * monitoring etc.
 * @author Konstantin Pribluda
 *
 */
@SuppressWarnings("serial")
public class JNDIProvided<T> implements ComponentAdapter<T>, Serializable {


    private JNDIObjectReference<T> jndiReference;
    private Class<T> type;
    private Object componentKey;

	Object key;

	/**
	 * Create adapter with specified key and reference
	 * @param key component key
	 * @param reference JNDI reference storing component
     * @param type the type that the JNDIObjectReference will return.
	 */
	public JNDIProvided(final Object key,final JNDIObjectReference<T> reference, final Class<T> type) {
		this.key = key;
		this.jndiReference = reference;
        this.type = type;
	}

	/**
	 * create adapter with JNDI reference. referenced object class will be
	 * takes as key
	 * @param reference JNDI reference storing component
     * @param type the type that the JNDIObjectReference will return.
	 */
	public JNDIProvided(final JNDIObjectReference<T> reference, final Class<T> type) {
		this(reference.get().getClass(),reference, type);
	}

	/**
	 * Create adapter based on JNDI name. I leave this unchecked because
	 * type is really not known at this time
	 * @param jndiName name to be used
     * @param type the type that the JNDIObjectReference will return.
	 * @throws NamingException will be thrown if something goes
	 * wrong in JNDI
	 */
	public JNDIProvided(final String jndiName, final Class<T> type) throws NamingException {
		this(new JNDIObjectReference<T>(jndiName), type);
	}

	public Object getComponentKey() {
		return key;
	}

	public Class<? extends T> getComponentImplementation() {
		return type;
	}

    /**
	 * Retrieve instance out of JNDI
	 */
	public T getComponentInstance(final PicoContainer container, final Type into)
			throws PicoCompositionException {
		return  jndiReference.get();
	}

	/*
	 * We have nothing to verify here
     */
	public void verify(final PicoContainer container) throws PicoCompositionException {
	}

	/*
	 * As there is no puprose of proceeding further down,
	 * we do nothing here
	 */
	public void accept(final PicoVisitor visitor) {
	}

    public ComponentAdapter<T> getDelegate() {
        return null;
    }

    public <U extends ComponentAdapter> U findAdapterOfType(final Class<U> adapterType) {
        return null;
    }

    public String getDescriptor() {
        return "JNDI(" + jndiReference.getName() + ")";
    }

}
