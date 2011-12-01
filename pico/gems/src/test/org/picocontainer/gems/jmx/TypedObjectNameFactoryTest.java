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

import static org.junit.Assert.assertEquals;

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.junit.Test;
import org.picocontainer.gems.jmx.testmodel.DynamicMBeanPerson;


/**
 * @author J&ouml;rg Schaible
 */
public class TypedObjectNameFactoryTest {

    @Test public void testSpecifiedDomain() throws MalformedObjectNameException, NotCompliantMBeanException {
        final ObjectNameFactory factory = new TypedObjectNameFactory("JUnit");
        final ObjectName objectName = factory.create(null, new DynamicMBeanPerson());
        assertEquals("JUnit:type=DynamicMBeanPerson", objectName.getCanonicalName());
    }

    @Test public void testDefaultDomain() throws MalformedObjectNameException, NotCompliantMBeanException {
        final ObjectNameFactory factory = new TypedObjectNameFactory();
        final ObjectName objectName = factory.create(null, new DynamicMBeanPerson());
        assertEquals(":type=DynamicMBeanPerson", objectName.getCanonicalName());
    }
}
