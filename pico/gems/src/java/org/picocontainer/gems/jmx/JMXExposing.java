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

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.gems.GemsCharacteristics;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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
            final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps, final Object key, final Class<T> impl, final Parameter... parameters)
            throws PicoCompositionException {
        final ComponentAdapter<T> delegateAdapter = super.createComponentAdapter(
                monitor, lifecycle,
                componentProps, key, impl, parameters);
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.NO_JMX)) {
            return delegateAdapter;            
        } else {        	
        	AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.JMX);
            return monitor.changedBehavior(new JMXExposed<T>(delegateAdapter, mBeanServer, providers));
        }
    }


    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor,
                                                final LifecycleStrategy lifecycle,
                                                final Properties componentProps,
                                                final ComponentAdapter<T> adapter) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.NO_JMX)) {
            return super.addComponentAdapter(monitor,
                                             lifecycle,
                                             componentProps,
                                             adapter);
        } else {
            return monitor.changedBehavior(new JMXExposed<T>(super.addComponentAdapter(monitor,
                                                                     lifecycle,
                                                                     componentProps,
                                                                     adapter), mBeanServer, providers));
        }

    }

    /**
     * {@link org.picocontainer.ComponentAdapter} that is exposing a component as MBean in a MBeanServer.
     * @author J&ouml;rg Schaible
     */
    @SuppressWarnings("serial")
    public static class JMXExposed<T> extends AbstractChangedBehavior<T> {


        private final MBeanServer mBeanServer;
        private final DynamicMBeanProvider[] providers;
        private List<ObjectName> registeredObjectNames;

        /**
         * Construct a JMXExposed behaviour
         * @param delegate The delegated {@link org.picocontainer.ComponentAdapter}.
         * @param mBeanServer The {@link javax.management.MBeanServer} used for registering the MBean.
         * @param providers An array with providers for converting the component instance into a
         *            {@link javax.management.DynamicMBean}.
         * @throws NullPointerException Thrown if the {@link javax.management.MBeanServer} or the array with the {@link org.picocontainer.gems.jmx.DynamicMBeanProvider}
         *             instances is null.
         */
        public JMXExposed(
                final ComponentAdapter<T> delegate, final MBeanServer mBeanServer, final DynamicMBeanProvider[] providers)
                throws NullPointerException {
            super(delegate);
            if (mBeanServer == null || providers == null) {
                throw new NullPointerException();
            }
            this.mBeanServer = mBeanServer;
            this.providers = providers;
        }

        /**
         * Construct a JMXExposed behaviour. This instance uses a {@link org.picocontainer.gems.jmx.DynamicMBeanComponentProvider} as default to
         * register any component instance in the {@link javax.management.MBeanServer}, that is already a
         * {@link javax.management.DynamicMBean}.
         * @param delegate The delegated {@link org.picocontainer.ComponentAdapter}.
         * @param mBeanServer The {@link javax.management.MBeanServer} used for registering the MBean.
         * @throws NullPointerException Thrown if the {@link javax.management.MBeanServer} or the array with the {@link org.picocontainer.gems.jmx.DynamicMBeanProvider}
         *             instances is null.
         */
        public JMXExposed(final ComponentAdapter<T> delegate, final MBeanServer mBeanServer)
                throws NullPointerException {
            this(delegate, mBeanServer, new DynamicMBeanProvider[]{new DynamicMBeanComponentProvider()});
        }

        /**
         * Retrieve the component instance. The implementation will automatically register it in the {@link javax.management.MBeanServer},
         * if a provider can return a {@link javax.management.DynamicMBean} for it.
         * <p>
         * Note, that you will have to wrap this {@link org.picocontainer.ComponentAdapter} with a {@link org.picocontainer.behaviors.Caching.Cached} to avoid
         * the registration of the same component again.
         * </p>
         * @throws org.picocontainer.PicoCompositionException Thrown by the delegate or if the registering of the
         *             {@link javax.management.DynamicMBean} in the {@link javax.management.MBeanServer } fails.
         * @see org.picocontainer.behaviors.AbstractBehavior.AbstractChangedBehavior#getComponentInstance(org.picocontainer.PicoContainer, Class)
         */
        @Override
        public T getComponentInstance(final PicoContainer container, final Type into)
                throws PicoCompositionException
        {
            final ComponentAdapter<T> componentAdapter = new Caching.Cached<T>(getDelegate());

            final T componentInstance = componentAdapter.getComponentInstance(container, into);

            for (DynamicMBeanProvider provider : providers) {
                final JMXRegistrationInfo info = provider.provide(container, componentAdapter);
                if (info != null) {
                    Exception exception = null;
                    try {
                        mBeanServer.registerMBean(info.getMBean(), info.getObjectName());
                    } catch (final InstanceAlreadyExistsException e) {
                        exception = e;
                    } catch (final MBeanRegistrationException e) {
                        exception = e;
                    } catch (final NotCompliantMBeanException e) {
                        exception = e;
                    }
                    if (null == registeredObjectNames) {
                        registeredObjectNames = new ArrayList<ObjectName>();
                    }
                    registeredObjectNames.add(info.getObjectName());
                    if (exception != null) {
                        throw new PicoCompositionException("Registering MBean failed", exception);
                    }
                }
            }
            return componentInstance;
        }

        public String getDescriptor() {
            return "ExposedJMX";
        }

        @Override
        public void dispose(final Object component) {
            if( null != registeredObjectNames ) {
                for (Object registeredObjectName : registeredObjectNames) {
                    try {
                        mBeanServer.unregisterMBean((ObjectName)registeredObjectName);
                    } catch (InstanceNotFoundException e) {
                        throw new JMXRegistrationException(e);
                    } catch (MBeanRegistrationException e) {
                        throw new JMXRegistrationException(e);
                    }
                }
            }

            if( super.hasLifecycle( getComponentImplementation( ) ) ) {
                super.dispose(component);
            }
        }

        @Override
        public boolean hasLifecycle( final Class<?> type ) {
            return true;
        }

    }
}
