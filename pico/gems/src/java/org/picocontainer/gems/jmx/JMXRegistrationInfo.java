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

import javax.management.DynamicMBean;
import javax.management.ObjectName;


/**
 * Helper class to associate a MBean with an ObjectName.
 * @author J&ouml;rg Schaible
 */
public class JMXRegistrationInfo {

    private final ObjectName objectName;
    private final DynamicMBean mBean;

    /**
     * Construct a JMXRegistrationInfo.
     * @param objectName The {@link ObjectName} used for the registration in the {@link javax.management.MBeanServer}.
     * @param mBean The {@link DynamicMBean} to register.
     */
    public JMXRegistrationInfo(final ObjectName objectName, final DynamicMBean mBean) {
        this.objectName = objectName;
        this.mBean = mBean;
    }

    /**
     * @return Returns the MBean.
     */
    public DynamicMBean getMBean() {
        return mBean;
    }

    /**
     * @return Returns the proposed {@link ObjectName}.
     */
    public ObjectName getObjectName() {
        return objectName;
    }
}
