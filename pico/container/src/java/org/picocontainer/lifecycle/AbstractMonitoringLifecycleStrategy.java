/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.lifecycle;

import java.io.Serializable;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.ComponentMonitorStrategy;
import org.picocontainer.LifecycleStrategy;

/**
 * Abstract base class for lifecycle strategy implementation supporting a {@link ComponentMonitor}.
 *
 * @author J&ouml;rg Schaible
 */
public abstract class AbstractMonitoringLifecycleStrategy implements LifecycleStrategy, ComponentMonitorStrategy, Serializable {

	/**
	 * Component monitor that receives lifecycle state.
	 */
    private ComponentMonitor monitor;

    /**
     * Construct a AbstractMonitoringLifecycleStrategy.
     *
     * @param monitor the monitor to use
     * @throws NullPointerException if the monitor is <code>null</code>
     */
    public AbstractMonitoringLifecycleStrategy(final ComponentMonitor monitor) {
        changeMonitor(monitor);
    }

    /**
     * Swaps the current monitor with a replacement.
     * @param monitor The new monitor.
     * @throws NullPointerException if the passed in monitor is null.
     */
    public void changeMonitor(final ComponentMonitor monitor) {
        if (monitor == null) {
            throw new NullPointerException("Monitor is null");
        }
        this.monitor = monitor;
    }

    public ComponentMonitor currentMonitor() {
        return monitor;
    }

    public boolean isLazy(final ComponentAdapter<?> adapter) {
        return false;
    }

}
