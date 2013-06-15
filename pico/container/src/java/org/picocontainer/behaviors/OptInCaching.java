/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.behaviors;

import java.util.Properties;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

/**
 * Behavior that turns off Caching behavior by default.
 * <p>Example:</p>
 * <pre>
 * 		import org.picocontainer.*;
 * 		import static org.picocontainer.Characteristics.*;
 *
 * 		MutablePicoContainer mpc = new PicoBuilder().withBehaviors(new OptInCaching()).build();
 * 		mpc.addComponent(Map.class, HashMap.class) //Multiple Instances, no Caching.
 * 		mpc.as(CACHE).addComponent(Set.class, HashSet.class) //Single Cached Instance.
 * </pre>
 * @author Aslak Helles&oslash;y
 * @author <a href="Rafal.Krzewski">rafal@caltha.pl</a>
 */
@SuppressWarnings("serial")
public class OptInCaching extends AbstractBehavior {

    @Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps, final Object key,
    			final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        if (removePropertiesIfPresent(componentProps, Characteristics.CACHE)) {
            return monitor.changedBehavior(new Caching.Cached<T>(
                    super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams)));
        }
        removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE);
        return super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
    }


    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                                                       final Properties componentProps, final ComponentAdapter<T> adapter) {
        if (removePropertiesIfPresent(componentProps, Characteristics.CACHE)) {
            return monitor.changedBehavior(new Caching.Cached<T>(super.addComponentAdapter(monitor, lifecycle, componentProps, adapter)));
        }
        removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE);
        return super.addComponentAdapter(monitor, lifecycle, componentProps, adapter);
    }
}