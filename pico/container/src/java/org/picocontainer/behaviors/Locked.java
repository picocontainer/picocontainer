/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;


import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoCompositionException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.reflect.Type;

/**
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class Locked<T> extends AbstractBehavior<T> {
	
	/**
	 * Reentrant lock.
	 */
	private Lock lock = new ReentrantLock();

    public Locked(ComponentAdapter<T> delegate) {
        super(delegate);
    }

    public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
        T retVal = null;
        lock.lock();
        try {
          retVal = super.getComponentInstance(container, into);
        }
        finally {
          lock.unlock();
        }
        return retVal;
    }

    public String getDescriptor() {
        return "Locked";
    }

}