/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Michael Ward                                    		 *
 *****************************************************************************/

package org.picocontainer.gems.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.util.Set;

import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
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
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.gems.jmx.testmodel.Person;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.ConstantParameter;


/**
 * @author Michael Ward
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class JMXVisitorTestCase  {
	
	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    private MutablePicoContainer picoContainer;
    private MBeanServer mBeanServer =  mockery.mock(MBeanServer.class);
    private DynamicMBeanProvider dynamicMBeanProvider = mockery.mock(DynamicMBeanProvider.class);
    private DynamicMBean dynamicMBean = mockery.mock(DynamicMBean.class);

    @Before
    public void setUp() throws Exception {
        picoContainer = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy());
    }

    private JMXVisitor createVisitor(final int providerCount) {
        final DynamicMBeanProvider[] providers = new DynamicMBeanProvider[providerCount];
        for (int i = 0; i < providers.length; i++) {
            providers[i] = dynamicMBeanProvider;
        }
        return new JMXVisitor(mBeanServer, providers);
    }

    /**
     * Test visit with registration.
     * @throws MalformedObjectNameException
     * @throws NotCompliantMBeanException 
     * @throws MBeanRegistrationException 
     * @throws InstanceAlreadyExistsException 
     */
    @Test public void testVisitWithRegistration() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        final JMXVisitor jmxVisitor = createVisitor(1);
        final ObjectName objectName = new ObjectName(":type=JUnit");
        final JMXRegistrationInfo registrationInfo = new JMXRegistrationInfo(objectName, dynamicMBean);
        final ObjectInstance objectInstance = new ObjectInstance(objectName, Person.class.getName());

        // parameter fixes coverage of visitParameter !!
        final ComponentAdapter componentAdapter = picoContainer.addComponent(
                Person.class, Person.class, new ConstantParameter("John Doe")).getComponentAdapter(Person.class, (NameBinding) null);

        mockery.checking(new Expectations(){{
        	one(dynamicMBeanProvider).provide(with(same(picoContainer)), with(same(componentAdapter)));
        	will(returnValue(registrationInfo));
        	one(mBeanServer).registerMBean(with(same(registrationInfo.getMBean())), with(same(registrationInfo.getObjectName())));
        	will(returnValue(objectInstance));
        }});

        final Set set = (Set)jmxVisitor.traverse(picoContainer);
        assertEquals(1, set.size());
        assertSame(objectInstance, set.iterator().next());
    }

    /**
     * Test the trial of multiple providers and ensure, that the first provider delivering a JMXRegistrationInfo is
     * used.
     * @throws MalformedObjectNameException
     * @throws NotCompliantMBeanException 
     * @throws MBeanRegistrationException 
     * @throws InstanceAlreadyExistsException 
     */
    @Test public void testVisitWithMultipleProviders() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        final JMXVisitor jmxVisitor = createVisitor(2);
        final JMXRegistrationInfo registrationInfo = new JMXRegistrationInfo(
                new ObjectName(":type=JUnit"), dynamicMBean);

        final ComponentAdapter componentAdapter1 = picoContainer.addComponent(this).getComponentAdapter(this.getClass(),
                                                                                                        (NameBinding) null);
        final ComponentAdapter componentAdapter2 = picoContainer.addComponent(Person.class).getComponentAdapter(Person.class,
                                                                                                        (NameBinding) null);

        mockery.checking(new Expectations(){{
        	one(dynamicMBeanProvider).provide(with(same(picoContainer)), with(same(componentAdapter1)));
        	will(returnValue(null));
        	one(dynamicMBeanProvider).provide(with(same(picoContainer)), with(same(componentAdapter1)));
        	will(returnValue(null));
        	one(dynamicMBeanProvider).provide(with(same(picoContainer)), with(same(componentAdapter2)));
        	will(returnValue(registrationInfo));
        	one(mBeanServer).registerMBean(with(same(registrationInfo.getMBean())), with(same(registrationInfo.getObjectName())));
        }});
        jmxVisitor.traverse(picoContainer);
    }

    /**
     * Test the traversal of the visitor.
     */
    @Test public void testTraversal() {
        final JMXVisitor jmxVisitor = createVisitor(1);
        final MutablePicoContainer child = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy());
        picoContainer.addChildContainer(child);

        final ComponentAdapter componentAdapter = child.addComponent(Person.class).getComponentAdapter(Person.class,
                                                                                                       (NameBinding) null);

        mockery.checking(new Expectations(){{
        	one(dynamicMBeanProvider).provide(with(same(child)), with(same(componentAdapter)));
        	will(returnValue(null));
        }});
        
        jmxVisitor.traverse(picoContainer);
    }

    /**
     * Test ctor.
     */
    @Test public void testInvalidConstructorArguments() {
        try {
            new JMXVisitor(null, new DynamicMBeanProvider[]{dynamicMBeanProvider});
            fail("NullPointerException expected");
        } catch (final NullPointerException e) {
            // fine
        }
        try {
            new JMXVisitor(mBeanServer, null);
            fail("NullPointerException expected");
        } catch (final NullPointerException e) {
            // fine
        }
        try {
            new JMXVisitor(mBeanServer, new DynamicMBeanProvider[]{});
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {
            // fine
        }
    }

    /**
     * Test illegal call of visitComponentAdapter
     */
    @Test public void testIllegalVisit() {
        final JMXVisitor jmxVisitor = createVisitor(1);
        final ComponentAdapter componentAdapter = new InstanceAdapter(this, this, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor());
        try {
            jmxVisitor.traverse(componentAdapter);
            fail("JMXRegistrationException expected");
        } catch (final JMXRegistrationException e) {
            // fine
        }
    }

    /**
     * Test failing registration.
     * @throws MalformedObjectNameException
     * @throws NotCompliantMBeanException 
     * @throws MBeanRegistrationException 
     * @throws InstanceAlreadyExistsException 
     */
    @Test public void testFailingMBeanRegistration() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        final JMXVisitor jmxVisitor = createVisitor(1);
        final JMXRegistrationInfo registrationInfo = new JMXRegistrationInfo(
                new ObjectName(":type=JUnit"), dynamicMBean);
        final Exception exception = new MBeanRegistrationException(null, "JUnit");

        // parameter fixes coverage of visitParameter !!
        final ComponentAdapter componentAdapter = picoContainer.addComponent(Person.class).getComponentAdapter(Person.class,
                                                                                                               (NameBinding) null);

        mockery.checking(new Expectations(){{
        	one(dynamicMBeanProvider).provide(with(same(picoContainer)), with(same(componentAdapter)));
        	will(returnValue(registrationInfo));
        	one(mBeanServer).registerMBean(with(same(registrationInfo.getMBean())), with(same(registrationInfo.getObjectName())));
        	will(throwException(exception));
        }});

        try {
            jmxVisitor.traverse(picoContainer);
            fail("JMXRegistrationException expected");
        } catch (final JMXRegistrationException e) {
            assertSame(exception, e.getCause());
        }
    }
}
