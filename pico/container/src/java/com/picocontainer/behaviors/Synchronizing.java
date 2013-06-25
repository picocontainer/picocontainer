/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.behaviors;

import java.lang.reflect.Type;
import java.util.Properties;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

/**
 * This behavior factory provides <strong>synchronized</strong> wrappers to control access to a particular component.
 *  It is recommended that you use {@link com.picocontainer.behaviors.Locking} instead since it results in better performance
 *  and does the same job.
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("serial")
public class Synchronizing extends AbstractBehavior {

    /** {@inheritDoc} **/
	@Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps,
                                                 final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) {
       if (removePropertiesIfPresent(componentProps, Characteristics.NO_SYNCHRONIZE)) {
    	   return super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
       }

    	removePropertiesIfPresent(componentProps, Characteristics.SYNCHRONIZE);
        return monitor.changedBehavior(new Synchronized<T>(super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams)));
    }

    /** {@inheritDoc} **/
    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps,
                                                final ComponentAdapter<T> adapter) {
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_SYNCHRONIZE)) {
        	return super.addComponentAdapter(monitor, lifecycle, componentProps, adapter);
        }

    	removePropertiesIfPresent(componentProps, Characteristics.SYNCHRONIZE);
        return monitor.changedBehavior(new Synchronized<T>(super.addComponentAdapter(monitor, lifecycle, componentProps, adapter)));
    }

    /**
     * Component Adapter that uses java synchronized around getComponentInstance().
     * @author Aslak Helles&oslash;y
     * @author Manish Shah
     */
    @SuppressWarnings("serial")
    public static class Synchronized<T> extends AbstractChangedBehavior<T> {

        public Synchronized(final ComponentAdapter<T> delegate) {
            super(delegate);
        }

        @Override
		public synchronized T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            return super.getComponentInstance(container, into);
        }

        public String getDescriptor() {
            return "Synchronized";
        }

    }
}
