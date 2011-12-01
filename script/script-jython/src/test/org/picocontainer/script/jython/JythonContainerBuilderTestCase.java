/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.script.jython;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Ignore;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.injectors.AbstractInjector.UnsatisfiableDependenciesException;
import org.picocontainer.script.AbstractScriptedContainerBuilderTestCase;
import org.picocontainer.script.ContainerBuilder;
import org.picocontainer.script.NoOpPostBuildContainerAction;
import org.picocontainer.script.TestHelper;
import org.picocontainer.script.testmodel.A;
import org.picocontainer.script.testmodel.WebServer;
import org.picocontainer.script.testmodel.WebServerImpl;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public class JythonContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {
 
	
	@Test
	public void testExecutionWithinCustomClassLoader() 
			throws MalformedURLException, ClassNotFoundException {
		Reader script = new StringReader("" +
        		"from org.picocontainer import *;\n" +
        		"from org.picocontainer.parameters import ComponentParameter;\n" +
        		"import TestComp;\n" +
                "pico = PicoBuilder().withLifecycle().withCaching().build();\n" +
				"pico.addComponent(\"TestComp\",TestComp, Parameter.ZERO);\n"
			);
        File testCompJar = TestHelper.getTestCompJarFile();
        assertTrue(testCompJar.isFile());
        URL compJarURL = testCompJar.toURI().toURL();
        final URLClassLoader cl  = new URLClassLoader(new URL[] {compJarURL}, 
        		getClass().getClassLoader());
        assertNotNull(cl.loadClass("TestComp"));
        
        ContainerBuilder containerBuilder = new JythonContainerBuilder(script, cl);
        
        PicoContainer pico = buildContainer(containerBuilder, null, null);
        assertNotNull(pico.getComponent("TestComp"));
        assertEquals("TestComp", pico.getComponent("TestComp").getClass().getName());
		
	}

	@Test public void testDependenciesAreSatisfiable() {
        Reader script = new StringReader(
                "from org.picocontainer.classname import *\n" +
                "from org.picocontainer.script import *\n" +
                "from org.picocontainer.script.testmodel import *\n" +
                "pico = DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent(WebServerImpl)\n" +
                "pico.addComponent(DefaultWebServerConfig)\n");
        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        assertNotNull(pico.getComponent(WebServer.class));
    }

    @Test public void testDependenciesAreSatisfiableByParentContainer() {
        Reader script = new StringReader("" +
                "from org.picocontainer.classname import *\n" +
                "from org.picocontainer.script import *\n" +
                "from org.picocontainer.script.testmodel import *\n" +
                "from org.picocontainer import Parameter\n"+
                "pico = DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent(DefaultWebServerConfig)\n" +
                "child = pico.makeChildContainer()\n" +
                "child.addComponent(WebServerImpl)\n" +
                "pico.addComponent('wayOfPassingSomethingToTestEnv', child.getComponent(WebServerImpl), Parameter.DEFAULT)");
        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        assertNotNull((WebServerImpl) pico.getComponent("wayOfPassingSomethingToTestEnv"));
    }

    @Test(expected=UnsatisfiableDependenciesException.class)
    public void testDependenciesAreUnsatisfiable() {
        Reader script = new StringReader("" +
                "from org.picocontainer.script.testmodel import *\n" +
                "from org.picocontainer.classname import *\n" +
                "from org.picocontainer.script import *\n" +      
                "pico = DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent(WebServerImpl)\n");
        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        pico.getComponent(WebServer.class);
    }

    @Test public void testContainerCanBeBuiltWithParent() {
        Reader script = new StringReader("" +
                "from org.picocontainer.script import *\n" +
                "from org.picocontainer.classname import *\n" +
                "from org.picocontainer.script.testmodel import *\n" +
                "pico = ScriptedBuilder(parent).withLifecycle().build()\n");
        PicoContainer parent = new DefaultPicoContainer();
        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");
        //pico.getParent() is immutable
        assertNotSame(parent, pico.getParent());
    }
    
	@Test public void testAutoStartingContainerBuilderStarts() {
        A.reset();
        Reader script = new StringReader("" +
                "from org.picocontainer.script import *\n" +
                "from org.picocontainer.classname import *\n" +
                "from org.picocontainer.script.testmodel import *\n" +
                "pico = parent.makeChildContainer() \n" +
                "pico.addComponent(A)\n" +
                "");
        PicoContainer parent = new PicoBuilder().withLifecycle().withCaching().build();
        PicoContainer pico = buildContainer(
    				new JythonContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");
        //PicoContainer.getParent() is immutable
        assertNotSame(parent, pico.getParent());
        assertEquals("<A",A.componentRecorder);		
        A.reset();
	}
	
	@Test public void testNonAutoStartingContainerBuildDoesntAutostart() {
        A.reset();
        Reader script = new StringReader("" +
                "from org.picocontainer.script.testmodel import *\n" +
                "from org.picocontainer.classname import *\n" +
                "from org.picocontainer.script import *\n" +                
                "pico = parent.makeChildContainer() \n" +
                "pico.addComponent(A)\n" +
                "");
        PicoContainer parent = new PicoBuilder().withLifecycle().withCaching().build();
        ContainerBuilder containerBuilder = new JythonContainerBuilder(script, 
        		getClass().getClassLoader()).setPostBuildAction(new NoOpPostBuildContainerAction());
        PicoContainer pico = buildContainer(containerBuilder, parent, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
        assertEquals("",A.componentRecorder);
        A.reset();
    }

}
