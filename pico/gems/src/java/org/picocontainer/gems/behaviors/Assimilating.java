/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/

package org.picocontainer.gems.behaviors;

import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.factory.StandardProxyFactory;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.behaviors.AbstractBehaviorFactory;
import org.picocontainer.ComponentFactory;

import java.util.Properties;


/**
 * Factory for the Assimilated. This factory will create {@link Assimilated} instances for all
 * {@link ComponentAdapter} instances created by the delegate. This will assimilate every component for a specific type.
 * (TODO Since assimilating is taking types that are not the result type, does this mean that we cannot use generics)
 * 
 * @author J&ouml;rg Schaible
 * for this type?  I've been unable to actually get it working.
 */
@SuppressWarnings("serial")
public class Assimilating extends AbstractBehaviorFactory {

	private final ProxyFactory proxyFactory;
    private final Class<?> assimilationType;

    /**
     * Construct an Assimilating. The instance will use the {@link StandardProxyFactory} using the JDK
     * implementation.
     * 
     * @param type The assimilated type.
     */
    public Assimilating(final Class<?> type) {
        this(type, new StandardProxyFactory());
    }

    /**
     * Construct an Assimilating using a special {@link ProxyFactory}.
     * 
     * @param type The assimilated type.
     * @param proxyFactory The proxy factory to use.
     */
    public Assimilating(final Class<?> type, final ProxyFactory proxyFactory) {
        this.assimilationType = type;
        this.proxyFactory = proxyFactory;
    }

    /**
     * Create a {@link Assimilated}. This adapter will wrap the returned {@link ComponentAdapter} of the
     * deleated {@link ComponentFactory}.
     * 
     * @see ComponentFactory#createComponentAdapter(ComponentMonitor,LifecycleStrategy,Properties,Object,Class,Parameter...)
     */
	@Override
	public ComponentAdapter createComponentAdapter(
            final ComponentMonitor componentMonitor, final LifecycleStrategy lifecycleStrategy, final Properties componentProperties, final Object componentKey, final Class componentImplementation, final Parameter... parameters)
            throws PicoCompositionException {
        return componentMonitor.newBehavior(new Assimilated(assimilationType, super.createComponentAdapter(
                componentMonitor, lifecycleStrategy, componentProperties, componentKey, componentImplementation, parameters), proxyFactory));
    }


    @Override
	public ComponentAdapter addComponentAdapter(final ComponentMonitor componentMonitor,
                                                final LifecycleStrategy lifecycleStrategy,
                                                final Properties componentProperties,
                                                final ComponentAdapter adapter) {
        return componentMonitor.newBehavior(new Assimilated(assimilationType, super.addComponentAdapter(componentMonitor,
                                         lifecycleStrategy,
                                         componentProperties,
                                         adapter)));
    }
}

