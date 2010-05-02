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

import org.picocontainer.ChangedBehavior;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.Characteristics;

import java.io.Serializable;
import java.util.Properties;

@SuppressWarnings("serial")
public class Automating extends AbstractBehavior implements Serializable {


    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                                   LifecycleStrategy lifecycle,
                                                   Properties componentProps,
                                                   Object key,
                                                   Class<T> impl,
                                                   Parameter... parameters) throws PicoCompositionException {
        removePropertiesIfPresent(componentProps, Characteristics.AUTOMATIC);
        return monitor.newBehavior(new Automated<T>(super.createComponentAdapter(monitor,
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
        removePropertiesIfPresent(componentProps, Characteristics.AUTOMATIC);
        return monitor.newBehavior(new Automated<T>(super.addComponentAdapter(monitor,
                                         lifecycle,
                                         componentProps,
                                         adapter)));
    }

    @SuppressWarnings("serial")
    public static class Automated<T> extends AbstractChangedBehavior<T> implements ChangedBehavior<T>, Serializable {


        public Automated(ComponentAdapter<T> delegate) {
            super(delegate);
        }

        public boolean hasLifecycle(Class<?> type) {
            return true;
        }

        public String getDescriptor() {
            return "Automated";
        }
    }
}
