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
        super(null, new NullLifecycleStrategy(), new NullComponentMonitor(), new Caching().wrap(new ConstructorInjection()));
    }

    public TransientPicoContainer(PicoContainer parent) {
        super(parent, new NullLifecycleStrategy(), new NullComponentMonitor(), new Caching().wrap(new ConstructorInjection()));
    }
    
    public TransientPicoContainer(ComponentFactory componentFactory, PicoContainer parent) {
        super(parent, new NullLifecycleStrategy(), new NullComponentMonitor(), componentFactory);
    }
}
