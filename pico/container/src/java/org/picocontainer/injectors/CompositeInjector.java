/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.Injector;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

import java.lang.reflect.Type;

@SuppressWarnings("serial")
public class CompositeInjector<T> extends AbstractInjector<T> {

    private final Injector<T>[] injectors;

    public CompositeInjector(Object componentKey, Class<?> componentImplementation, Parameter[] parameters, ComponentMonitor monitor,
                             boolean useNames, Injector... injectors) {
        super(componentKey, componentImplementation, parameters, monitor, useNames);
        this.injectors = injectors;
    }


    @Override
    public T getComponentInstance(PicoContainer container) throws PicoCompositionException {
        return getComponentInstance(container, NOTHING.class);
    }

    @Override
    public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
        T instance = null;
        for (Injector<T> injector : injectors) {
            if (instance == null) {
                instance = injector.getComponentInstance(container, NOTHING.class);
            } else {
                injector.decorateComponentInstance(container, into, instance);
            }
        }
        return (T) instance;
    }


    /**
     * @return the object returned is the result of the last of the injectors delegated to
     */
    @Override
    public Object decorateComponentInstance(PicoContainer container, Type into, T instance) {
        Object result = null;
        for (Injector<T> injector : injectors) {
            result = injector.decorateComponentInstance(container, into, instance);
        }        
        return result;
    }

    @Override
    public void verify(PicoContainer container) throws PicoCompositionException {
        for (Injector<T> injector : injectors) {
            injector.verify(container);
        }
    }

    @Override
    public final void accept(PicoVisitor visitor) {
        super.accept(visitor);
        for (Injector<T> injector : injectors) {
            injector.accept(visitor);
        }
    }

    @Override
    public String getDescriptor() {
        return "CompositeInjector";
    }
}
