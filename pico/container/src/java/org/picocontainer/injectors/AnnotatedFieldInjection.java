/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.Characteristics;
import org.picocontainer.behaviors.AbstractBehaviorFactory;
import org.picocontainer.annotations.Inject;

import java.util.Properties;
import java.lang.annotation.Annotation;

/**
 * A {@link org.picocontainer.InjectionFactory} for Guice-style annotated fields.
 * The factory creates {@link AnnotatedFieldInjector}.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class AnnotatedFieldInjection extends AbstractInjectionFactory {

	private final Class<? extends Annotation> injectionAnnotation;

    public AnnotatedFieldInjection(Class<? extends Annotation> injectionAnnotation) {
        this.injectionAnnotation = injectionAnnotation;
    }

    public AnnotatedFieldInjection() {
        this(Inject.class);
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                                   LifecycleStrategy lifecycleStrategy,
                                                   Properties componentProperties,
                                                   Object componentKey,
                                                   Class<T> componentImplementation,
                                                   Parameter... parameters) throws PicoCompositionException {
        boolean useNames = AbstractBehaviorFactory.arePropertiesPresent(componentProperties, Characteristics.USE_NAMES, true);
        return wrapLifeCycle(monitor.newInjector(new AnnotatedFieldInjector(componentKey, componentImplementation, parameters, monitor,
                                          injectionAnnotation, useNames)), lifecycleStrategy);
    }
}
