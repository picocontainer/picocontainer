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
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.ObjectReference;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.ComponentLifecycle;

import java.lang.reflect.Type;
import java.io.Serializable;

/**
 * behaviour for allows components to be guarded by another component
 *
 * @author Paul Hammant
 * @param <T>
 */
@SuppressWarnings("serial")
public class Guarded<T> extends AbstractBehavior<T> {
    private final String guard;

    public Guarded(ComponentAdapter delegate, String guard) {
        super(delegate);
        this.guard = guard;
    }

    public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
        container.getComponent(guard);
        return super.getComponentInstance(container, into);
    }

    public String getDescriptor() {
        return "Guarded(with " + guard + ")";
    }


}