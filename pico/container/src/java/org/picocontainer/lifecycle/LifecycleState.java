/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.lifecycle;

/**
 * Current lifecycle state of the container.
 * @author Michael Rimov
 * @author Paul Hammant
 */
public interface LifecycleState {

	/**
	 * Lifecycle state for when a component is being removed.
	 */
    void removingComponent();

	/**
	 * Start is normally allowed if the object is constructed or
	 * already stopped.  It is not allowed if the system is already
	 * started or disposed.
	 * @return true if start lifecycle methods should be allowed.
	 */
    void starting();

    /**
     * Lifecycle state for when the container is being stopped.  (Ie, right after Picocontainer.stop()
     * has been called, but before any components are stopped.
     */
    void stopping();

    /**
     * Lifecycle state for when stop has been completed.
     */
    void stopped();

    /**
     * Checks if current lifecycle is started.
     * @return true if the current container state is STARTED.
     */
    boolean isStarted();

    /**
     * Turns the lifecycle state to indicate that the dispose() process is being
     * executed on the container.
     */
    void disposing();

    /**
     * Turns the lifecycle state to completely disposed.  Internally called after PicoContainer.dispose()
     * is finished.
     */
    void disposed();
    
    /**
     * Checks if the current lifecycle is disposed.
     * @return true if the current state is DISPOSED.
     */
    boolean isDisposed();
    
    /**
     * Checks if the current lifecyle is stopped.
     * @return true if the current state is STOPPED;
     * @return
     */
    boolean isStopped();
}
