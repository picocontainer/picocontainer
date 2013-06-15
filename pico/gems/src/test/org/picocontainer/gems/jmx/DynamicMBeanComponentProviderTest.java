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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.gems.jmx.testmodel.DynamicMBeanPerson;
import org.picocontainer.gems.jmx.testmodel.Person;
import org.picocontainer.gems.jmx.testmodel.PersonMBean;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;

/**
 * @author J&ouml;rg Schaible
 */
@RunWith(JMock.class)
public class DynamicMBeanComponentProviderTest {

	private final Mockery mockery = mockeryWithCountingNamingScheme();

	@Test public void testDynamicMBeansAreIdentified()
			throws NotCompliantMBeanException {
		final PersonMBean person = new DynamicMBeanPerson();
		final DynamicMBeanProvider provider = new DynamicMBeanComponentProvider();
		JMXRegistrationInfo info = provider.provide(null, new InstanceAdapter(
				PersonMBean.class, new Person(), new NullLifecycleStrategy(),
				new NullComponentMonitor()));
		assertNull(info);
		info = provider.provide(null, new InstanceAdapter("JUnit", person,
				new NullLifecycleStrategy(), new NullComponentMonitor()));
		assertNotNull(info);
		assertSame(person, info.getMBean());
	}

	@Test public void testDynamicMBeansAreOnlyProvidedWithObjectName()
			throws NotCompliantMBeanException, MalformedObjectNameException {
		final DynamicMBeanPerson person = new DynamicMBeanPerson();

		final ObjectNameFactory objectNameFactory = mockery
				.mock(ObjectNameFactory.class);
		mockery.checking(new Expectations() {
			{
				one(objectNameFactory).create(with(equal("JUnit")),
						with(same(person)));
				will(returnValue(null));
			}
		});

		final DynamicMBeanProvider provider = new DynamicMBeanComponentProvider(
				objectNameFactory);
		final JMXRegistrationInfo info = provider
				.provide(null,
						new InstanceAdapter("JUnit", person,
								new NullLifecycleStrategy(),
								new NullComponentMonitor()));
		assertNull(info);
	}

	@Test public void testDynamicMBeansWithMalformedObjectName()
			throws NotCompliantMBeanException, MalformedObjectNameException {
		final DynamicMBeanPerson person = new DynamicMBeanPerson();
		final Exception exception = new MalformedObjectNameException("JUnit");

		final ObjectNameFactory objectNameFactory = mockery
				.mock(ObjectNameFactory.class);
		mockery.checking(new Expectations() {
			{
				one(objectNameFactory).create(with(equal("JUnit")),
						with(same(person)));
				will(throwException(exception));
			}
		});

		final DynamicMBeanProvider provider = new DynamicMBeanComponentProvider(
				objectNameFactory);
		try {
			provider.provide(null, new InstanceAdapter("JUnit", person,
					new NullLifecycleStrategy(), new NullComponentMonitor()));
			fail("JMXRegistrationException expected");
		} catch (final JMXRegistrationException e) {
			assertSame(exception, e.getCause());
		}
	}
}
