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

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;

import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


/**
 * A DynamicMBeanProvider that constructs StandardMBean instances that as long as an ObjectName and a MBeanInfo can be
 * generated for the component.
 * @author J&ouml;rg Schaible
 */
public abstract class AbstractConstructingProvider implements DynamicMBeanProvider {

    /**
     * Create a StandardMBean from the component provided by the ComponentAdapter. One of the registered
     * {@link MBeanInfoProvider} instances must provide a {@link MBeanInfo} for the component and the registered
     * {@link ObjectNameFactory} has to provide a proper {@link ObjectName}.
     * <p>
     * Note: An instance of the component is only created, if a management interface is available.
     * </p>
     * @see org.picocontainer.gems.jmx.DynamicMBeanProvider#provide(org.picocontainer.PicoContainer,
     *      org.picocontainer.ComponentAdapter)
     */
    public JMXRegistrationInfo provide(final PicoContainer picoContainer, final ComponentAdapter componentAdapter) {

        // locate MBeanInfo
        MBeanInfo mBeanInfo = null;
        MBeanInfoProvider[] mBeanInfoProviders = getMBeanInfoProviders();
        for (int i = 0; i < mBeanInfoProviders.length && mBeanInfo == null; ++i) {
            mBeanInfo = mBeanInfoProviders[i].provide(picoContainer, componentAdapter);
        }

		Class management = null;
		try {
		// throws ClassNotFoundException if not successful
			 management = getManagementInterface(componentAdapter.getComponentImplementation(), mBeanInfo);
		} catch (final ClassNotFoundException e) {
			// No management interface available
		}

		if( management != null || mBeanInfo != null ) {
			try {
				// create MBean
				final DynamicMBean mBean = getMBeanFactory().create(
						componentAdapter.getComponentInstance(picoContainer,null), management, mBeanInfo);
				final ObjectName objectName = getObjectNameFactory().create(componentAdapter.getComponentKey(), mBean);
				if (objectName != null) {
					return new JMXRegistrationInfo(objectName, mBean);
				}
			} catch (final MalformedObjectNameException e) {
				throw new JMXRegistrationException("Cannot create ObjectName for component '"
						+ componentAdapter.getComponentKey()
						+ "'", e);
			}
		}
		return null;
    }

    /**
     * @return Returns the {@link DynamicMBeanFactory} to use.
     */
    protected abstract DynamicMBeanFactory getMBeanFactory();

    /**
     * Deliver the ObjectNameFactory used to provide the {@link ObjectName} instances registering the MBeans.
     * @return Return the {@link ObjectNameFactory} instance.
     */
    protected abstract ObjectNameFactory getObjectNameFactory();

    /**
     * Deliver the MBeanInfoProvider instances to use. The instances are used in the delivered sequence to retrieve a
     * {@link MBeanInfo} for a MBean to create. It is valid for an implementation to return an empty array.
     * @return Return an array of {@link MBeanInfoProvider} instances.
     */
    protected abstract MBeanInfoProvider[] getMBeanInfoProviders();

    /**
     * Determin the management interface from the component implementation type and an optional MBeanInfo instance.
     * @param implementation The type of the component's implementation.
     * @param mBeanInfo The {@link MBeanInfo} to expose the component. May be <code>null</code>.
     * @return Returns the management interface.
     * @throws ClassNotFoundException Thrown if no interface can be determined.
     */
    protected abstract Class getManagementInterface(final Class implementation, final MBeanInfo mBeanInfo)
            throws ClassNotFoundException;
}
