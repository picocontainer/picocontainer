/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.references.ThreadLocalReference;

/**
 * <p>
 * This behavior supports caches values per thread.
 * </p>
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public final class ThreadCached<T> extends Stored<T>{

    public ThreadCached(ComponentAdapter<T> delegate) {
        super(delegate, new ThreadLocalReference<Instance<T>>());
    }

    public String getDescriptor() {
        return "ThreadCached" + getLifecycleDescriptor();
    }
}