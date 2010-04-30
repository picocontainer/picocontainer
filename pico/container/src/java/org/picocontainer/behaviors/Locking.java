/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Characteristics;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;

import java.lang.reflect.Type;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This behavior factory provides java.util.concurrent locks.  It is recommended to be used instead
 * of {@link org.picocontainer.behaviors.Synchronizing} since it results in better performance.
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant.
 */
@SuppressWarnings("serial")
public class Locking extends AbstractBehavior {

    /** {@inheritDoc} **/
	public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor,
                                                   LifecycleStrategy lifecycleStrategy,
                                                   Properties componentProperties,
                                                   Object componentKey,
                                                   Class<T> componentImplementation,
                                                   Parameter... parameters) {
    	
        if (removePropertiesIfPresent(componentProperties, Characteristics.NO_LOCK)) {
     	   return super.createComponentAdapter(
     	            componentMonitor,
     	            lifecycleStrategy,
     	            componentProperties,
     	            componentKey,
     	            componentImplementation,
     	            parameters);
        }
        
        removePropertiesIfPresent(componentProperties, Characteristics.LOCK);
        return componentMonitor.newBehavior(new Locked<T>(super.createComponentAdapter(
            componentMonitor,
            lifecycleStrategy,
            componentProperties,
            componentKey,
            componentImplementation,
            parameters)));
    }

    /** {@inheritDoc} **/
	public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor componentMonitor,
                                                LifecycleStrategy lifecycleStrategy,
                                                Properties componentProperties,
                                                ComponentAdapter<T> adapter) {
        if (removePropertiesIfPresent(componentProperties, Characteristics.NO_LOCK)) {
        	return super.addComponentAdapter(componentMonitor,
                    lifecycleStrategy,
                    componentProperties,
                    adapter);
        }    	
    	
        removePropertiesIfPresent(componentProperties, Characteristics.LOCK);
        return componentMonitor.newBehavior(new Locked<T>(super.addComponentAdapter(componentMonitor,
                                                          lifecycleStrategy,
                                                          componentProperties,
                                                          adapter)));
    }

    /**
     * @author Paul Hammant
     */
    @SuppressWarnings("serial")
    public static class Locked<T> extends AbstractChangedBehavior<T> {

        /**
         * Reentrant lock.
         */
        private Lock lock = new ReentrantLock();

        public Locked(ComponentAdapter<T> delegate) {
            super(delegate);
        }

        public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
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