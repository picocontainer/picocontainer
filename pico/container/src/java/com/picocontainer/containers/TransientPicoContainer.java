/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.containers;

import com.picocontainer.ComponentFactory;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;

@SuppressWarnings("serial")
public class TransientPicoContainer extends DefaultPicoContainer {

    public TransientPicoContainer() {
        super(null, new NullLifecycleStrategy(), new NullComponentMonitor(), new Caching().wrap(new ConstructorInjection()));
    }

    public TransientPicoContainer(final PicoContainer parent) {
        super(parent, new NullLifecycleStrategy(), new NullComponentMonitor(), new Caching().wrap(new ConstructorInjection()));
    }

    public TransientPicoContainer(final ComponentFactory componentFactory, final PicoContainer parent) {
        super(parent, new NullLifecycleStrategy(), new NullComponentMonitor(), componentFactory);
    }
}
