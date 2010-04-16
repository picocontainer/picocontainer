/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/

package org.picocontainer.gems.jmx;

import javax.management.MBeanServer;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.gems.GemsCharacteristics;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;

import java.lang.management.ManagementFactory;
import java.util.Properties;


/**
 * {@link org.picocontainer.ComponentFactory} that instantiates {@link JMXExposed} instances.
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public class JMXExposing extends AbstractBehavior {

	
	private final MBeanServer mBeanServer;
    private final DynamicMBeanProvider[] providers;

    /**
     * Constructs a JMXExposingComponentFactory that uses the system default MBean Server.
     * @since PicoContainer-Gems 2.4
     */
    public JMXExposing() {
    	this(ManagementFactory.getPlatformMBeanServer());
    }
    
    
    /**
     * Construct a JMXExposingComponentFactory.
     * @param mBeanServer The {@link MBeanServer} used for registering the MBean.
     * @param providers An array with providers for converting the component instance into a
     *            {@link javax.management.DynamicMBean}.
     * @throws NullPointerException Thrown if the {@link MBeanServer} or the array with the {@link DynamicMBeanProvider}
     *             instances is null.
     */
    public JMXExposing(
            final MBeanServer mBeanServer,
            final DynamicMBeanProvider[] providers) throws NullPointerException {
        if (mBeanServer == null || providers == null) {
            throw new NullPointerException();
        }
        this.mBeanServer = mBeanServer;
        this.providers = providers;
    }

    /**
     * Construct a JMXExposingComponentFactory. This instance uses a {@link DynamicMBeanComponentProvider} as
     * default to register any component instance in the {@link MBeanServer}, that is already a
     * {@link javax.management.DynamicMBean}.
     * @param mBeanServer The {@link MBeanServer} used for registering the MBean.
     * @throws NullPointerException Thrown if the {@link MBeanServer} or the array with the {@link DynamicMBeanProvider}
     *             instances is null.
     */
    public JMXExposing(final MBeanServer mBeanServer)
            throws NullPointerException {
        this(mBeanServer, new DynamicMBeanProvider[]{new DynamicMBeanComponentProvider()});
    }
    

    /**
     * Retrieve a {@link ComponentAdapter}. Wrap the instance retrieved by the delegate with an instance of a
     * {@link JMXExposed}.
     * @see org.picocontainer.ComponentFactory#createComponentAdapter(ComponentMonitor,LifecycleStrategy,Properties,Object,Class,Parameter...)
     */
    @Override
	public <T> ComponentAdapter<T> createComponentAdapter(
            final ComponentMonitor componentMonitor, final LifecycleStrategy lifecycleStrategy, final Properties componentProperties, final Object componentKey, final Class<T> componentImplementation, final Parameter... parameters)
            throws PicoCompositionException {
        final ComponentAdapter<T> delegateAdapter = super.createComponentAdapter(
                componentMonitor, lifecycleStrategy,
                componentProperties, componentKey, componentImplementation, parameters);
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.NO_JMX)) {
            return delegateAdapter;            
        } else {        	
        	AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.JMX);
            return componentMonitor.newBehavior(new JMXExposed<T>(delegateAdapter, mBeanServer, providers));
        }
    }


    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor componentMonitor,
                                                final LifecycleStrategy lifecycleStrategy,
                                                final Properties componentProperties,
                                                final ComponentAdapter<T> adapter) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, GemsCharacteristics.NO_JMX)) {
            return super.addComponentAdapter(componentMonitor,
                                             lifecycleStrategy,
                                             componentProperties,
                                             adapter);
        } else {
            return componentMonitor.newBehavior(new JMXExposed<T>(super.addComponentAdapter(componentMonitor,
                                                                     lifecycleStrategy,
                                                                     componentProperties,
                                                                     adapter), mBeanServer, providers));
        }

    }
}
