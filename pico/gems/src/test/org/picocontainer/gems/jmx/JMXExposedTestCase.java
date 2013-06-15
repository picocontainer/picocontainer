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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.gems.jmx.testmodel.DynamicMBeanPerson;
import org.picocontainer.gems.jmx.testmodel.Person;
import org.picocontainer.gems.jmx.testmodel.PersonMBean;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;


/**
 * @author J&ouml;rg Schaible
 */
public class JMXExposedTestCase {

	private final Mockery mockery = mockeryWithCountingNamingScheme();

    private final MBeanServer mBeanServer = mockery. mock(MBeanServer.class);

    @Test public void testWillRegisterByDefaultComponentsThatAreMBeans() throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        final DynamicMBeanPerson person = new DynamicMBeanPerson();
        final ComponentAdapter componentAdapter = new JMXExposing.JMXExposed(new InstanceAdapter(
                PersonMBean.class, person, new NullLifecycleStrategy(), new NullComponentMonitor()), mBeanServer);
        mockery.checking(new Expectations() {{
        	one(mBeanServer).registerMBean(with(same(person)), with(any(ObjectName.class)));
        }});

        assertSame(person, componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class));
    }

    @Test public void testWillRegisterAndUnRegisterByDefaultComponentsThatAreMBeans() throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InstanceNotFoundException {
        final DynamicMBeanPerson person = new DynamicMBeanPerson();
        final JMXExposing.JMXExposed componentAdapter = new JMXExposing.JMXExposed(new InstanceAdapter(
                PersonMBean.class, person, new NullLifecycleStrategy(), new NullComponentMonitor()), mBeanServer);
        mockery.checking(new Expectations() {{
        	one(mBeanServer).registerMBean(with(same(person)), with(any(ObjectName.class)));
        	one(mBeanServer).unregisterMBean(with(any(ObjectName.class)));
        }});

        assertSame(person, componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class));
        componentAdapter.dispose(person);
    }

    @Test public void testWillTryAnyDynamicMBeanProvider() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        final Person person = new Person();
        final DynamicMBeanProvider provider1 = mockery.mock(DynamicMBeanProvider.class);
        final DynamicMBeanProvider provider2 = mockery.mock(DynamicMBeanProvider.class);
        final ObjectName objectName = new ObjectName(":type=Person");
        final DynamicMBean mBean = new DynamicMBeanPerson();
        final JMXRegistrationInfo info = new JMXRegistrationInfo(objectName, mBean);

        final ComponentAdapter componentAdapter = new JMXExposing.JMXExposed(new InstanceAdapter(
                PersonMBean.class, person, new NullLifecycleStrategy(), new NullComponentMonitor()), mBeanServer, new DynamicMBeanProvider[]{
                provider1, provider2});
        mockery.checking(new Expectations() {{
        	one(provider1).provide(with(aNull(PicoContainer.class)), with(any(ComponentAdapter.class)));
        	will(returnValue(null));
        	one(provider2).provide(with(aNull(PicoContainer.class)), with(any(ComponentAdapter.class)));
        	will(returnValue(info));
        	one(mBeanServer).registerMBean(with(same(mBean)), with(equal(objectName)));
        }});

        assertSame(person, componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class));
    }

    @Test public void testThrowsPicoInitializationExceptionIfMBeanIsAlreadyRegistered() throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        final PersonMBean person = new DynamicMBeanPerson();
        final Exception exception = new InstanceAlreadyExistsException("JUnit");
        final ComponentAdapter componentAdapter = new JMXExposing.JMXExposed(new InstanceAdapter(
                PersonMBean.class, person, new NullLifecycleStrategy(), new NullComponentMonitor()), mBeanServer);
        mockery.checking(new Expectations() {{
        	one(mBeanServer).registerMBean(with(same(person)), with(any(ObjectName.class)));
        	will(throwException(exception));
        }});

        try {
            assertSame(person, componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class));
            fail("PicoCompositionException expected");
        } catch (final PicoCompositionException e) {
            assertSame(exception, e.getCause());
        }
    }

    @Test public void testThrowsPicoInitializationExceptionIfMBeanCannotBeRegistered() throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        final PersonMBean person = new DynamicMBeanPerson();
        final Exception exception = new MBeanRegistrationException(new Exception(), "JUnit");
        final ComponentAdapter componentAdapter = new JMXExposing.JMXExposed(new InstanceAdapter(
                PersonMBean.class, person, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor()), mBeanServer);
        mockery.checking(new Expectations() {{
        	one(mBeanServer).registerMBean(with(same(person)), with(any(ObjectName.class)));
        	will(throwException(exception));
        }});

        try {
            assertSame(person, componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class));
            fail("PicoCompositionException expected");
        } catch (final PicoCompositionException e) {
            assertSame(exception, e.getCause());
        }
    }

    @Test public void testThrowsPicoInitializationExceptionIfMBeanNotCompliant() throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        final PersonMBean person = new DynamicMBeanPerson();
        final Exception exception = new NotCompliantMBeanException("JUnit");
        final ComponentAdapter componentAdapter = new JMXExposing.JMXExposed(new InstanceAdapter(
                PersonMBean.class, person, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor()), mBeanServer);

        mockery.checking(new Expectations() {{
        	one(mBeanServer).registerMBean(with(same(person)), with(any(ObjectName.class)));
        	will(throwException(exception));
        }});

        try {
            assertSame(person, componentAdapter.getComponentInstance(null, ComponentAdapter.NOTHING.class));
            fail("PicoCompositionException expected");
        } catch (final PicoCompositionException e) {
            assertSame(exception, e.getCause());
        }
    }

    @Test public void testConstructorThrowsNPE() {
        try {
			new JMXExposing.JMXExposed(new InstanceAdapter(JMXExposedTestCase.class, this,
					new NullLifecycleStrategy(), new NullComponentMonitor()),
					null, new DynamicMBeanProvider[] {});
			fail("NullPointerException expected");
		} catch (final NullPointerException e) {
		}
		try {
			new JMXExposing.JMXExposed(new InstanceAdapter(JMXExposedTestCase.class, this,
					new NullLifecycleStrategy(), new NullComponentMonitor()),
					mBeanServer, null);
			fail("NullPointerException expected");
		} catch (final NullPointerException e) {
		}
    }
}
