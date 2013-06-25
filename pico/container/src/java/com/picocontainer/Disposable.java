/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the license.txt file.                                                    *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/
package com.picocontainer;

/**
 * An interface which is implemented by components that need to dispose of resources during the shutdown of that
 * component. The {@link Disposable#dispose()} must be called once during shutdown, directly after {@link
 * Startable#stop()} (if the component implements the {@link Startable} interface).
 * @see com.picocontainer.Startable the Startable interface if you need to <code>start()</code> and
 *      <code>stop()</code> semantics.
 * @see com.picocontainer.PicoContainer the main PicoContainer interface (and hence its subinterfaces and
 *      implementations like {@link DefaultPicoContainer}) implement this interface.
 */
public interface Disposable {
    /**
     * Dispose this component. The component should deallocate all resources. The contract for this method defines a
     * single call at the end of this component's life.
     */
    void dispose();
}
