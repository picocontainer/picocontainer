/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer;

/**
 *
 *
 */
public interface ComponentLifecycle<T> {

    /**
     * Invoke the "start" method on the component.
     *
     * @param container the container to "start" the component
     */
    void start(PicoContainer container);

    /**
     * Invoke the "stop" method on the component.
     *
     * @param container the container to "stop" the component
     */
    void stop(PicoContainer container);

    /**
     * Invoke the "dispose" method on the component.
     *
     * @param container the container to "dispose" the component
     */
    void dispose(PicoContainer container);

    /**
     * Test if a component honors a lifecycle.
     *
     * @return <code>true</code> if the component has a lifecycle
     */
    boolean componentHasLifecycle();

    boolean isStarted();


}
