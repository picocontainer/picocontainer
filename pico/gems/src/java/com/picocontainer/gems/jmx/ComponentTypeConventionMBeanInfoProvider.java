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

import javax.management.MBeanInfo;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.PicoContainer;


/**
 * A MBeanInfoProvider that searches for a MBeanInfo instance in the PicoContainer. The key of the MBeanInfo is
 * calculated from the component type following naming conventions.
 * @author J&ouml;rg Schaible
 */
public class ComponentTypeConventionMBeanInfoProvider extends AbstractNamingConventionMBeanInfoProvider {

    /**
     * Use the key of the component to search for a MBeanInfo in the PicoContainer. The matching MBeanInfo must be
     * stored in the PicoContainer. The key of the MBeanInfo follows the naming scheme
     * &quot;&lt;ComponentKey&gt;MBeanInfo&quot;. The the component's key is a type, the class name is used as prefix
     * otherwise the string representation of the key. The key part may already end with &quot;MBean&quot; as it would
     * for components registered with the management interface as key, that follow the JMX naming conventions. As last
     * resort the calculated key of the MBeanInfo is turned into a type that is used again as lookup key.
     * @see com.picocontainer.gems.jmx.MBeanInfoProvider#provide(com.picocontainer.PicoContainer,
     *      com.picocontainer.ComponentAdapter)
     */
    public MBeanInfo provide(final PicoContainer picoContainer, final ComponentAdapter componentAdapter) {
        final Class mBeanType = componentAdapter.getComponentImplementation();
        final String mBeanInfoName = mBeanType.getName() + "MBeanInfo";
        return instantiateMBeanInfo(mBeanInfoName, picoContainer, mBeanType.getClassLoader());
    }

}
