/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import java.lang.reflect.Type;
import java.util.Properties;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.InjectionType;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

@SuppressWarnings("serial")
public class Reinjection extends CompositeInjection {

    public Reinjection(final InjectionType reinjectionType, final PicoContainer parent) {
        super(new ReinjectionInjectionType(parent), reinjectionType);
    }

	private static class ReinjectionInjector<T> extends AbstractInjector<T> {
        private final PicoContainer parent;

        public ReinjectionInjector(final Object key, final Class<T> impl, final ComponentMonitor monitor, final PicoContainer parent,
				final boolean useNames, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams,
				final MethodParameters[] methodParams) {
            super(key, impl, monitor, useNames, methodParams);
            this.parent = parent;
		}

		@Override
		public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            return (T) parent.getComponentInto(getComponentKey(), into);
        }
    }

	private static class ReinjectionInjectionType extends AbstractInjectionType {
        private final PicoContainer parent;

        public ReinjectionInjectionType(final PicoContainer parent) {
            this.parent = parent;
        }

        public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
            boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
            return new ReinjectionInjector<T>(key, impl, monitor, parent, useNames, constructorParams, fieldParams, methodParams);
        }
    }
}
