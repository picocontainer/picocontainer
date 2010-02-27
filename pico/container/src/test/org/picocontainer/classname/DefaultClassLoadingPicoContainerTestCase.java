/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/

package org.picocontainer.classname;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.monitors.ConsoleComponentMonitor;
import org.picocontainer.tck.AbstractPicoContainerTest;

/**
 * @author Paul Hammant
 */
@RunWith(JMock.class)
public class DefaultClassLoadingPicoContainerTestCase extends AbstractPicoContainerTest {

	private Mockery context = new JUnit4Mockery();

	
    protected MutablePicoContainer createPicoContainer(PicoContainer parent) {
        return new DefaultClassLoadingPicoContainer(this.getClass().getClassLoader(), new DefaultPicoContainer(new Caching(), parent));
    }

    protected Properties[] getProperties() {
        return new Properties[] { Characteristics.NONE};
    }

    @Test public void testNamedChildContainerIsAccessible()  {
        StringBuffer sb = new StringBuffer();
        final ClassLoadingPicoContainer parent = (ClassLoadingPicoContainer) createPicoContainer(null);
        parent.addComponent(sb);
        final ClassLoadingPicoContainer child = (ClassLoadingPicoContainer) parent.makeChildContainer("foo");
        child.addComponent(LifeCycleMonitoring.class,LifeCycleMonitoring.class);
        LifeCycleMonitoring o = (LifeCycleMonitoring) parent.getComponent((Object)("foo/*" + LifeCycleMonitoring.class.getName()));
        assertNotNull(o);
    }

    @Test public void testNamedChildContainerIsAccessibleForStringKeys() {
        StringBuffer sb = new StringBuffer();
        final ClassLoadingPicoContainer parent = (ClassLoadingPicoContainer) createPicoContainer(null);
        parent.addComponent(sb);
        final MutablePicoContainer child = parent.makeChildContainer("foo");
        child.addComponent("lcm",LifeCycleMonitoring.class);
        Object o = parent.getComponent("foo/lcm");
        assertNotNull(o);
        assertTrue(sb.toString().indexOf("-instantiated") != -1);
    }

    @Test public void testNamedChildContainerIsAccessibleForClassKeys() {
        StringBuffer sb = new StringBuffer();
        final ClassLoadingPicoContainer parent = (ClassLoadingPicoContainer) createPicoContainer(null);
        parent.addComponent(sb);
        final MutablePicoContainer child = parent.makeChildContainer("foo");
        child.addComponent(LifeCycleMonitoring.class,LifeCycleMonitoring.class);
        Object o = parent.getComponent("foo/*" + LifeCycleMonitoring.class.getName());
        assertNotNull(o);
        assertTrue(sb.toString().indexOf("-instantiated") != -1);
    }

    @Test public void testMakeRemoveChildContainer() {
        final ClassLoadingPicoContainer parent = (ClassLoadingPicoContainer) createPicoContainer(null);
        parent.addComponent("java.lang.String", "This is a test");
        MutablePicoContainer pico = parent.makeChildContainer();
        // Verify they are indeed wired together.
        assertNotNull(pico.getComponent("java.lang.String"));
        boolean result = parent.removeChildContainer(pico);
        assertTrue(result);
    }

    // test methods inherited. This container is otherwise fully compliant.
    @Test public void testAcceptImplementsBreadthFirstStrategy() {
        super.testAcceptImplementsBreadthFirstStrategy();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testSwapComponentMonitorWithNoComponentMonitorStrategyDelegateThrowsIllegalStateException() {
    	MutablePicoContainer delegate = context.mock(MutablePicoContainer.class);
    	//Delegate it twice for effect.
    	DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer(new DefaultClassLoadingPicoContainer(delegate));
    	pico.changeMonitor(new ConsoleComponentMonitor());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testCurrentMonitorWithNoComponentMonitorStrategyDelegateThrowsIllegalStateException() {
    	Mockery context = new JUnit4Mockery();
    	MutablePicoContainer delegate = context.mock(MutablePicoContainer.class);
    	//Delegate it twice for effect.
    	DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer(new DefaultClassLoadingPicoContainer(delegate));
    	pico.currentMonitor();    	
    }

    protected void addContainers(List expectedList) {
        expectedList.add(DefaultClassLoadingPicoContainer.class);
        expectedList.add(DefaultPicoContainer.class);
    }


    protected void addDefaultComponentFactories(List expectedList) {
        expectedList.add(Caching.class);
    }


}
