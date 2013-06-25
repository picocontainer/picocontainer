/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package com.picocontainer.behaviors;

import java.util.Properties;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;
import com.picocontainer.references.ThreadLocalReference;

/** @author Paul Hammant */
@SuppressWarnings("serial")
public class ThreadCaching extends AbstractBehavior {

    @Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                        final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE)) {
            return super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
        }
        removePropertiesIfPresent(componentProps, Characteristics.CACHE);
        return monitor.changedBehavior(new ThreadCached<T>(
                super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams)));

    }

    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                                                       final Properties componentProps, final ComponentAdapter<T> adapter) {
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE)) {
            return super.addComponentAdapter(monitor, lifecycle, componentProps, adapter);
        }
        removePropertiesIfPresent(componentProps, Characteristics.CACHE);
        return monitor.changedBehavior(new ThreadCached<T>(super.addComponentAdapter(monitor, lifecycle, componentProps, adapter)));
    }

    /**
     * <p>
     * This behavior supports caches values per thread.
     * </p>
     *
     * @author Paul Hammant
     */
    public static final class ThreadCached<T> extends Storing.Stored<T> {

        public ThreadCached(final ComponentAdapter<T> delegate) {
            super(delegate, new ThreadLocalReference<Instance<T>>());
        }

        @Override
		public String getDescriptor() {
            return "ThreadCached" + getLifecycleDescriptor();
        }
    }
}