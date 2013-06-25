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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.ComponentMonitorStrategy;
import com.picocontainer.InjectionType;
import com.picocontainer.Injector;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

/**
 * A Composite of other types on InjectionFactories - pass them into the varargs constructor.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class CompositeInjection extends AbstractInjectionType {

    private final InjectionType[] injectionTypes;

    public CompositeInjection(final InjectionType... injectionTypes) {
        this.injectionTypes = injectionTypes;
    }

    public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor,
                                                          final LifecycleStrategy lifecycle,
                                                          final Properties componentProps,
                                                          final Object key,
                                                          final Class<T> impl,
                                                          final ConstructorParameters constructorParams,
                                                          final FieldParameters[] fieldParams,
                                                          final MethodParameters[] methodParams) throws PicoCompositionException {

        @SuppressWarnings("unchecked")
		Injector<T>[] injectors = new Injector[injectionTypes.length];

        for (int i = 0; i < injectionTypes.length; i++) {
            InjectionType injectionType = injectionTypes[i];
            injectors[i] = (Injector<T>) injectionType.createComponentAdapter(monitor,
                    lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
        }

        boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
        return wrapLifeCycle(monitor.newInjector(new CompositeInjector<T>(key, impl, monitor, useNames, injectors)), lifecycle);
    }

    public static class CompositeInjector<T> extends AbstractInjector<T> {

        private final Injector<T>[] injectors;

        public CompositeInjector(final Object key, final Class<?> impl, final ComponentMonitor monitor,
                                 final boolean useNames, final Injector<T>... injectors) {
            super(key, impl, monitor, useNames);
            this.injectors = injectors;
        }

        @Override
        public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
	            T instance = null;

	            for (Class<?> eachSuperClass : this.getListOfSupertypesToDecorate(getComponentImplementation())) {
		            for (Injector<T> injector : injectors) {
		                if (instance == null) {
		                    instance = injector.getComponentInstance(container, NOTHING.class);
		                } else {
		                    injector.partiallyDecorateComponentInstance(container, into, instance, eachSuperClass);
		                }
		            }
	            }
	            return instance;
        }

        protected Class<?>[] getListOfSupertypesToDecorate(final Class<?> startClass) {
        	if (startClass == null) {
        		throw new NullPointerException("startClass");
        	}

        	List<Class<?>> result = new ArrayList<Class<?>>();

        	Class<?> current = startClass;
        	while (!Object.class.getName().equals(current.getName())) {
        		result.add(current);
        		current = current.getSuperclass();
        	}

        	//Needed for: com.picocontainer.injectors.AdaptingInjectionTestCase.testSingleUsecanBeInstantiatedByDefaultComponentAdapter()
        	if (result.size() == 0) {
        		result.add(Object.class);
        	}

        	//Start with base class, not derived class.
        	Collections.reverse(result);

        	return result.toArray(new Class[result.size()]);
        }


        /**
         * Performs a set of partial injections starting at the base class and working its
         * way down.
         * <p>{@inheritDoc}</p>
         * @return the object returned is the result of the last of the injectors delegated to
         */
        @Override
        public Object decorateComponentInstance(final PicoContainer container, final Type into, final T instance) {
        	Object result = null;
        	for (Class<?> eachSuperClass : this.getListOfSupertypesToDecorate(instance.getClass())) {
        		result = partiallyDecorateComponentInstance(container, into, instance, eachSuperClass);
        	}

        	return result;

        }

		@Override
		public Object partiallyDecorateComponentInstance(final PicoContainer container, final Type into, final T instance,
				final Class<?> classFilter) {
			Object result = null;

            for (Injector<T> injector : injectors) {
            	result = injector.partiallyDecorateComponentInstance(container, into, instance, classFilter);
            }
            return result;
		}

        @Override
        public void verify(final PicoContainer container) throws PicoCompositionException {
            for (Injector<T> injector : injectors) {
                injector.verify(container);
            }
        }

        @Override
        public final void accept(final PicoVisitor visitor) {
            super.accept(visitor);
            for (Injector<T> injector : injectors) {
                injector.accept(visitor);
            }
        }

        @Override
        public String getDescriptor() {
            StringBuilder sb = new StringBuilder("CompositeInjector(");
            for (Injector<T> injector : injectors) {
                sb.append(injector.getDescriptor());
            }

            if (sb.charAt(sb.length() - 1) == '-') {
            	sb.deleteCharAt(sb.length()-1); // remove last dash
            }

            return sb.toString().replace("-", "+") + ")-";
        }

		@Override
		public void changeMonitor(final ComponentMonitor monitor) {
			super.changeMonitor(monitor);
			for (Injector<?> eachInjector : injectors) {
				if (eachInjector instanceof ComponentMonitorStrategy) {
					((ComponentMonitorStrategy)eachInjector).changeMonitor(monitor);
				}
			}
		}

    }
}