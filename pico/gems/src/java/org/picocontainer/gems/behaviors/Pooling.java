/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.gems.behaviors;

import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.gems.GemsCharacteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;

import java.util.Properties;

@SuppressWarnings("serial")
public class Pooling extends AbstractBehavior {

 	private final Pooled.Context poolContext;

    public Pooling(final Pooled.Context poolContext) {
        this.poolContext = poolContext;
    }

    public Pooling() {
        poolContext = new Pooled.DefaultContext();
    }

    @Override
	public ComponentAdapter createComponentAdapter(final ComponentMonitor componentMonitor, final LifecycleStrategy lifecycleStrategy, final Properties componentProperties, final Object componentKey, final Class componentImplementation, final Parameter... parameters)
            throws PicoCompositionException {
        ComponentAdapter delegate = super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                                                                         componentProperties, componentKey, componentImplementation, parameters);

        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.NO_POOL)) {
        	return delegate;
		} 
        
        AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.POOL);
        Pooled behavior = new Pooled(delegate, poolContext);
        //TODO
        //Characteristics.HIDE.setProcessedIn(componentCharacteristics);
        return componentMonitor.newBehavior(behavior);
    }

    @Override
	public ComponentAdapter addComponentAdapter(final ComponentMonitor componentMonitor,
                                                final LifecycleStrategy lifecycleStrategy,
                                                final Properties componentProperties,
                                                final ComponentAdapter adapter) {

        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.NO_POOL)) {
        	return super.addComponentAdapter(componentMonitor,
                    lifecycleStrategy,
                    componentProperties,
                    adapter);
		} 
    	
        AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.POOL);
    	return componentMonitor.newBehavior(new Pooled(super.addComponentAdapter(componentMonitor,
                                         lifecycleStrategy,
                                         componentProperties,
                                         adapter), poolContext));
    }
}
