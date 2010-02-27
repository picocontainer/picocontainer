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
import org.picocontainer.behaviors.AbstractBehaviorFactory;

import java.util.Properties;

/**
 * This behavior factory provides java.util.concurrent locks.  It is recommended to be used instead
 * of {@link org.picocontainer.behaviors.Synchronizing} since it results in better performance.
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant.
 */
@SuppressWarnings("serial")
public class Locking extends AbstractBehaviorFactory {

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
}