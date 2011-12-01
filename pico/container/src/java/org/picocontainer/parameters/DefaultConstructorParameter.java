/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file. 
 ******************************************************************************/
package org.picocontainer.parameters;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

/**
 * Part of the replacement construct for Parameter.ZERO
 * @since PicoContainer 2.8
 */
@SuppressWarnings("serial")
public final class DefaultConstructorParameter extends AbstractParameter implements Parameter, Serializable {

	/**
	 * The one and only instance
	 */
	public static final DefaultConstructorParameter INSTANCE = new DefaultConstructorParameter();
	
	/**
	 * No instantiation
	 */
	public void accept(PicoVisitor visitor) {
		visitor.visitParameter(this);
	}

	public Resolver resolve(PicoContainer container,
                            ComponentAdapter<?> forAdapter, ComponentAdapter<?> injecteeAdapter, Type expectedType,
                            NameBinding expectedNameBinding, boolean useNames,
                            Annotation binding) {
		return new Parameter.NotResolved();
	}

	public void verify(PicoContainer container,
			ComponentAdapter<?> adapter, Type expectedType,
			NameBinding expectedNameBinding, boolean useNames,
			Annotation binding) {
		
		if (!(expectedType instanceof Class)) {
			throw new ClassCastException("Unable to use except for class types.  Offending type: " + expectedType);
		}
		
		Class<?> type = (Class<?>)expectedType;
		try {
			Constructor constructor = type.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("No default constructor for type " + expectedType,e);
		}	
	}

    @Override
	public String toString() {
		return "Force Default Constructor Parameter";
	}
	
	/**
	 * Returns true if the object object is a DEFAULT_CONSTRUCTOR object.
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		
		return (other.getClass().getName()).equals(getClass().getName());
	}
}
