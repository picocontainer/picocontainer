/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/

package org.picocontainer.gems.jmx.testmodel;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;

import mx4j.MBeanDescriptionAdapter;


/**
 * MBean description used automatically for StandardMBeans by MX4J. Note: This will only work if MX4J provides
 * javax.management.StandardMBean. With J2SE 5 you will always use the classes from the JDK and therefore the mechanism
 * fails. The component will still be exposed as bean, but no description for the exposed parts will be available.
 * @author J&ouml;rg Schaible
 */
public final class PersonMBeanDescription extends MBeanDescriptionAdapter {

    private static final MBeanInfo MBEAN_INFO = Person.createMBeanInfo();

    @Override
	public String getAttributeDescription(final String attribute) {
        MBeanAttributeInfo[] attributes = MBEAN_INFO.getAttributes();
        for (final MBeanAttributeInfo info : attributes) {
            if (info.getName().equals(attribute)) {
                return info.getDescription();
            }
        }
        return super.getAttributeDescription(attribute);
    }

    @Override
	public String getMBeanDescription() {
        return MBEAN_INFO.getDescription();
    }
}
