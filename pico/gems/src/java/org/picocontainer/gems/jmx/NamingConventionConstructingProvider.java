/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/

package org.picocontainer.gems.jmx;

import javax.management.MBeanInfo;


/**
 * A DynamicMBeanProvider that constructs StandardMBean instances that follow the JMX naming conventions. The name of
 * the management interface must follow the naming conventions with an <em>MBean</em> appended to the MBean's type.
 * The implementation will use the registered MBeanInfoProvider instances of type
 * {@link ComponentKeyConventionMBeanInfoProvider} and {@link ComponentTypeConventionMBeanInfoProvider} to provide a
 * {@link MBeanInfo} for the component's MBean. If a {@link MBeanInfo} was found, the MBean's type is used from the
 * MBeanInfo otherwise the type is the implementation class of the component.
 * @author J&ouml;rg Schaible
 */
public class NamingConventionConstructingProvider extends AbstractConstructingProvider {

    private final ObjectNameFactory objectNameFactory;
    private final MBeanInfoProvider[] mBeanProviders;
    private final StandardMBeanFactory mBeanFactory;

    /**
     * Construct a NamingConventionConstructingProvider. Following {@link MBeanInfoProvider} instances are registered
     * with this constructor:
     * <ul>
     * <li>{@link ComponentKeyConventionMBeanInfoProvider}</li>
     * <li>{@link ComponentTypeConventionMBeanInfoProvider}</li>
     * </ul>
     * @param factory The ObjectNameFactory used to name the created MBeans.
     */
    public NamingConventionConstructingProvider(final ObjectNameFactory factory) {
        if (factory == null) {
            throw new NullPointerException("ObjectNameFactory is null");
        }
        mBeanFactory = new StandardMBeanFactory();
        objectNameFactory = factory;
        mBeanProviders = new MBeanInfoProvider[]{
                new ComponentKeyConventionMBeanInfoProvider(), new ComponentTypeConventionMBeanInfoProvider()};
    }

    /**
     * Return a {@link StandardMBeanFactory}.
     * @see org.picocontainer.gems.jmx.AbstractConstructingProvider#getMBeanFactory()
     */
    @Override
	protected DynamicMBeanFactory getMBeanFactory() {
        return mBeanFactory;
    }

    /**
     * @see org.picocontainer.gems.jmx.AbstractConstructingProvider#getObjectNameFactory()
     */
    @Override
	public ObjectNameFactory getObjectNameFactory() {
        return objectNameFactory;
    }

    /**
     * Return an array with an instance of type {@link ComponentKeyConventionMBeanInfoProvider} and
     * {@link ComponentTypeConventionMBeanInfoProvider}.
     * @see org.picocontainer.gems.jmx.AbstractConstructingProvider#getMBeanInfoProviders()
     */
    @Override
	public MBeanInfoProvider[] getMBeanInfoProviders() {
        return mBeanProviders;
    }

    /**
     * Determin the default management interface using naming convetions of the JMX specification.
     * @param implementation The type of the component's implementation.
     * @param mBeanInfo The {@link MBeanInfo} to expose the component. May be <code>null</code>.
     * @return Returns the management interface.
     * @throws ClassNotFoundException Thrown if no interface can be determined.
     */
    @Override
	protected Class getManagementInterface(final Class implementation, final MBeanInfo mBeanInfo)
            throws ClassNotFoundException {
        return mBeanFactory.getDefaultManagementInterface(implementation, mBeanInfo);
    }
}
