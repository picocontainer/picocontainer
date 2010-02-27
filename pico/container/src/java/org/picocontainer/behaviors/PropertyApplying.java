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
import org.picocontainer.behaviors.AbstractBehaviorFactory;
import org.picocontainer.behaviors.PropertyApplicator;

import java.util.Properties;

/**
 * A {@link org.picocontainer.ComponentFactory} that creates
 * {@link PropertyApplicator} instances.
 * 
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("serial")
public final class PropertyApplying extends AbstractBehaviorFactory {

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor,
            LifecycleStrategy lifecycleStrategy, Properties componentProperties, Object componentKey,
            Class<T> componentImplementation, Parameter... parameters) throws PicoCompositionException {
        ComponentAdapter<?> decoratedAdapter = super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                componentProperties, componentKey, componentImplementation, parameters);
        removePropertiesIfPresent(componentProperties, Characteristics.PROPERTY_APPLYING);
        return componentMonitor.newBehavior(new PropertyApplicator(decoratedAdapter));
    }

    public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor componentMonitor,
            LifecycleStrategy lifecycleStrategy, Properties componentProperties, ComponentAdapter<T> adapter) {
        removePropertiesIfPresent(componentProperties, Characteristics.PROPERTY_APPLYING);
        return componentMonitor.newBehavior(new PropertyApplicator(super.addComponentAdapter(componentMonitor, lifecycleStrategy,
                componentProperties, adapter)));
    }
}
