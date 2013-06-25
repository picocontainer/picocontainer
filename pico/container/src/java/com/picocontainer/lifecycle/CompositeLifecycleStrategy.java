/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package com.picocontainer.lifecycle;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.LifecycleStrategy;

/**
 * Allow for use of alternate LifecycleStrategy strategies to be used
 * at the same time. A component can be started/stopped/disposed according
 * to *any* of the supplied LifecycleStrategy instances.
 *
 * @author Paul Hammant
 */
public class CompositeLifecycleStrategy implements LifecycleStrategy {

    private final LifecycleStrategy[] alternateStrategies;

    public CompositeLifecycleStrategy(final LifecycleStrategy... alternateStrategies) {
        this.alternateStrategies = alternateStrategies;
    }

    public void start(final Object component) {
        for (LifecycleStrategy lifecycle : alternateStrategies) {
    		lifecycle.start(component);
        }
    }

    public void stop(final Object component) {
        for (LifecycleStrategy lifecycle : alternateStrategies) {
    		lifecycle.stop(component);
        }
    }

    public void dispose(final Object component) {
        for (LifecycleStrategy lifecycle : alternateStrategies) {
            lifecycle.dispose(component);
        }
    }

    public boolean hasLifecycle(final Class<?> type) {
        for (LifecycleStrategy lifecycle : alternateStrategies) {
            if (lifecycle.hasLifecycle(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLazy(final ComponentAdapter<?> adapter) {
        for (LifecycleStrategy lifecycle : alternateStrategies) {
            if (lifecycle.isLazy(adapter)) {
                return true;
            }
        }
        return false;
    }
}
