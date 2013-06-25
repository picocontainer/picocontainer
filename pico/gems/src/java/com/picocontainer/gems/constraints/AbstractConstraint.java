/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/

package com.picocontainer.gems.constraints;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Map;


import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.NameBinding;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.injectors.AbstractInjector;
import com.picocontainer.parameters.CollectionComponentParameter;

/**
 * Base class for parameter constraints.
 *
 * @author Nick Sieger
 */
public abstract class AbstractConstraint extends CollectionComponentParameter implements Constraint {

    /** Construct an AbstractContraint. */
    protected AbstractConstraint() {
        super(false);
    }

    @Override
	public Resolver resolve(final PicoContainer container,
                         final ComponentAdapter<?> forAdapter,
                         final ComponentAdapter<?> injecteeAdapter, final Type expectedType,
                         final NameBinding expectedNameBinding, final boolean useNames,
                         final Annotation binding) throws PicoCompositionException {
        final Resolver resolver;
        return new Parameter.DelegateResolver(super.resolve(container, forAdapter,
                null, getArrayType((Class) expectedType), expectedNameBinding, useNames, binding)) {
            @Override
            public Object resolveInstance(final Type into) {
                final Object[] array = (Object[]) super.resolveInstance(into);
                if (array.length == 1) {
                    return array[0];
                }
                return null;
            }
        };
    }

    @Override
	public void verify(final PicoContainer container,
                       final ComponentAdapter<?> adapter,
                       final Type expectedType,
                       final NameBinding expectedNameBinding, final boolean useNames, final Annotation binding) throws PicoCompositionException {
        super.verify(container, adapter, getArrayType((Class) expectedType), expectedNameBinding, useNames, binding);
    }

    @Override
	public abstract boolean evaluate(ComponentAdapter adapter);

    @Override
	protected Map<Object, ComponentAdapter<?>> getMatchingComponentAdapters(final PicoContainer container,
                                                                            final ComponentAdapter adapter,
                                                                            final Class keyType,
                                                                            final Generic<?> valueType) {
        final Map<Object, ComponentAdapter<?>> map =
            super.getMatchingComponentAdapters(container, adapter, keyType, valueType);
        if (map.size() > 1) {
            throw new AbstractInjector.AmbiguousComponentResolutionException(valueType, map.keySet().toArray(new Object[map.size()]));
        }
        return map;
    }

    private Type getArrayType(final Class expectedType) {
        return Array.newInstance(expectedType, 0).getClass();
    }
}
