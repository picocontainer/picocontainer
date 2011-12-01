/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
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
        super(new ReinjectionInjectionType(parent), reinjectionType);
    }

    private static class ReinjectionInjector<T> extends AbstractInjector<T> {
        private final PicoContainer parent;

        public ReinjectionInjector(Object key, Class<T> impl, ComponentMonitor monitor, PicoContainer parent, boolean useNames, Parameter... parameters) {
            super(key, impl, monitor, useNames, parameters);
            this.parent = parent;
        }

        public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            return (T) parent.getComponentInto(getComponentKey(), into);
        }
    }

    private static class ReinjectionInjectionType extends AbstractInjectionType {
        private final PicoContainer parent;

        public ReinjectionInjectionType(PicoContainer parent) {
            this.parent = parent;
        }

        public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle,
                Properties componentProps, final Object key, Class<T> impl, Parameter... parameters) throws PicoCompositionException {
            boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
            return new ReinjectionInjector<T>(key, impl, monitor, parent, useNames, parameters);
        }
    }
}
