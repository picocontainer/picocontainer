/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Jon Tirsen                        *
 *****************************************************************************/

package com.picocontainer.parameters;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.NameBinding;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoException;
import com.picocontainer.PicoVisitor;


/**
 * A ConstantParameter should be used to pass in "constant" arguments to constructors. This
 * includes {@link String}s,{@link Integer}s or any other object that is not registered in
 * the container.
 *
 * @author Jon Tirs&eacute;n
 * @author Aslak Helles&oslash;y
 * @author J&ouml;rg Schaible
 * @author Thomas Heller
 */
@SuppressWarnings("serial")
public class ConstantParameter extends AbstractParameter implements Parameter, Serializable {

    private final Object value;


    public ConstantParameter(final Object value) {
		this.value = value;

    }

    public Resolver resolve(final PicoContainer container, final ComponentAdapter<?> forAdapter,
                            final ComponentAdapter<?> injecteeAdapter, final Type expectedType, final NameBinding expectedNameBinding,
                            final boolean useNames, final Annotation binding) {
        if (expectedType instanceof Class) {
            return new Parameter.ValueResolver(isAssignable(expectedType), value, null);
        } else if (expectedType instanceof ParameterizedType) {
        	return new Parameter.ValueResolver(isAssignable(((ParameterizedType)expectedType).getRawType()), value, null);
        }
        return new Parameter.ValueResolver(true, value, null);
    }

    /**
     * {@inheritDoc}
     *
     * @see Parameter#verify(com.picocontainer.PicoContainer,com.picocontainer.ComponentAdapter,java.lang.reflect.Type,com.picocontainer.NameBinding,boolean,java.lang.annotation.Annotation)
     */
    public void verify(final PicoContainer container, final ComponentAdapter<?> adapter,
                       final Type expectedType, final NameBinding expectedNameBinding,
                       final boolean useNames, final Annotation binding) throws PicoException {
        if (!isAssignable(expectedType)) {
            throw new PicoCompositionException(
                expectedType + " is not assignable from " +
                        (value != null ? value.getClass().getName() : "null"));
        }
    }

    protected boolean isAssignable(final Type expectedType) {
        if (expectedType instanceof Class) {
            Class<?> expectedClass = (Class<?>) expectedType;
            if (checkPrimitive(expectedClass) || expectedClass.isInstance(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Visit the current {@link Parameter}.
     *
     * @see com.picocontainer.Parameter#accept(com.picocontainer.PicoVisitor)
     */
    public void accept(final PicoVisitor visitor) {
        visitor.visitParameter(this);
    }

    private boolean checkPrimitive(final Class<?> expectedType) {
        try {
            if (expectedType.isPrimitive()) {
                final Field field = value.getClass().getField("TYPE");
                final Class<?> type = (Class<?>) field.get(value);
                return expectedType.isAssignableFrom(type);
            }
        } catch (NoSuchFieldException e) {
            //ignore
        } catch (IllegalAccessException e) {
            //ignore
        }
        return false;
    }


	public Object getValue() {
		return value;
	}

}
