/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Michael Ward                                    		 *
 *****************************************************************************/

package com.picocontainer.gems.jmx.mx4j;

import javax.management.DynamicMBean;
import javax.management.MBeanInfo;

import com.picocontainer.gems.jmx.StandardMBeanFactory;


/**
 * This is the a factory for creating DynamicMBean instances. However it is tied specifically to MX4J. Those not
 * interested in being dependent on MX4J should implement another Factory and register it to the container. The single
 * difference to the StandardMBeanFactory is, that it does not need a special management interface for a component to
 * expose.
 * @author Michael Ward
 */
public class MX4JDynamicMBeanFactory extends StandardMBeanFactory {

    /**
     * Create a MX4JDynamicMBean for the component. MX4J is only used, if management is <code>null</code>.
     * @see com.picocontainer.gems.jmx.StandardMBeanFactory#create(java.lang.Object, java.lang.Class,
     *      javax.management.MBeanInfo)
     */
    @Override
	public DynamicMBean create(final Object componentInstance, final Class management, final MBeanInfo mBeanInfo) {
        if (management != null || mBeanInfo == null) {
            return super.create(componentInstance, management, mBeanInfo);
        } else {
            return new MX4JDynamicMBean(componentInstance, mBeanInfo);
        }
    }
}
