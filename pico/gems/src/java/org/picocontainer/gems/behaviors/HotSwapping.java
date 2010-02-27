/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.gems.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.behaviors.AbstractBehaviorFactory;
import org.picocontainer.gems.GemsCharacteristics;

import java.util.Properties;


/**
 * Hides implementation.
 * 
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @see HotSwappable
 */
@SuppressWarnings("serial")
public class HotSwapping extends AbstractBehaviorFactory {


	@Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor componentMonitor, final LifecycleStrategy lifecycleStrategy, final Properties componentProperties, final Object componentKey, final Class<T> componentImplementation, final Parameter... parameters)
            throws PicoCompositionException {
        ComponentAdapter<T> delegateAdapter = super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                componentProperties, componentKey, componentImplementation, parameters);

        if (AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, GemsCharacteristics.NO_HOT_SWAP)) {
        	return delegateAdapter;
		} 

		AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, GemsCharacteristics.HOT_SWAP);        
        return componentMonitor.newBehavior(new HotSwappable<T>(delegateAdapter));
    }

    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor componentMonitor,
                                                final LifecycleStrategy lifecycleStrategy,
                                                final Properties componentProperties,
                                                final ComponentAdapter<T> adapter) {
        if (AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, GemsCharacteristics.NO_HOT_SWAP)) {
        	return super.addComponentAdapter(componentMonitor,
                    lifecycleStrategy,
                    componentProperties,
                    adapter);
		} 

    	
		AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, GemsCharacteristics.HOT_SWAP);        
    	return componentMonitor.newBehavior(new HotSwappable<T>(super.addComponentAdapter(componentMonitor,
                                                                 lifecycleStrategy,
                                                                 componentProperties,
                                                                 adapter)));
    }
}
