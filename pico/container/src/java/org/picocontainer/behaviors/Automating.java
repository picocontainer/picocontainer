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
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.Characteristics;

import java.io.Serializable;
import java.util.Properties;

@SuppressWarnings("serial")
public class Automating extends AbstractBehaviorFactory implements Serializable {


    public ComponentAdapter createComponentAdapter(ComponentMonitor componentMonitor,
                                                   LifecycleStrategy lifecycleStrategy,
                                                   Properties componentProperties,
                                                   Object componentKey,
                                                   Class componentImplementation,
                                                   Parameter... parameters) throws PicoCompositionException {
        removePropertiesIfPresent(componentProperties, Characteristics.AUTOMATIC);
        return componentMonitor.newBehavior(new Automated(super.createComponentAdapter(componentMonitor,
                                            lifecycleStrategy,
                                            componentProperties,
                                            componentKey,
                                            componentImplementation,
                                            parameters)));
    }

    public ComponentAdapter addComponentAdapter(ComponentMonitor componentMonitor,
                                                LifecycleStrategy lifecycleStrategy,
                                                Properties componentProperties,
                                                ComponentAdapter adapter) {
        removePropertiesIfPresent(componentProperties, Characteristics.AUTOMATIC);
        return componentMonitor.newBehavior(new Automated(super.addComponentAdapter(componentMonitor,
                                         lifecycleStrategy,
                                         componentProperties,
                                         adapter)));
    }
}
