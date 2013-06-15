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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

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
public class AbstractConstructingProviderTest {

	private final Mockery mockery = mockeryWithCountingNamingScheme();

    private final ObjectNameFactory objectNameFactory = mockery.mock(ObjectNameFactory.class);
    private final DynamicMBeanFactory dynamicMBeanFactory = mockery.mock(DynamicMBeanFactory.class);
    private MBeanInfoProvider[] mBeanInfoProviders;
    private ObjectName objectName;
    private MutablePicoContainer pico;

    private class ConstructingProvider extends AbstractConstructingProvider {

        @Override
		protected ObjectNameFactory getObjectNameFactory() {
            return objectNameFactory;
        }

        @Override
		protected MBeanInfoProvider[] getMBeanInfoProviders() {
            return mBeanInfoProviders;
        }

        @Override
		protected DynamicMBeanFactory getMBeanFactory() {
            return dynamicMBeanFactory;
        }

        @Override
		protected Class getManagementInterface(final Class implementation, final MBeanInfo mBeanInfo)
                throws ClassNotFoundException {
            if (implementation.equals(Person.class)) {
                return PersonMBean.class;
            }
            throw new ClassNotFoundException();
        }

    }

    @Before
    public void setUp() throws Exception {
        objectName = new ObjectName(":type=JUnit");
        pico = new DefaultPicoContainer();
        mBeanInfoProviders = new MBeanInfoProvider[0];
    }

    @Test public void testCanCreateMBean() throws MalformedObjectNameException {
        final Person person = new Person();
        final ComponentAdapter componentAdapter = pico.addComponent(person).getComponentAdapter(person.getClass(), (NameBinding) null);
        final DynamicMBean dynamicMBean = mockery.mock(DynamicMBean.class);
        final MBeanInfoProvider mBeanInfoProvider = mockery.mock(MBeanInfoProvider.class);
        mBeanInfoProviders = new MBeanInfoProvider[]{mBeanInfoProvider};
        mockery.checking(new Expectations() {{
        	one(mBeanInfoProvider).provide(with(same(pico)), with(same(componentAdapter)));
        	will(returnValue(Person.createMBeanInfo()));
            one(dynamicMBeanFactory).create(with(same(person)), with(same(PersonMBean.class)), with(equal(Person.createMBeanInfo())));
            will(returnValue(dynamicMBean));
            one(objectNameFactory).create(with(same(Person.class)), with(any(DynamicMBean.class)));
            will(returnValue(objectName));
        }});
        final DynamicMBeanProvider provider = new ConstructingProvider();
        final JMXRegistrationInfo info = provider.provide(pico, componentAdapter);
        assertNotNull(info);

    }

    @Test public void testNoInstanceIsCreatedIfManagementInterfaceIsMissing() {
        final ComponentAdapter componentAdapter = pico.addComponent(OtherPerson.class).getComponentAdapter(OtherPerson.class,
                                                                                                           (NameBinding) null);
        final DynamicMBeanProvider provider = new ConstructingProvider();
        assertNull(provider.provide(pico, componentAdapter));
    }

    @Test public void testObjectNameMustBeGiven() throws MalformedObjectNameException {
    	 mockery.checking(new Expectations() {{
             one(dynamicMBeanFactory).create(with(any(Person.class)), with(same(PersonMBean.class)), with(aNull(MBeanInfo.class)));
             will(returnValue(mockery.mock(DynamicMBean.class)));
             one(objectNameFactory).create(with(same(Person.class)), with(any(DynamicMBean.class)));
             will(returnValue(null));
         }});
        final ComponentAdapter componentAdapter = pico.addComponent(Person.class).getComponentAdapter(Person.class, (NameBinding) null);
        final DynamicMBeanProvider provider = new ConstructingProvider();
        assertNull(provider.provide(pico, componentAdapter));
    }

    @Test public void testMalformedObjectNameThrowsJMXRegistrationException() throws MalformedObjectNameException {
    	 mockery.checking(new Expectations() {{
             one(dynamicMBeanFactory).create(with(any(Person.class)), with(same(PersonMBean.class)), with(aNull(MBeanInfo.class)));
             will(returnValue(mockery.mock(DynamicMBean.class)));
             one(objectNameFactory).create(with(same(Person.class)), with(any(DynamicMBean.class)));
             will(throwException(new MalformedObjectNameException("JUnit")));
         }});
        final ComponentAdapter componentAdapter = pico.addComponent(Person.class).getComponentAdapter(Person.class, (NameBinding) null);
        final DynamicMBeanProvider provider = new ConstructingProvider();
        try {
            provider.provide(pico, componentAdapter);
            fail("JMXRegistrationException expected");
        } catch (final JMXRegistrationException e) {
            assertEquals("JUnit", e.getCause().getMessage());
        }
    }
}
