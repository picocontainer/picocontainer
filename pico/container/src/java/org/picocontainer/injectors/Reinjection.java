/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.InjectionType;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.Characteristics;
import org.picocontainer.behaviors.AbstractBehavior;

import java.lang.reflect.Type;
import java.util.Properties;

public class Reinjection extends CompositeInjection {

    public Reinjection(InjectionType reinjectionType, final PicoContainer parent) {
        super(new AbstractInjectionType() {
            public <T> ComponentAdapter<T> createComponentAdapter(
                    ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy,
                    Properties componentProps, final Object key, Class<T> impl,
                    Parameter... parameters) throws PicoCompositionException {
                boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
                return new ReinjectionInjector(key, impl, parameters, componentMonitor, parent, useNames);
            }
        }, reinjectionType);
    }

    private static class ReinjectionInjector<T> extends AbstractInjector {
        private final PicoContainer parent;

        public ReinjectionInjector(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor componentMonitor, PicoContainer parent, boolean useNames) {
            super(key, impl, parameters, componentMonitor, useNames);
            this.parent = parent;
        }

        public Object getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            return parent.getComponentInto(getComponentKey(), ComponentAdapter.NOTHING.class);
        }

    }
}
