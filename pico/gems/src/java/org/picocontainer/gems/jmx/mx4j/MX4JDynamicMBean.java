/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by James Strachan                                           *
 *****************************************************************************/
package org.picocontainer.gems.jmx.mx4j;

import javax.management.MBeanInfo;

import mx4j.AbstractDynamicMBean;


/**
 * DynamicMBean implementation based on MX4J.
 * @author James Strachan
 * @author Michael Ward
 */
public class MX4JDynamicMBean extends AbstractDynamicMBean {

    /**
     * Construct a MBean from an instance and a MBeanInfo.
     * @param componentInstance the instance to expose.
     * @param mBeanInfo the MBeanInfo for the instance.
     */
    public MX4JDynamicMBean(final Object componentInstance, final MBeanInfo mBeanInfo) {
        setResource(componentInstance);
        setMBeanInfo(mBeanInfo);
    }
}
