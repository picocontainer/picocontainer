/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file. 
 ******************************************************************************/
package org.picocontainer.parameters;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

/**
 * Once in a great while, you actually want to pass in 'null' as an argument.  Instead
 * of bypassing the type checking mechanisms available in 
 * {@link org.picocontainer.parameters.ConstantParameter ConstantParameter}, we provide a  <em>Special Type</em>
 * geared to marking nulls.
 * @author Michael Rimov
 *
 */
@SuppressWarnings("serial")
public class NullParameter extends AbstractParameter implements Serializable {

	/**
	 * The one and only instance of null parameter.
	 */
	public static final NullParameter INSTANCE = new NullParameter();
	
	/**
	 * Only once instance of Null parameter needed.
	 */
	protected NullParameter() {
	}

	/**
	 * {@inheritDoc}
	 * @see org.picocontainer.Parameter#accept(org.picocontainer.PicoVisitor)
	 */
	public void accept(PicoVisitor visitor) {
		visitor.visitParameter(this);
	}

	/**
	 * {@inheritDoc}
	 * @see org.picocontainer.Parameter#resolve(org.picocontainer.PicoContainer, org.picocontainer.ComponentAdapter, org.picocontainer.ComponentAdapter, java.lang.reflect.Type, org.picocontainer.NameBinding, boolean, java.lang.annotation.Annotation)
	 */
	public Resolver resolve(PicoContainer container,
			ComponentAdapter<?> forAdapter,
			ComponentAdapter<?> injecteeAdapter, Type expectedType,
			NameBinding expectedNameBinding, boolean useNames,
			Annotation binding) {
		return new ValueResolver(isAssignable(expectedType), null, null);
	}

	/**
	 * {@inheritDoc}
	 * @see org.picocontainer.Parameter#verify(org.picocontainer.PicoContainer, org.picocontainer.ComponentAdapter, java.lang.reflect.Type, org.picocontainer.NameBinding, boolean, java.lang.annotation.Annotation)
	 */
	public void verify(PicoContainer container, ComponentAdapter<?> adapter,
			Type expectedType, NameBinding expectedNameBinding,
			boolean useNames, Annotation binding) {
		if (!isAssignable(expectedType)) {
			throw new PicoCompositionException(expectedType + " cannot be assigned a null value");
		}
	}
	
	/**
	 * Nulls cannot be assigned to primitives.
	 * @param expectedType
	 * @return
	 */
    protected boolean isAssignable(Type expectedType) {
        if (expectedType instanceof Class<?>) {
            Class<?> expectedClass = Class.class.cast(expectedType);
            if (expectedClass.isPrimitive()) {
                return false;
            }
        }
        return true;
    }
	
	
}
