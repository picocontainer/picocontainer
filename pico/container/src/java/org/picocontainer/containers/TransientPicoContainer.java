/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.containers;

import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;

@SuppressWarnings("serial")
public class TransientPicoContainer extends DefaultPicoContainer {

    public TransientPicoContainer() {
        super(new Caching().wrap(new ConstructorInjection()), new NullLifecycleStrategy(), null, new NullComponentMonitor());
    }

    public TransientPicoContainer(PicoContainer parent) {
        super(new Caching().wrap(new ConstructorInjection()), new NullLifecycleStrategy(), parent, new NullComponentMonitor());
    }
    
    public TransientPicoContainer(ComponentFactory componentFactory, PicoContainer parent) {
        super(componentFactory, new NullLifecycleStrategy(), parent, new NullComponentMonitor());
    }
}
