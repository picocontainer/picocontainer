/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;

import java.util.Properties;

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

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycleStrategy, Properties componentProps, Object key,
    			Class<T> impl, Parameter... parameters)
            throws PicoCompositionException {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.CACHE)) {
            return monitor.newBehavior(new Caching.Cached<T>(super.createComponentAdapter(monitor,
                                                                                        lifecycleStrategy,
                                                                                        componentProps,
                                                                                        key,
                                                                                        impl,
                                                                                        parameters)));
        }
        AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE);
        return super.createComponentAdapter(monitor, lifecycleStrategy,
                                            componentProps, key, impl, parameters);
    }


    public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor monitor,
                                                LifecycleStrategy lifecycleStrategy,
                                                Properties componentProps,
                                                ComponentAdapter<T> adapter) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.CACHE)) {
            return monitor.newBehavior(new Caching.Cached<T>(super.addComponentAdapter(monitor,
                                                                 lifecycleStrategy,
                                                                 componentProps,
                                                                 adapter)));
        }
        AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE);
        return super.addComponentAdapter(monitor,
                                         lifecycleStrategy,
                                         componentProps,
                                         adapter);
    }
}