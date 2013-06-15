/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.picocontainer.injectors.AnnotatedMethodInjection.getInjectionAnnotation;

import java.util.Properties;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.annotations.Inject;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

/** @author Paul Hammant */
@SuppressWarnings("serial")
public class MultiInjection extends AbstractInjectionType {
    private final String setterPrefix;

    public MultiInjection(final String setterPrefix) {
        this.setterPrefix = setterPrefix;
    }

    public MultiInjection() {
        this("set");
    }

    public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor,
                                                          final LifecycleStrategy lifecycle,
                                                          final Properties componentProps,
                                                          final Object key,
                                                          final Class<T> impl,
                                                          final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
        boolean requireConsumptionOfAllParameters = !(AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.ALLOW_UNUSED_PARAMETERS, false));

        return wrapLifeCycle(new MultiInjector<T>(key, impl, monitor, setterPrefix, useNames, requireConsumptionOfAllParameters, constructorParams, fieldParams, methodParams), lifecycle);
    }

    /** @author Paul Hammant */
    @SuppressWarnings("serial")
    public static class MultiInjector<T> extends CompositeInjection.CompositeInjector<T> {

        @SuppressWarnings("unchecked")
		public MultiInjector(final Object key, final Class<T> impl, final ComponentMonitor monitor, final String setterPrefix, final boolean useNames, final boolean useAllParameter,
        		final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) {
            super(key, impl, monitor, useNames,
                    monitor.newInjector(new ConstructorInjection.ConstructorInjector<T>(monitor, useNames, key, impl, constructorParams)),
                    monitor.newInjector(new SetterInjection.SetterInjector<T>(key, impl, monitor, setterPrefix, useNames, "", false, methodParams)),
                    monitor.newInjector(new AnnotatedMethodInjection.AnnotatedMethodInjector<T>(key, impl, methodParams, monitor, useNames, useAllParameter, Inject.class, getInjectionAnnotation("javax.inject.Inject"))),
                    monitor.newInjector(new AnnotatedFieldInjection.AnnotatedFieldInjector<T>(key, impl, fieldParams, monitor, useNames, useAllParameter, Inject.class, getInjectionAnnotation("javax.inject.Inject")))
           );

        }

        @Override
		public String getDescriptor() {
            return "MultiInjector";
        }
    }
}
