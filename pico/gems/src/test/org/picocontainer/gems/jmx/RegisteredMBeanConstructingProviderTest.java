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
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import javax.management.MBeanInfo;
import javax.management.ObjectName;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.gems.jmx.testmodel.Person;
import org.picocontainer.gems.jmx.testmodel.PersonMBean;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;


/**
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class RegisteredMBeanConstructingProviderTest {

	private final Mockery mockery = mockeryWithCountingNamingScheme();

    private ObjectName objectName;
    private final DynamicMBeanFactory dynamicMBeanFactory = mockery.mock(DynamicMBeanFactory.class);

    @Before
    public void setUp() throws Exception {
        objectName = new ObjectName(":type=JUnit");
    }

    @Test public void testRegisterWithoutComponentKey() {
        final MBeanInfo mBeanInfo = Person.createMBeanInfo();
        final Person person = new Person();

        mockery.checking(new Expectations() {{
        	one(dynamicMBeanFactory).create(with(same(person)), with(same(Person.class)), with(same(mBeanInfo)));
        }});

        final RegisteredMBeanConstructingProvider provider = new RegisteredMBeanConstructingProvider(
                dynamicMBeanFactory);
        provider.register(objectName, mBeanInfo);
        assertNotNull(provider.provide(null, new InstanceAdapter(Person.class, person, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor())));
    }

    @Test public void testRegisterWithArbitraryComponentKey() {
        final MBeanInfo mBeanInfo = Person.createMBeanInfo();
        final Person person = new Person();

        mockery.checking(new Expectations() {{
        	one(dynamicMBeanFactory).create(with(same(person)), with(same(Person.class)), with(same(mBeanInfo)));
        }});

        final RegisteredMBeanConstructingProvider provider = new RegisteredMBeanConstructingProvider(
                dynamicMBeanFactory);
        provider.register("JUnit", objectName, mBeanInfo);
        assertNotNull(provider.provide(null, new InstanceAdapter("JUnit", person, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor())));
    }

    @Test public void testRegisterWithArbitraryComponentKeyAndManagementInterface() {
        final MBeanInfo mBeanInfo = Person.createMBeanInfo();
        final Touchable touchable = new SimpleTouchable();

        mockery.checking(new Expectations() {{
        	one(dynamicMBeanFactory).create(with(same(touchable)), with(same(Touchable.class)), with(same(mBeanInfo)));
        }});

        final RegisteredMBeanConstructingProvider provider = new RegisteredMBeanConstructingProvider(
                dynamicMBeanFactory);
        provider.register("JUnit", objectName, Touchable.class, mBeanInfo);
        assertNotNull(provider.provide(null, new InstanceAdapter("JUnit", touchable, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor())));
    }

    @Test public void testRegisterWithTypedComponentKeyButWithoutMBeanInfo() {
        final Person person = new Person();

        mockery.checking(new Expectations() {{
        	one(dynamicMBeanFactory).create(with(same(person)), with(same(PersonMBean.class)), with(aNull(MBeanInfo.class)));
        }});

        final RegisteredMBeanConstructingProvider provider = new RegisteredMBeanConstructingProvider(
                dynamicMBeanFactory);
        provider.register(PersonMBean.class, objectName);
        assertNotNull(provider.provide(null, new InstanceAdapter(PersonMBean.class, person, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor())));
    }

    @Test public void testRegisterWithArbitraryComponentKeyButWithoutMBeanInfo() {
        final Person person = new Person();

        mockery.checking(new Expectations() {{
        	one(dynamicMBeanFactory).create(with(same(person)), with(same(Person.class)), with(aNull(MBeanInfo.class)));
        }});

        final RegisteredMBeanConstructingProvider provider = new RegisteredMBeanConstructingProvider(
                dynamicMBeanFactory);
        provider.register("JUnit", objectName);
        assertNotNull(provider.provide(null, new InstanceAdapter("JUnit", person, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor())));
    }

    @Test public void testUsageOfStandardMBeanFactory() {
        final RegisteredMBeanConstructingProvider provider = new RegisteredMBeanConstructingProvider();
        provider.register(PersonMBean.class, objectName);
        final JMXRegistrationInfo info = provider.provide(null, new InstanceAdapter(
                PersonMBean.class, new Person(), new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor()));
        assertNotNull(info.getMBean());
        assertEquals(objectName, info.getObjectName());
    }
}
