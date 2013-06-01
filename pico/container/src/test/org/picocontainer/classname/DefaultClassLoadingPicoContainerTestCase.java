/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/

package org.picocontainer.classname;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoClassNotFoundException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer.CannotListClassesInAJarException;
import org.picocontainer.monitors.ConsoleComponentMonitor;
import org.picocontainer.tck.AbstractPicoContainerTest;

import java.util.List;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Paul Hammant
 */
@RunWith(JMock.class)
public class DefaultClassLoadingPicoContainerTestCase extends AbstractPicoContainerTest {

	private Mockery context = new JUnit4Mockery();

	
    protected MutablePicoContainer createPicoContainer(PicoContainer parent) {
        return new DefaultClassLoadingPicoContainer(this.getClass().getClassLoader(), new DefaultPicoContainer(parent));
    }

    protected Properties[] getProperties() {
        return new Properties[0];
    }

    @Test public void testNamedChildContainerIsAccessible()  {
        StringBuffer sb = new StringBuffer();
        final ClassLoadingPicoContainer parent = (ClassLoadingPicoContainer) createPicoContainer(null);
        parent.addComponent(sb);
        final ClassLoadingPicoContainer child = parent.makeChildContainer("foo");
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

    @Override
    protected void addContainers(List expectedList) {
        expectedList.add(DefaultClassLoadingPicoContainer.class);
        expectedList.add(DefaultPicoContainer.class);
    }


    @Test()
    public void visitingClassesSiblingToAClassWithRegexSubsetWorksWithRecursive() {

        final StringBuilder sb = new StringBuilder();
    	DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer();
        int found = pico.visit(new ClassName("org.picocontainer.DefaultPicoContainer"), ".*Container\\.class", true, new DefaultClassLoadingPicoContainer.ClassNameVisitor() {
            public void classFound(Class clazz) {
                sb.append(clazz.getName()).append("\n");
            }
        });
        
        //TODO: these should be "contains" so the test doesn't fail every time we add a container.
        assertEquals("org.picocontainer.classname.ClassLoadingPicoContainer\n" +
                "org.picocontainer.classname.DefaultClassLoadingPicoContainer$AsPropertiesPicoContainer\n" +
                "org.picocontainer.classname.DefaultClassLoadingPicoContainer\n" +
                "org.picocontainer.containers.AbstractDelegatingMutablePicoContainer\n" +
                "org.picocontainer.containers.AbstractDelegatingPicoContainer\n" +
                "org.picocontainer.containers.CommandLinePicoContainer\n" +
                "org.picocontainer.containers.CompositePicoContainer\n" +
                "org.picocontainer.containers.EmptyPicoContainer\n" +
                "org.picocontainer.containers.ImmutablePicoContainer\n" +
                "org.picocontainer.containers.JSRPicoContainer\n"+
                "org.picocontainer.containers.PropertiesPicoContainer\n" +
                "org.picocontainer.containers.SystemPropertiesPicoContainer\n" +
                "org.picocontainer.containers.TieringPicoContainer\n" +
                "org.picocontainer.containers.TransientPicoContainer\n" +
                "org.picocontainer.DefaultPicoContainer$AsPropertiesPicoContainer\n" +
                "org.picocontainer.DefaultPicoContainer\n" +
                "org.picocontainer.MutablePicoContainer\n" +
                "org.picocontainer.PicoContainer\n",
                sb.toString());
        
        //Same here.
        assertEquals(18, found);
    }

    @Test()
    public void visitingClassesSiblingToAClassWithRegexSubsetWorksWithoutRecursive() {

        final StringBuilder sb = new StringBuilder();
    	DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer();
        int found = pico.visit(new ClassName("org.picocontainer.DefaultPicoContainer"), ".*Container\\.class", false, new DefaultClassLoadingPicoContainer.ClassNameVisitor() {
            public void classFound(Class clazz) {
                sb.append(clazz.getName()).append("\n");
            }
        });
        assertEquals("org.picocontainer.DefaultPicoContainer$AsPropertiesPicoContainer\n" +
                "org.picocontainer.DefaultPicoContainer\n" +
                "org.picocontainer.MutablePicoContainer\n" +
                "org.picocontainer.PicoContainer\n",
                sb.toString());
        assertEquals(4, found);
    }


    @Test(expected = DefaultClassLoadingPicoContainer.CannotListClassesInAJarException.class)
    public void visitingFailsIfClassInAJar() {

    	DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer();
        int found = pico.visit(new ClassName("java.util.ArrayList"), ".*List\\.class", false, new DefaultClassLoadingPicoContainer.ClassNameVisitor() {
            public void classFound(Class clazz) {
            }
        });
    }

    @Test(expected = PicoClassNotFoundException.class)
    public void visitingFailsIfBogusClass() {

    	DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer();
        pico.visit(new ClassName("org.picocontainer.BlahBlah"), ".*Container\\.class", false, new DefaultClassLoadingPicoContainer.ClassNameVisitor() {
            public void classFound(Class clazz) {
            	//Does nothing.
            }
        });
    }

    @Test(expected = CannotListClassesInAJarException.class)
    public void visitingFailsIfJDKClass() {
        DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer();
        pico.visit(new ClassName("java.util.ArrayList"), 
            ".*Container\\.class",
            false, 
            new DefaultClassLoadingPicoContainer.ClassNameVisitor() {
            public void classFound(Class clazz) {
                //Does nothing, we're expecting the class to get thrown.
            }
        });
    }

    @Test
    public void visitingPassesIfClassInAJar() {

    	DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer();
        final StringBuilder sb = new StringBuilder();
        int found = pico.visit(new ClassName("com.thoughtworks.xstream.XStream"),
        			".*m\\.class", 
        			false, 
        			new DefaultClassLoadingPicoContainer.ClassNameVisitor() {
        	
            public void classFound(Class clazz) {
                sb.append(clazz.getName()).append("\n");
            }
        });
        assertEquals("com.thoughtworks.xstream.XStream\n",
                sb.toString());
        assertEquals(1, found);
    }

    @Test
    public void visitingPassesIfClassInAJarRecursively() {

    	DefaultClassLoadingPicoContainer pico = new DefaultClassLoadingPicoContainer();
        final StringBuilder sb = new StringBuilder();
        int found = pico.visit(new ClassName("com.thoughtworks.xstream.XStream"), ".*m\\.class", true, new DefaultClassLoadingPicoContainer.ClassNameVisitor() {
            public void classFound(Class clazz) {
                sb.append(clazz.getName()).append("\n");
            }
        });
        assertEquals("com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom\n" +
                "com.thoughtworks.xstream.core.util.PrioritizedList$PrioritizedItem\n" +
                "com.thoughtworks.xstream.core.util.CustomObjectInputStream\n" +
                "com.thoughtworks.xstream.core.util.CustomObjectOutputStream\n" +
                "com.thoughtworks.xstream.XStream\n",
                sb.toString());
        assertEquals(5, found);
    }


}
