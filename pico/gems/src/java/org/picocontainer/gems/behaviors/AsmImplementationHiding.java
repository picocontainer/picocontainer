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

import org.picocontainer.LifecycleStrategy;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Characteristics;
import org.picocontainer.behaviors.AbstractBehaviorFactory;

import java.util.Properties;

/**
 * Because AsmImplementationHiding is the same type of behavior as HiddenImplementation, we use the same
 * characteristic properties for turning on and off AsmImplementation Hiding.
 * @see org.picocontainer.Characteristics.HIDE_IMPL
 * @see org.picocontainer.Characteristics.NO_HIDE_IMPL
 */
@SuppressWarnings("serial")
public class AsmImplementationHiding extends AbstractBehaviorFactory {


	@Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor componentMonitor,
                                                   final LifecycleStrategy lifecycleStrategy,
                                                   final Properties componentProperties,
                                                   final Object componentKey,
                                                   final Class<T> componentImplementation,
                                                   final Parameter... parameters) throws PicoCompositionException {
        if (AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.NO_HIDE_IMPL)) {
            return super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                                                componentProperties, componentKey, componentImplementation, parameters);
        }
        AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.HIDE_IMPL);
        ComponentAdapter<T> componentAdapter = super.createComponentAdapter(componentMonitor,
                                                                         lifecycleStrategy,
                                                                         componentProperties,
                                                                         componentKey,
                                                                         componentImplementation,
                                                                         parameters);
        return componentMonitor.newBehavior(new AsmHiddenImplementation<T>(componentAdapter));
    }

    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor componentMonitor,
                                                final LifecycleStrategy lifecycleStrategy,
                                                final Properties componentProperties,
                                                final ComponentAdapter<T> adapter) {
        if (AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.NO_HIDE_IMPL)) {
            return super.addComponentAdapter(componentMonitor,
                                             lifecycleStrategy,
                                             componentProperties,
                                             adapter);
        }
        AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.HIDE_IMPL);
        return componentMonitor.newBehavior(new AsmHiddenImplementation<T>(super.addComponentAdapter(componentMonitor,
                                                                          lifecycleStrategy,
                                                                          componentProperties,
                                                                          adapter)));
    }
}