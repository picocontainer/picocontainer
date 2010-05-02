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

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.references.ThreadLocalReference;

import java.util.Properties;

/** @author Paul Hammant */
@SuppressWarnings("serial")
public class ThreadCaching extends AbstractBehavior {

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                                          LifecycleStrategy lifecycle,
                                                          Properties componentProps,
                                                          Object key,
                                                          Class<T> impl,
                                                          Parameter... parameters)
        throws PicoCompositionException {
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE)) {
            return super.createComponentAdapter(monitor,
                                                lifecycle,
                                                componentProps,
                                                key,
                                                impl,
                                                parameters);
        }
        removePropertiesIfPresent(componentProps, Characteristics.CACHE);
        return monitor.newBehavior(new ThreadCached<T>(super.createComponentAdapter(monitor,
                                                                lifecycle,
                                                                componentProps,
                                                                key,
                                                                impl,
                                                                parameters)));

    }

    public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor monitor,
                                                       LifecycleStrategy lifecycle,
                                                       Properties componentProps,
                                                       ComponentAdapter<T> adapter) {
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE)) {
            return super.addComponentAdapter(monitor, lifecycle, componentProps, adapter);
        }
        removePropertiesIfPresent(componentProps, Characteristics.CACHE);
        return monitor.newBehavior(new ThreadCached<T>(super.addComponentAdapter(monitor,
                                                             lifecycle,
                                                             componentProps,
                                                             adapter)));
    }

    /**
     * <p>
     * This behavior supports caches values per thread.
     * </p>
     *
     * @author Paul Hammant
     */
    @SuppressWarnings("serial")
    public static final class ThreadCached<T> extends Storing.Stored<T> {

        public ThreadCached(ComponentAdapter<T> delegate) {
            super(delegate, new ThreadLocalReference<Instance<T>>());
        }

        public String getDescriptor() {
            return "ThreadCached" + getLifecycleDescriptor();
        }
    }
}