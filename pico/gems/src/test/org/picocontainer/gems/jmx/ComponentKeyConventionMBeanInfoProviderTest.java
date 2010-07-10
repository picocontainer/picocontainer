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
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import javax.management.MBeanInfo;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.gems.jmx.testmodel.OtherPerson;
import org.picocontainer.gems.jmx.testmodel.Person;
import org.picocontainer.gems.jmx.testmodel.PersonMBean;

/**
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class ComponentKeyConventionMBeanInfoProviderTest {

	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    private MutablePicoContainer pico;
    private MBeanInfoProvider mBeanProvider;

    @Before
    public void setUp() throws Exception {
        pico = new DefaultPicoContainer();
        mBeanProvider = new ComponentKeyConventionMBeanInfoProvider();
    }

    @Test public void testMBeanInfoIsDeterminedIfKeyIsType() {
        final PersonMBean person = new OtherPerson();

        final ComponentAdapter componentAdapter = mockery.mock(ComponentAdapter.class);
        mockery.checking(new Expectations() {{
        	atLeast(1).of(componentAdapter).getComponentKey();
        	will(returnValue(Person.class));
        	atLeast(1).of(componentAdapter).getComponentImplementation();
        	will(returnValue(person.getClass()));
        }});

        pico.addAdapter(componentAdapter);
        pico.addComponent(Person.class.getName() + "MBeanInfo", Person.createMBeanInfo());

        final MBeanInfo info = mBeanProvider.provide(pico, componentAdapter);
        assertNotNull(info);
        assertEquals(Person.createMBeanInfo().getDescription(), info.getDescription());
    }

    @Test public void testMBeanInfoIsDeterminedIfKeyIsManagementInterface() {
        final ComponentAdapter componentAdapter = pico.addComponent(PersonMBean.class, Person.class).getComponentAdapter(PersonMBean.class,
                                                                                                                         (NameBinding) null);
        pico.addComponent(PersonMBean.class.getName() + "Info", Person.createMBeanInfo());

        final MBeanInfo info = mBeanProvider.provide(pico, componentAdapter);
        assertNotNull(info);
        assertEquals(Person.createMBeanInfo().getDescription(), info.getDescription());
    }

    @Test public void testMBeanInfoIsDeterminedIfKeyIsString() {
        final ComponentAdapter componentAdapter = pico.addComponent("JUnit", Person.class).getComponentAdapter("JUnit");
        pico.addComponent("JUnitMBeanInfo", Person.createMBeanInfo());

        final MBeanInfo info = mBeanProvider.provide(pico, componentAdapter);
        assertNotNull(info);
        assertEquals(Person.createMBeanInfo().getDescription(), info.getDescription());
    }

}
