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
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


/**
 * Core interface for generating ObjectName instances for a DynamicMBean.
 * @author J&ouml;rg Schaible
 */
public interface ObjectNameFactory {

    /**
     * Create an ObjectName.
     * @param key The key of the component within PicoContainer.
     * @param mBean The instance of the DynamicMBean.
     * @return Returns the Object Name for the DynamicMBean.
     * @throws MalformedObjectNameException Thrown for an invalid part in the {@link ObjectName}.
     */
    ObjectName create(Object key, DynamicMBean mBean) throws MalformedObjectNameException;
}
