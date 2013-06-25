/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.injectors;

import java.lang.reflect.Type;
import java.util.Properties;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.InjectionType;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

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
