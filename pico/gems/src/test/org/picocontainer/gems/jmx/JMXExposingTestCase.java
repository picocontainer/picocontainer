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
import static org.junit.Assert.fail;
import static org.picocontainer.gems.GemsCharacteristics.JMX;
import static org.picocontainer.gems.GemsCharacteristics.NO_JMX;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.Behavior;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.gems.jmx.testmodel.DynamicMBeanPerson;
import org.picocontainer.gems.jmx.testmodel.PersonMBean;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;


/**
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class JMXExposingTestCase  {

	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    private MBeanServer mBeanServer = mockery.mock(MBeanServer.class);

    @Test public void testWillRegisterByDefaultComponentsThatAreMBeans() throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        final JMXExposing componentFactory = new JMXExposing(
                mBeanServer);
        componentFactory.wrap(new ConstructorInjection());
        mockery.checking(new Expectations() {{
        	one(mBeanServer).registerMBean(with(any(DynamicMBeanPerson.class)), with(any(ObjectName.class)));
        }});

        final ComponentAdapter<?> componentAdapter = componentFactory.createComponentAdapter(
                new NullComponentMonitor(), new NullLifecycleStrategy(), Characteristics.CDI, PersonMBean.class, DynamicMBeanPerson.class, null, null, null);
        assertNotNull(componentAdapter);
        assertNotNull(componentAdapter.getComponentInstance(null,null));
    }

    @Test public void testWillRegisterByDefaultComponentsThatAreMBeansUnlessNOJMX() throws NotCompliantMBeanException {
        final JMXExposing componentFactory = new JMXExposing(
                mBeanServer);
        componentFactory.wrap(new ConstructorInjection());

        final Properties rc = new Properties(NO_JMX);

        final ComponentAdapter<?> componentAdapter = componentFactory.createComponentAdapter(
                new NullComponentMonitor(), new NullLifecycleStrategy(), rc, PersonMBean.class, DynamicMBeanPerson.class, null, null, null);
        assertNotNull(componentAdapter);
        assertNotNull(componentAdapter.getComponentInstance(null,null));
    }
    
    @Test
    public void testPicoContainerIntegration() throws Exception {
        final Behavior componentFactory = new JMXExposing(mBeanServer);
        
        MutablePicoContainer pico = new PicoBuilder()
        				.withBehaviors(componentFactory)
        				.withConstructorInjection()
        				.withLifecycle().build();
        

        pico.change(NO_JMX)
        	.addComponent(DynamicMBeanPerson.class) //No Register
        	.change(JMX)
        	.addComponent(PersonMBean.class, DynamicMBeanPerson.class) //Register
        	.as(NO_JMX)
        	.addComponent("Test Person", DynamicMBeanPerson.class);  //No Register
        
        mockery.checking(new Expectations() {{
        	one(mBeanServer).registerMBean(with(any(DynamicMBeanPerson.class)), with(any(ObjectName.class)));
        }});
    	
        //Get instances to force registration.
        pico.getComponent(DynamicMBeanPerson.class);
        pico.getComponent(PersonMBean.class);
        pico.getComponent("Test Person");
        
    }

    @Test public void testConstructorThrowsNPE() {
        try {
            new JMXExposing(
                    null, new DynamicMBeanProvider[]{});
            fail("NullPointerException expected");
        } catch (final NullPointerException e) {
        }
        try {
            new JMXExposing(
                    mBeanServer, null);
            fail("NullPointerException expected");
        } catch (final NullPointerException e) {
        }
    }
}
