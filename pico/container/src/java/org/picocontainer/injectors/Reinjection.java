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
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

import java.lang.reflect.Type;
import java.util.Properties;

@SuppressWarnings("serial")
public class Reinjection extends CompositeInjection {

    public Reinjection(InjectionType reinjectionType, final PicoContainer parent) {
        super(new ReinjectionInjectionType(parent), reinjectionType);
    }

	private static class ReinjectionInjector<T> extends AbstractInjector<T> {
        private final PicoContainer parent;

        public ReinjectionInjector(Object key, Class<T> impl, ComponentMonitor monitor, PicoContainer parent,
				boolean useNames, ConstructorParameters constructorParams, FieldParameters[] fieldParams,
				MethodParameters[] methodParams) {
            super(key, impl, monitor, useNames, methodParams);
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
                Properties componentProps, final Object key, Class<T> impl, ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) throws PicoCompositionException {
            boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
            return new ReinjectionInjector<T>(key, impl, monitor, parent, useNames, constructorParams, fieldParams, methodParams);
        }
    }
}
