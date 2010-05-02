/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.Characteristics;
import org.picocontainer.behaviors.AbstractBehavior;

import java.util.Properties;

/** @author Paul Hammant */
@SuppressWarnings("serial")
public class MultiInjection extends AbstractInjectionType {
    private final String setterPrefix;

    public MultiInjection(String setterPrefix) {
        this.setterPrefix = setterPrefix;
    }

    public MultiInjection() {
        this("set");
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                                          LifecycleStrategy lifecycleStrategy,
                                                          Properties componentProperties,
                                                          Object key,
                                                          Class<T> componentImplementation,
                                                          Parameter... parameters) throws PicoCompositionException {
        boolean useNames = AbstractBehavior.arePropertiesPresent(componentProperties, Characteristics.USE_NAMES, true);
        return wrapLifeCycle(new MultiInjector(key, componentImplementation, parameters, monitor, setterPrefix, useNames), lifecycleStrategy);
    }
}
