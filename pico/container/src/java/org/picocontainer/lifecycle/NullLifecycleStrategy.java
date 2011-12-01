/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.lifecycle;

import java.io.Serializable;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.LifecycleStrategy;

/**
 * Lifecycle strategy that does nothing.
 *
 */
@SuppressWarnings("serial")
public class NullLifecycleStrategy implements LifecycleStrategy, Serializable {

 
    /** {@inheritDoc} **/
	public void start(final Object component) {
		//Does nothing
    }

    /** {@inheritDoc} **/
    public void stop(final Object component) {
		//Does nothing
    }

    /** {@inheritDoc} **/
    public void dispose(final Object component) {
		//Does nothing
    }

    /** {@inheritDoc} **/
    public boolean hasLifecycle(final Class<?> type) {
        return false;
    }

    public boolean isLazy(ComponentAdapter<?> adapter) {
        return false;
    }
}
