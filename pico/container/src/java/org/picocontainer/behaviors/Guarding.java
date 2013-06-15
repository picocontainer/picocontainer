/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.behaviors;

import java.lang.reflect.Type;
import java.util.Properties;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

/**
 * factory class creating guard behaviour
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class Guarding extends AbstractBehavior {

    @Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
            final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        String guard = getAndRemovePropertiesIfPresentByKey(componentProps, Characteristics.GUARD);
        ComponentAdapter<T> delegate = super.createComponentAdapter(monitor, lifecycle,
                componentProps, key, impl, constructorParams, fieldParams, methodParams);
        if (guard == null) {
            return delegate;
        } else {
            return monitor.changedBehavior(new Guarded<T>(delegate, guard));
        }

    }

    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
            final Properties componentProps, final ComponentAdapter<T> adapter) {
        String guard = getAndRemovePropertiesIfPresentByKey(componentProps, Characteristics.GUARD);
        ComponentAdapter<T> delegate = super.addComponentAdapter(monitor, lifecycle, componentProps, adapter);
        if (guard == null) {
            return delegate;
        } else {
            return monitor.changedBehavior(monitor.changedBehavior(new Guarded<T>(delegate, guard)));
        }
    }

    /**
     * behaviour for allows components to be guarded by another component
     *
     * @author Paul Hammant
     * @param <T>
     */
    @SuppressWarnings("serial")
    public static class Guarded<T> extends AbstractChangedBehavior<T> {
        private final String guard;

        public Guarded(final ComponentAdapter<T> delegate, final String guard) {
            super(delegate);
            this.guard = guard;
        }

        @Override
		public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            container.getComponentInto(guard, into);
            return super.getComponentInstance(container, into);
        }

        public String getDescriptor() {
            return "Guarded(with " + guard + ")";
        }


    }
}