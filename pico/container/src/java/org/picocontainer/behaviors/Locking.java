/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;

import java.lang.reflect.Type;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
 * This behavior factory provides java.util.concurrent locks.  It is recommended to be used instead
 * of {@link org.picocontainer.behaviors.Synchronizing} since it results in better performance.
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant.
 */
@SuppressWarnings("serial")
public class Locking extends AbstractBehavior {

    /** {@inheritDoc} **/
	@Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                                                   final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) {

        if (removePropertiesIfPresent(componentProps, Characteristics.NO_LOCK)) {
     	   return super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
        }

        removePropertiesIfPresent(componentProps, Characteristics.LOCK);
        return monitor.changedBehavior(new Locked<T>(super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams)));
    }

    /** {@inheritDoc} **/
	@Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                                                final Properties componentProps, final ComponentAdapter<T> adapter) {
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_LOCK)) {
        	return super.addComponentAdapter(monitor, lifecycle, componentProps, adapter);
        }

        removePropertiesIfPresent(componentProps, Characteristics.LOCK);
        return monitor.changedBehavior(new Locked<T>(super.addComponentAdapter(monitor, lifecycle, componentProps, adapter)));
    }

    /**
     * @author Paul Hammant
     */
    @SuppressWarnings("serial")
    public static class Locked<T> extends AbstractChangedBehavior<T> {

        /**
         * Reentrant lock.
         */
        private final Lock lock = new ReentrantLock();

        public Locked(final ComponentAdapter<T> delegate) {
            super(delegate);
        }

        @Override
		public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            T retVal = null;
            lock.lock();
            try {
              retVal = super.getComponentInstance(container, into);
            }
            finally {
              lock.unlock();
            }
            return retVal;
        }

        public String getDescriptor() {
            return "Locked";
        }

    }
}