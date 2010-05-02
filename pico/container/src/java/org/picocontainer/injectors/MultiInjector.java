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
import org.picocontainer.annotations.Inject;

/** @author Paul Hammant */
@SuppressWarnings("serial")
public class MultiInjector extends CompositeInjector {

    public MultiInjector(Object key,
                         Class impl,
                         Parameter[] parameters,
                         ComponentMonitor componentMonitor, String setterPrefix, boolean useNames) {
        super(key, impl, parameters, componentMonitor, useNames,
                componentMonitor.newInjector(new ConstructorInjector(key, impl, parameters, componentMonitor, useNames)),
                componentMonitor.newInjector(new SetterInjector(key, impl, parameters, componentMonitor, setterPrefix, useNames)),
                componentMonitor.newInjector(new AnnotatedMethodInjector(key, impl, parameters, componentMonitor, useNames, Inject.class)),
                componentMonitor.newInjector(new AnnotatedFieldInjector(key, impl, parameters, componentMonitor, useNames, Inject.class)));

    }

    public String getDescriptor() {
        return "MultiInjector";
    }
}
