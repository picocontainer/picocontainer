/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.gems.jndi;

import java.io.Serializable;
import java.lang.reflect.Type;

import javax.naming.NamingException;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

/**
 * represents dependency provided via JNDI. This dependency is not 
 * to be managed by container at all, so there is no lifecycle, no 
 * monitoring etc. 
 * @author Konstantin Pribluda
 *
 */
@SuppressWarnings("serial")
public class JNDIProvided<T> implements ComponentAdapter<T> , Serializable {


	JNDIObjectReference<T> jndiReference;
	
	 Object componentKey;
	
	/**
	 * create adapter with specified key and reference
	 * @param componentKey component key
	 * @param reference JNDI reference storing component
	 */
	public JNDIProvided(final Object componentKey,final JNDIObjectReference<T> reference) {
		this.componentKey = componentKey;
		this.jndiReference = reference;
	}
	
	/**
	 * create adapter with JNDI reference. referenced object class will be 
	 * takes as key
	 * @param reference JNDI reference storing component
	 */
	public JNDIProvided(final JNDIObjectReference<T> reference) {
		this(reference.get().getClass(),reference);
	}
	
	/**
	 * create adapter based on JNDI name. I leave this unchecked because
	 * type is really not known at this time
	 * @param jndiName name to be used
	 * @throws NamingException will be thrown if something goes 
	 * wrong in JNDI
	 */
	@SuppressWarnings("unchecked")
	public JNDIProvided(final String jndiName) throws NamingException {
		this(new JNDIObjectReference(jndiName));
	}
	
	public Object getComponentKey() {	
		return componentKey;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends T> getComponentImplementation() {
		return (Class<? extends T>) jndiReference.get().getClass();
	}

    /**
	 * retrieve instance out of JNDI
	 */
	public T getComponentInstance(final PicoContainer container, final Type into)
			throws PicoCompositionException {
		return  jndiReference.get();
	}

	/**
	 * we have nothing to verify here
	 */
	public void verify(final PicoContainer container) throws PicoCompositionException {
	}

	/**
	 * as there is no puprose of proceeding further down, 
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
