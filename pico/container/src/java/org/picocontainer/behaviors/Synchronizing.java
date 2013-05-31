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

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Characteristics;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

import java.lang.reflect.Type;
import java.util.Properties;

/**
 * This behavior factory provides <strong>synchronized</strong> wrappers to control access to a particular component.
 *  It is recommended that you use {@link org.picocontainer.behaviors.Locking} instead since it results in better performance
 *  and does the same job.
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("serial")
public class Synchronizing extends AbstractBehavior {
	
    /** {@inheritDoc} **/
	public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties componentProps,
                                                 Object key, Class<T> impl, ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) {
       if (removePropertiesIfPresent(componentProps, Characteristics.NO_SYNCHRONIZE)) {
    	   return super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
       }
    	
    	removePropertiesIfPresent(componentProps, Characteristics.SYNCHRONIZE);
        return monitor.changedBehavior(new Synchronized<T>(super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams)));
    }

    /** {@inheritDoc} **/
    public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties componentProps,
                                                ComponentAdapter<T> adapter) {
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

        public Synchronized(ComponentAdapter<T> delegate) {
            super(delegate);
        }

        public synchronized T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            return super.getComponentInstance(container, into);
        }

        public String getDescriptor() {
            return "Synchronized";
        }

    }
}
