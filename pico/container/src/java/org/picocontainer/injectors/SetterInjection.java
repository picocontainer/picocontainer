/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.injectors;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.behaviors.AbstractBehavior;

import java.util.Properties;


/**
 * A {@link org.picocontainer.InjectionType} for JavaBeans.
 * The factory creates {@link SetterInjector}.
 *
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public class SetterInjection extends AbstractInjectionType {

    private final String prefix;

    public SetterInjection(String prefix) {
        this.prefix = prefix;
    }

    public SetterInjection() {
        this("set");
    }

    /**
     * Create a {@link SetterInjector}.
     * 
     * @param monitor
     * @param lifecycleStrategy
     * @param componentProperties
     * @param componentKey The component's key
     * @param componentImplementation The class of the bean.
     * @param parameters Any parameters for the setters. If null the adapter
     *            solves the dependencies for all setters internally. Otherwise
     *            the number parameters must match the number of the setter.
     * @return Returns a new {@link SetterInjector}.
     * @throws PicoCompositionException if dependencies cannot be solved
     */
    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycleStrategy, Properties componentProperties, Object componentKey, Class<T> componentImplementation, Parameter... parameters)
            throws PicoCompositionException {
        boolean useNames = AbstractBehavior.arePropertiesPresent(componentProperties, Characteristics.USE_NAMES, true);
        return wrapLifeCycle(monitor.newInjector(new SetterInjector(componentKey, componentImplementation, parameters, monitor, prefix, useNames)), lifecycleStrategy);
    }

}
