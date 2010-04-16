/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.LifecycleStrategy;

import java.util.Properties;

/**
 * factory class creating guard behaviour
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class Guarding extends AbstractBehavior {

    public <T> ComponentAdapter<T> createComponentAdapter(
            ComponentMonitor componentMonitor,
            LifecycleStrategy lifecycleStrategy,
            Properties componentProperties, Object componentKey,
            Class<T> componentImplementation, Parameter... parameters)
            throws PicoCompositionException {
        String guard = getAndRemovePropertiesIfPresentByKey(componentProperties, Characteristics.GUARD);
        ComponentAdapter<T> delegate = super.createComponentAdapter(componentMonitor, lifecycleStrategy,
                componentProperties, componentKey, componentImplementation, parameters);
        if (guard == null) {
            return delegate;
        } else {
            return componentMonitor.newBehavior(new Guarded<T>(delegate, guard));
        }

    }

    public <T> ComponentAdapter<T> addComponentAdapter(
            ComponentMonitor componentMonitor,
            LifecycleStrategy lifecycleStrategy,
            Properties componentProperties, ComponentAdapter<T> adapter) {
        String guard = getAndRemovePropertiesIfPresentByKey(componentProperties, Characteristics.GUARD);
        ComponentAdapter<T> delegate = super.addComponentAdapter(componentMonitor, lifecycleStrategy, componentProperties, adapter);
        if (guard == null) {
            return delegate;
        } else {
            return componentMonitor.newBehavior(componentMonitor.newBehavior(new Guarded<T>(delegate, guard)));
        }
    }

}