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


    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor,
                                                   LifecycleStrategy lifecycleStrategy,
                                                   Properties componentProperties,
                                                   Object key,
                                                   Class<T> impl,
                                                   Parameter... parameters) throws PicoCompositionException {
        removePropertiesIfPresent(componentProperties, Characteristics.AUTOMATIC);
        return componentMonitor.newBehavior(new Automated<T>(super.createComponentAdapter(componentMonitor,
                                            lifecycleStrategy,
                                            componentProperties,
                                            key,
                                            impl,
                                            parameters)));
    }

    public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor componentMonitor,
                                                LifecycleStrategy lifecycleStrategy,
                                                Properties componentProperties,
                                                ComponentAdapter<T> adapter) {
        removePropertiesIfPresent(componentProperties, Characteristics.AUTOMATIC);
        return componentMonitor.newBehavior(new Automated<T>(super.addComponentAdapter(componentMonitor,
                                         lifecycleStrategy,
                                         componentProperties,
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
