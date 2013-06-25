/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/

package com.picocontainer.gems.jmx;

import javax.management.DynamicMBean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.PicoContainer;


/**
 * DynamicMBeanProvider, that will provide a component directly if it is already a {@link DynamicMBean}.
 * @author J&ouml;rg Schaible
 */
public class DynamicMBeanComponentProvider implements DynamicMBeanProvider {

    private final ObjectNameFactory objectNameFactory;

    /**
     * Construct a DynamicMBeanComponentProvider. This instance will use a {@link TypedObjectNameFactory} and register
     * all MBeans in the default domain of the {@link javax.management.MBeanServer}.
     */
    public DynamicMBeanComponentProvider() {
        this(new TypedObjectNameFactory());
    }

    /**
     * Construct a DynamicMBeanComponentProvider with a specified ObjectNameFactory.
     * @param factory The {@link ObjectNameFactory}.
     */
    public DynamicMBeanComponentProvider(final ObjectNameFactory factory) {
        if (factory == null) {
            throw new NullPointerException("ObjectFactoryName is null");
        }
        objectNameFactory = factory;
    }

    /**
     * Provide the component itself as {@link DynamicMBean} if it is one and if an {@link ObjectName} can be created.
     * @see com.picocontainer.gems.jmx.DynamicMBeanProvider#provide(com.picocontainer.PicoContainer,
     *      com.picocontainer.ComponentAdapter)
     */
    public JMXRegistrationInfo provide(final PicoContainer picoContainer, final ComponentAdapter componentAdapter) {
        if (DynamicMBean.class.isAssignableFrom(componentAdapter.getComponentImplementation())) {
            final DynamicMBean mBean = (DynamicMBean)componentAdapter.getComponentInstance(picoContainer,null);
            try {
                final ObjectName objectName = objectNameFactory.create(componentAdapter.getComponentKey(), mBean);
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
}
