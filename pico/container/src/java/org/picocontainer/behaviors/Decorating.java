/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/

package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.behaviors.Decorated;
import org.picocontainer.behaviors.AbstractBehaviorFactory;

import java.util.Properties;


/**
 * BehaviorFactory for Decorating. This factory will create {@link org.picocontainer.gems.behaviors.Decorated} that will
 * allow you to decorate what you like on the component instance that has been created
 *
 * @author Paul Hammant
 */
public abstract class Decorating extends AbstractBehaviorFactory implements Decorated.Decorator {


    public ComponentAdapter createComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy,
                                                   Properties componentProperties, final Object componentKey,
                                                   final Class componentImplementation, final Parameter... parameters) throws PicoCompositionException {
        return componentMonitor.newBehavior(new Decorated(super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                componentProperties,componentKey, componentImplementation, parameters), this));
    }


    public ComponentAdapter addComponentAdapter(ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy,
                                                Properties componentProperties, ComponentAdapter adapter) {
        return super.addComponentAdapter(componentMonitor, lifecycleStrategy, componentProperties, adapter);
    }
}