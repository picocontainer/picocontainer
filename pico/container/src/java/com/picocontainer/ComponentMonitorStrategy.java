/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer;


/**
 * <p>
 * Interface responsible for changing monitoring strategy.
 * It may be implemented by {@link com.picocontainer.PicoContainer containers} and
 * single {@link com.picocontainer.ComponentAdapter component adapters}.
 * The choice of supporting the monitor strategy is left to the
 * implementers of the container and adapters.
 * </p>
 *
 * @author Paul Hammant
 * @author Joerg Schaible
 * @author Mauro Talevi
 */
public interface ComponentMonitorStrategy {

    /**
     * Changes the component monitor used
     * @param monitor the new ComponentMonitor to use
     * @return the old monitor
     */
	ComponentMonitor changeMonitor(ComponentMonitor monitor);

    /**
     * Returns the monitor currently used
     * @return The ComponentMonitor currently used
     */
    ComponentMonitor currentMonitor();

}
