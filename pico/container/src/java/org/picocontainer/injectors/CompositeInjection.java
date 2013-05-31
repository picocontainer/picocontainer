/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.ComponentMonitorStrategy;
import org.picocontainer.InjectionType;
import org.picocontainer.Injector;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

import java.lang.reflect.Type;
import java.util.Properties;

/**
 * A Composite of other types on InjectionFactories - pass them into the varargs constructor.
 * 
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class CompositeInjection extends AbstractInjectionType {

    private final InjectionType[] injectionTypes;

    public CompositeInjection(InjectionType... injectionTypes) {
        this.injectionTypes = injectionTypes;
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                                          LifecycleStrategy lifecycle,
                                                          Properties componentProps,
                                                          Object key,
                                                          Class<T> impl,
                                                          ConstructorParameters constructorParams, 
                                                          FieldParameters[] fieldParams, 
                                                          MethodParameters[] methodParams) throws PicoCompositionException {

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

        public CompositeInjector(Object key, Class<?> impl, ComponentMonitor monitor,
                                 boolean useNames, Injector<T>... injectors) {
            super(key, impl, monitor, useNames);
            this.injectors = injectors;
        }

        @Override
        public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
	            T instance = null;
	            for (Injector<T> injector : injectors) {
	                if (instance == null) {
	                    instance = injector.getComponentInstance(container, NOTHING.class);
	                } else {
	                    injector.decorateComponentInstance(container, into, instance);
	                }
	            }
	            return (T) instance;
        }


        /**
         * @return the object returned is the result of the last of the injectors delegated to
         */
        @Override
        public Object decorateComponentInstance(PicoContainer container, Type into, T instance) {
            Object result = null;
            for (Injector<T> injector : injectors) {
                result = injector.decorateComponentInstance(container, into, instance);
            }
            return result;
        }

        @Override
        public void verify(PicoContainer container) throws PicoCompositionException {
            for (Injector<T> injector : injectors) {
                injector.verify(container);
            }
        }

        @Override
        public final void accept(PicoVisitor visitor) {
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
		public void changeMonitor(ComponentMonitor monitor) {
			super.changeMonitor(monitor);
			for (Injector<?> eachInjector : injectors) {
				if (eachInjector instanceof ComponentMonitorStrategy) {
					((ComponentMonitorStrategy)eachInjector).changeMonitor(monitor);
				}
			}
		}

    }
}