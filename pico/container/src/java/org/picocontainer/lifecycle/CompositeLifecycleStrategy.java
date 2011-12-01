/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.lifecycle;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.LifecycleStrategy;

/**
 * Allow for use of alternate LifecycleStrategy strategies to be used
 * at the same time. A component can be started/stopped/disposed according
 * to *any* of the supplied LifecycleStrategy instances.
 *
 * @author Paul Hammant
 */
public class CompositeLifecycleStrategy implements LifecycleStrategy {

    private final LifecycleStrategy[] alternateStrategies;

    public CompositeLifecycleStrategy(LifecycleStrategy... alternateStrategies) {
        this.alternateStrategies = alternateStrategies;
    }

    public void start(Object component) {
        for (LifecycleStrategy lifecycle : alternateStrategies) {
    		lifecycle.start(component);
        }
    }

    public void stop(Object component) {
        for (LifecycleStrategy lifecycle : alternateStrategies) {
    		lifecycle.stop(component);
        }
    }

    public void dispose(Object component) {
        for (LifecycleStrategy lifecycle : alternateStrategies) {
            lifecycle.dispose(component);
        }
    }

    public boolean hasLifecycle(Class<?> type) {
        for (LifecycleStrategy lifecycle : alternateStrategies) {
            if (lifecycle.hasLifecycle(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLazy(ComponentAdapter<?> adapter) {
        for (LifecycleStrategy lifecycle : alternateStrategies) {
            if (lifecycle.isLazy(adapter)) {
                return true;
            }
        }
        return false;
    }
}
