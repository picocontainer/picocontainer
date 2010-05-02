/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Michael Ward                                    		 *
 *****************************************************************************/

package org.picocontainer.gems.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.ObjectName;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;


/**
 * A DynamicMBeanProvider, that creates DynamicMBeans for registered Pico components on the fly.
 * @author Michael Ward
 * @author J&ouml;rg Schaible
 */
public class RegisteredMBeanConstructingProvider implements DynamicMBeanProvider {

    private final DynamicMBeanFactory factory;
    private final Map registry;

    /**
     * Construct a RegisteredMBeanConstructingProvider with a {@link StandardMBeanFactory} as default.
     */
    public RegisteredMBeanConstructingProvider() {
        this(new StandardMBeanFactory());
    }

    /**
     * Construct a RegisteredMBeanConstructingProvider, that uses a specific {@link DynamicMBeanFactory}.
     * @param factory
     */
    public RegisteredMBeanConstructingProvider(final DynamicMBeanFactory factory) {
        this.factory = factory;
        this.registry = new HashMap();
    }

    /**
     * Provide a DynamicMBean for the given Pico component. The implementation will lookup the component's key in the
     * internal registry. Only components that were registered with additional information will be considered and a
     * {@link DynamicMBean} will be created for them using the {@link DynamicMBeanFactory}. If the component key is of
     * type class, it is used as management interface.
     * @see org.picocontainer.gems.jmx.DynamicMBeanProvider#provide(PicoContainer, ComponentAdapter)
     */
    public JMXRegistrationInfo provide(final PicoContainer picoContainer, final ComponentAdapter componentAdapter) {
        final Object key = componentAdapter.getComponentKey();
        final MBeanInfoWrapper wrapper = (MBeanInfoWrapper)registry.get(key);
        if (wrapper != null) {
            final Object instance = componentAdapter.getComponentInstance(picoContainer, ComponentAdapter.NOTHING.class);
            final Class management = wrapper.getManagementInterface() != null
                                                                             ? wrapper.getManagementInterface()
                                                                             : key instanceof Class
                                                                                                   ? (Class)key
                                                                                                   : instance
                                                                                                           .getClass();
            final DynamicMBean mBean = factory.create(instance, management, wrapper.getMBeanInfo());
            return new JMXRegistrationInfo(wrapper.getObjectName(), mBean);
        }
        return null;
    }

    /**
     * Register a specific Pico component by key with an MBeanInfo and an ObjectName.
     * @param key The key of the Pico component.
     * @param objectName The {@link ObjectName} of the MBean.
     * @param management The management interface.
     * @param mBeanInfo The {@link MBeanInfo} of the MBean.
     */
    public void register(
            final Object key, final ObjectName objectName, final Class management, final MBeanInfo mBeanInfo) {
        registry.put(key, new MBeanInfoWrapper(mBeanInfo, objectName, management));
    }

    /**
     * Register a specific Pico component by key with an MBeanInfo and an ObjectName.
     * @param key The key of the Pico component.
     * @param objectName The {@link ObjectName} of the MBean.
     * @param mBeanInfo The {@link MBeanInfo} of the MBean.
     */
    public void register(final Object key, final ObjectName objectName, final MBeanInfo mBeanInfo) {
        register(key, objectName, null, mBeanInfo);
    }

    /**
     * Register a specific Pico component with an MBeanInfo and an ObjectName. The implementation class of the
     * {@link DynamicMBean} must be the key of the Pico component.
     * @param objectName The {@link ObjectName} of the MBean.
     * @param mBeanInfo The {@link MBeanInfo} of the MBean.
     */
    public void register(final ObjectName objectName, final MBeanInfo mBeanInfo) {
        try {
            register(getClass().getClassLoader().loadClass(mBeanInfo.getClassName()), objectName, mBeanInfo);
        } catch (final ClassNotFoundException e) {
            throw new JMXRegistrationException("Cannot access class " + mBeanInfo.getClassName() + " of MBean", e);
        }
    }

    /**
     * Register a specific Pico component by key with an ObjectName.
     * @param key The key of the Pico component.
     * @param objectName The {@link ObjectName} of the MBean.
     */
    public void register(final Object key, final ObjectName objectName) {
        registry.put(key, new MBeanInfoWrapper(null, objectName, null));
    }

    /**
     * Simple wrapper to tie a MBeanInfo to an ObjectName
     */
    private static class MBeanInfoWrapper {
        private final MBeanInfo mBeanInfo;
        private final ObjectName objectName;
        private final Class managementInterface;

        MBeanInfoWrapper(final MBeanInfo mBeanInfo, final ObjectName objectName, final Class management) {
            this.mBeanInfo = mBeanInfo;
            this.objectName = objectName;
            this.managementInterface = management;
        }

        MBeanInfo getMBeanInfo() {
            return mBeanInfo;
        }

        ObjectName getObjectName() {
            return objectName;
        }

        Class getManagementInterface() {
            return managementInterface;
        }
    }

}
