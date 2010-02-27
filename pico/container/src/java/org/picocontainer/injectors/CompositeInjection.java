/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import java.util.Properties;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.InjectionFactory;
import org.picocontainer.Injector;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.behaviors.AbstractBehaviorFactory;

/**
 * A Composite of other types on InjectionFactories - pass them into the varargs constructor.
 * 
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class CompositeInjection extends AbstractInjectionFactory {

    private final InjectionFactory[] injectionFactories;

    public CompositeInjection(InjectionFactory... injectionFactories) {
        this.injectionFactories = injectionFactories;
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                                          LifecycleStrategy lifecycleStrategy,
                                                          Properties componentProperties,
                                                          Object componentKey,
                                                          Class<T> componentImplementation,
                                                          Parameter... parameters) throws PicoCompositionException {

        Injector[] injectors = new Injector[injectionFactories.length];

        for (int i = 0; i < injectionFactories.length; i++) {
            InjectionFactory injectionFactory = injectionFactories[i];
            injectors[i] = (Injector) injectionFactory.createComponentAdapter(monitor,
                    lifecycleStrategy, componentProperties, componentKey, componentImplementation, parameters);
        }

        boolean useNames = AbstractBehaviorFactory.arePropertiesPresent(componentProperties, Characteristics.USE_NAMES, true);
        return wrapLifeCycle(monitor.newInjector(new CompositeInjector(componentKey, componentImplementation, parameters,
                monitor, useNames, injectors)), lifecycleStrategy);
    }
}