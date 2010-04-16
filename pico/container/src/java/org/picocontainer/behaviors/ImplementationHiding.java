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
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Characteristics;
import org.picocontainer.behaviors.AbstractBehavior;

import java.util.Properties;

/**
 * @author Aslak Helles&oslash;y
 * @see org.picocontainer.gems.adapters.HotSwappingComponentFactory for a more feature-rich version of the class
 */
@SuppressWarnings("serial")
public class ImplementationHiding extends AbstractBehavior {

    public ComponentAdapter createComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy, Properties componentProperties, Object componentKey, Class componentImplementation, Parameter... parameters) throws PicoCompositionException {

        removePropertiesIfPresent(componentProperties, Characteristics.ENABLE_CIRCULAR);

        ComponentAdapter componentAdapter = super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                                                                         componentProperties, componentKey, componentImplementation, parameters);
        if (removePropertiesIfPresent(componentProperties, Characteristics.NO_HIDE_IMPL)) {
            return componentAdapter;
        }
        removePropertiesIfPresent(componentProperties, Characteristics.HIDE_IMPL);
        return componentMonitor.newBehavior(new HiddenImplementation(componentAdapter));

    }

    public ComponentAdapter addComponentAdapter(ComponentMonitor componentMonitor,
                                                LifecycleStrategy lifecycleStrategy,
                                                Properties componentProperties,
                                                ComponentAdapter adapter) {
        if (removePropertiesIfPresent(componentProperties, Characteristics.NO_HIDE_IMPL)) {
            return adapter;
        }
        removePropertiesIfPresent(componentProperties, Characteristics.HIDE_IMPL);
        return componentMonitor.newBehavior(new HiddenImplementation(super.addComponentAdapter(componentMonitor,
                                                                          lifecycleStrategy,
                                                                          componentProperties,
                                                                          adapter)));

    }
}
