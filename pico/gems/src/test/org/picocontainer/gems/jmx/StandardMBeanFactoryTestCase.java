/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Michael Ward                                             *
 *****************************************************************************/

package org.picocontainer.gems.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;

import org.junit.Test;
import org.picocontainer.gems.jmx.testmodel.Person;
import org.picocontainer.gems.jmx.testmodel.PersonMBean;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;



/**
 * @author Michael Ward
 * @author J&ouml;rg Schaible
 */
public class StandardMBeanFactoryTestCase {

    @Test public void testMBeanCreationWithMBeanInfo() {
        final DynamicMBeanFactory factory = new StandardMBeanFactory();
        final MBeanInfo mBeanInfo = Person.createMBeanInfo();
        final DynamicMBean mBean = factory.create(new Person(), null, mBeanInfo);
        assertNotNull(mBean);
        assertEquals(mBeanInfo, mBean.getMBeanInfo());
    }

    @Test public void testMBeanCreationWithoutMBeanInfo() {
        final DynamicMBeanFactory factory = new StandardMBeanFactory();
        final DynamicMBean mBean = factory.create(new Person(), null, null);
        assertNotNull(mBean);
        assertNotNull(mBean.getMBeanInfo());
    }

    @Test public void testMBeanCreationWithMBeanInfoAndArbitraryInterfaceName() {
        final DynamicMBeanFactory factory = new StandardMBeanFactory();
        final MBeanInfo mBeanInfo = Person.createMBeanInfo();
        final DynamicMBean mBean = factory.create(new SimpleTouchable(), Touchable.class, mBeanInfo);
        assertNotNull(mBean);
    }

    @Test public void testMBeanCreationFailsWithoutManagementInterface() {
        final DynamicMBeanFactory factory = new StandardMBeanFactory();
        final MBeanInfo mBeanInfo = Person.createMBeanInfo();
        try {
            factory.create(new SimpleTouchable(), null, mBeanInfo);
            fail("JMXRegistrationException expected");
        } catch (final JMXRegistrationException e) {
            // fine
        }
    }

    @Test public void testMBeanCreationWithoutManagementInterfaceWorksForModelMBeanInfo() {
        final DynamicMBeanFactory factory = new StandardMBeanFactory();
        final ModelMBeanAttributeInfo[] attributes = new ModelMBeanAttributeInfo[]{new ModelMBeanAttributeInfo(
                "Name", String.class.getName(), "desc", true, false, false)};
        final MBeanInfo mBeanInfo = new ModelMBeanInfoSupport(
                Person.class.getName(), "Description of Person", attributes, null, null, null);

        final DynamicMBean mBean = factory.create(new SimpleTouchable(), null, mBeanInfo);
        assertNotNull(mBean);
    }

    @Test public void testGetDefaultManagementInterfaceFromMBeanType() throws ClassNotFoundException {
        final StandardMBeanFactory factory = new StandardMBeanFactory();
        assertSame(PersonMBean.class, factory.getDefaultManagementInterface(Person.class, null));
    }

    @Test public void testGetDefaultManagementInterfaceFromMBeanInfo() throws ClassNotFoundException {
        final StandardMBeanFactory factory = new StandardMBeanFactory();
        final MBeanInfo mBeanInfo = Person.createMBeanInfo();
        assertSame(PersonMBean.class, factory.getDefaultManagementInterface(SimpleTouchable.class, mBeanInfo));
    }
}
