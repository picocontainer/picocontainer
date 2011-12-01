/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Michael Ward                                    		 *
 *****************************************************************************/

package org.picocontainer.gems.jmx;

import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.StandardMBean;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;


/**
 * A factory for DynamicMBeans, that creates MBean instances using the classes {@link StandardMBean} and
 * {@link ModelMBean} provided by the JMX specification. The implementation offers special support for StandardMBeans
 * following the naming convention for their management interface using the class name of the component with an appended
 * <em>MBean</em>.
 * @author Michael Ward
 * @author J&ouml;rg Schaible
 */
public class StandardMBeanFactory implements DynamicMBeanFactory {

    /**
     * Create a StandardMBean for the component.
     * @param componentInstance {@inheritDoc}
     * @param management The management interface. If <code>null</code> the implementation will use the interface
     *            complying with the naming convention for management interfaces.
     * @param mBeanInfo The {@link MBeanInfo} to use. If <code>null</code> the {@link StandardMBean} will use an
     *            automatically generated one.
     * @return Returns a {@link StandardMBean}. If the <strong>mBeanInfo</strong> was not null, it is an instance of a
     *         {@link StandardNanoMBean}.
     * @see org.picocontainer.gems.jmx.DynamicMBeanFactory#create(java.lang.Object, java.lang.Class,
     *      javax.management.MBeanInfo)
     */
    public DynamicMBean create(final Object componentInstance, final Class management, final MBeanInfo mBeanInfo) {
        try {
            if (mBeanInfo == null) {
                final Class managementInterface = getManagementInterface(componentInstance.getClass(), management, null);
                return new StandardMBean(componentInstance, managementInterface);
            } else if (mBeanInfo instanceof ModelMBeanInfo) {
                final ModelMBean mBean = new RequiredModelMBean((ModelMBeanInfo)mBeanInfo);
                try {
                    mBean.setManagedResource(componentInstance, "ObjectReference");
                } catch (final InvalidTargetObjectTypeException e) {
                    // N/A: "ObjectReference" is a valid reference type
                } catch (final InstanceNotFoundException e) {
                    // N/A: the instance was a valid object
                }
                return mBean;
            } else {
                final Class<?> managementInterface = getManagementInterface(
                        componentInstance.getClass(), management, mBeanInfo);
                return new StandardNanoMBean(componentInstance, managementInterface, mBeanInfo);
            }
        } catch (final ClassNotFoundException e) {
            throw new JMXRegistrationException("Cannot load management interface for StandardMBean", e);
        } catch (final NotCompliantMBeanException e) {
            throw new JMXRegistrationException("Cannot create StandardMBean", e);
        } catch (final RuntimeOperationsException e) {
            throw new JMXRegistrationException("Cannot create ModelMBean", e);
        } catch (final MBeanException e) {
            throw new JMXRegistrationException("Cannot create ModelMBean", e);
        }
    }

    private Class getManagementInterface(final Class type, final Class management, final MBeanInfo mBeanInfo)
            throws ClassNotFoundException {
        final Class managementInterface;
        if (management == null) {
            managementInterface = getDefaultManagementInterface(type, mBeanInfo);
        } else {
            managementInterface = management;
        }
        return managementInterface;
    }

    /**
     * Determin the management interface for the given type. The class name of the given type is used as class name of
     * the mBean unless the caller has provided a {@link MBeanInfo}, the class name of the MBean is retrieved a
     * MBeanInfo that defines this name. Following the naming conventions is the name of the management interface the
     * same as the class name of the MBean with an appended <em>MBean</em>. The {@link ClassLoader} of the type is
     * used to load the interface type.
     * @param type The class of the MBean.
     * @param mBeanInfo The {@link MBeanInfo} for the MBean. May be <code>null</code>.
     * @return Returns the default management interface.
     * @throws ClassNotFoundException If the management interface cannot be found.
     */
    public Class getDefaultManagementInterface(final Class type, final MBeanInfo mBeanInfo)
            throws ClassNotFoundException {
        final ClassLoader classLoader = type.getClassLoader() != null ? type.getClassLoader() : Thread.currentThread()
                .getContextClassLoader();
        return classLoader.loadClass((mBeanInfo == null ? type.getName() : mBeanInfo.getClassName()) + "MBean");
    }
}
