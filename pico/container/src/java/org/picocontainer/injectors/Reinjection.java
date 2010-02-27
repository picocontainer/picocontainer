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
import org.picocontainer.InjectionFactory;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.Characteristics;
import org.picocontainer.behaviors.AbstractBehaviorFactory;

import java.lang.reflect.Type;
import java.util.Properties;

public class Reinjection extends CompositeInjection {

    public Reinjection(InjectionFactory reinjectionFactory, final PicoContainer parent) {
        super(new AbstractInjectionFactory() {
            public <T> ComponentAdapter<T> createComponentAdapter(
                    ComponentMonitor componentMonitor, LifecycleStrategy lifecycleStrategy,
                    Properties componentProperties, final Object componentKey, Class<T> componentImplementation,
                    Parameter... parameters) throws PicoCompositionException {
                boolean useNames = AbstractBehaviorFactory.arePropertiesPresent(componentProperties, Characteristics.USE_NAMES, true);
                return new ReinjectionInjector(componentKey, componentImplementation, parameters, componentMonitor, parent, useNames);
            }
        }, reinjectionFactory);
    }

    private static class ReinjectionInjector<T> extends AbstractInjector {
        private final PicoContainer parent;

        public ReinjectionInjector(Object componentKey, Class<T> componentImplementation, Parameter[] parameters, ComponentMonitor componentMonitor, PicoContainer parent, boolean useNames) {
            super(componentKey, componentImplementation, parameters, componentMonitor, useNames);
            this.parent = parent;
        }

        public Object getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            return parent.getComponent(getComponentKey());
        }

    }
}
