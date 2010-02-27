/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.Parameter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.annotations.Inject;

/** @author Paul Hammant */
@SuppressWarnings("serial")
public class MultiInjector extends CompositeInjector {

    public MultiInjector(Object componentKey,
                         Class componentImplementation,
                         Parameter[] parameters,
                         ComponentMonitor componentMonitor, String setterPrefix, boolean useNames) {
        super(componentKey, componentImplementation, parameters, componentMonitor, useNames,
                componentMonitor.newInjector(new ConstructorInjector(componentKey, componentImplementation, parameters, componentMonitor, useNames)),
                componentMonitor.newInjector(new SetterInjector(componentKey, componentImplementation, parameters, componentMonitor, setterPrefix, useNames)),
                componentMonitor.newInjector(new AnnotatedMethodInjector(componentKey, componentImplementation, parameters, componentMonitor, Inject.class, useNames)),
                componentMonitor.newInjector(new AnnotatedFieldInjector(componentKey, componentImplementation, parameters, componentMonitor, Inject.class, useNames)));

    }

    public String getDescriptor() {
        return "MultiInjector";
    }
}
