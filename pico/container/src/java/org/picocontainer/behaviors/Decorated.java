/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoCompositionException;

import java.lang.reflect.Type;

@SuppressWarnings("serial")
public class Decorated<T> extends AbstractBehaved<T> {
    private final Decorator decorator;


    public Decorated(ComponentAdapter<T> delegate, Decorator decorator) {
        super(delegate);
        this.decorator = decorator;
    }

    public T getComponentInstance(final PicoContainer container, Type into)
            throws PicoCompositionException {
        T instance = super.getComponentInstance(container, into);
        decorator.decorate(instance);
        return instance;
    }


    public String getDescriptor() {
        return "FieldDecorated";
    }

    interface Decorator {

        void decorate(Object instance);


    }

}
