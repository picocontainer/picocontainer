/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/

package org.picocontainer.gems.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.gems.jmx.testmodel.OtherPerson;
import org.picocontainer.gems.jmx.testmodel.Person;
import org.picocontainer.gems.jmx.testmodel.PersonMBean;


/**
 * @author J&ouml;rg Schaible
 */
@RunWith(JMock.class)
public class NamingConventionConstructingProviderTest {

	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    private ObjectNameFactory nameFactory = mockery.mock(ObjectNameFactory.class);

    @Test public void testObjectNameFactoryMustNotBeNull() {
        try {
            new NamingConventionConstructingProvider(null);
            fail("NullPointerException expected");
        } catch (final NullPointerException e) {
        }
    }

    @Test public void testGivenObjectNameFactoryIsProvided() {
        final NamingConventionConstructingProvider provider = new NamingConventionConstructingProvider(nameFactory);
        assertSame(nameFactory, provider.getObjectNameFactory());
    }

    @Test public void testReusesMBeanFactory() {
        final NamingConventionConstructingProvider provider = new NamingConventionConstructingProvider(nameFactory);
        final DynamicMBeanFactory beanFactory = provider.getMBeanFactory();
        assertNotNull(beanFactory);
        assertSame(beanFactory, provider.getMBeanFactory());
    }

    @Test public void testUsesNamingConventionMBeanInfoProvidersInRightSequence() {
        final NamingConventionConstructingProvider provider = new NamingConventionConstructingProvider(nameFactory);
        final MBeanInfoProvider[] infoProviders = provider.getMBeanInfoProviders();
        assertNotNull(infoProviders);
        assertEquals(2, infoProviders.length);
        assertTrue(infoProviders[0] instanceof ComponentKeyConventionMBeanInfoProvider);
        assertTrue(infoProviders[1] instanceof ComponentTypeConventionMBeanInfoProvider);
        assertSame(infoProviders, provider.getMBeanInfoProviders());
    }

    @Test public void testFindsManagementInterfaceAccordingNamingConventions() throws ClassNotFoundException {
        final NamingConventionConstructingProvider provider = new NamingConventionConstructingProvider(nameFactory);
        assertSame(PersonMBean.class, provider.getManagementInterface(Person.class, null));
    }

    @Test public void testThrowsClassNotFoundExceptionIfNoManagementInterfaceCanBeFound() {
        final NamingConventionConstructingProvider provider = new NamingConventionConstructingProvider(nameFactory);
        try {
            provider.getManagementInterface(OtherPerson.class, null);
            fail("ClassNotFoundException expected");
        } catch (final ClassNotFoundException e) {
        }
    }
}
