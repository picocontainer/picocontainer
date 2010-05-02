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
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.annotations.Inject;

import java.util.Properties;
import java.lang.annotation.Annotation;

import static org.picocontainer.injectors.AnnotatedMethodInjection.getInjectionAnnotation;

/**
 * A {@link org.picocontainer.InjectionType} for Guice-style annotated fields.
 * The factory creates {@link AnnotatedFieldInjector}.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class AnnotatedFieldInjection extends AbstractInjectionType {

	private final Class<? extends Annotation>[] injectionAnnotations;

    public AnnotatedFieldInjection(Class<? extends Annotation>... injectionAnnotations) {
        this.injectionAnnotations = injectionAnnotations;
    }

    public AnnotatedFieldInjection() {
        this(Inject.class, getInjectionAnnotation("javax.inject.Inject"));
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                                   LifecycleStrategy lifecycleStrategy,
                                                   Properties componentProps,
                                                   Object key,
                                                   Class<T> impl,
                                                   Parameter... parameters) throws PicoCompositionException {
        boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
        return wrapLifeCycle(monitor.newInjector(new AnnotatedFieldInjector(key, impl, parameters, monitor,
                useNames, injectionAnnotations)), lifecycleStrategy);
    }
}
