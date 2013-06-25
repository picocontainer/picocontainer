/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.script.jython;

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

import org.junit.Test;
import com.picocontainer.script.AbstractScriptedContainerBuilderTestCase;
import com.picocontainer.script.ContainerBuilder;
import com.picocontainer.script.NoOpPostBuildContainerAction;
import com.picocontainer.script.TestHelper;
import com.picocontainer.script.testmodel.WebServer;
import com.picocontainer.script.testmodel.X;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoContainer;
import com.picocontainer.injectors.AbstractInjector.UnsatisfiableDependenciesException;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public class JythonContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {


	@Test
	public void testExecutionWithinCustomClassLoader()
			throws MalformedURLException, ClassNotFoundException {
		Reader script = new StringReader("" +
        		"from com.picocontainer import *;\n" +
        		"from com.picocontainer.parameters import *;\n" +
        		"import TestComp;\n" +
                "pico = PicoBuilder().withLifecycle().withCaching().build();\n" +
				"pico.addComponent(\"TestComp\",TestComp, DefaultConstructorParameter.INSTANCE);\n"
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
                "from com.picocontainer.classname import *\n" +
                "from com.picocontainer.script import *\n" +
                "from com.picocontainer.script.testmodel import *\n" +
                "pico = DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent(WebServerImpl)\n" +
                "pico.addComponent(DefaultWebServerConfig)\n");
        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        assertNotNull(pico.getComponent(WebServer.class));
    }

    @Test public void testDependenciesAreSatisfiableByParentContainer() {
        Reader script = new StringReader("" +
                "from com.picocontainer.classname import *\n" +
                "from com.picocontainer.script import *\n" +
                "from com.picocontainer.script.testmodel import *\n" +
                "from com.picocontainer.parameters import DefaultConstructorParameter\n"+
                "pico = DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent(DefaultWebServerConfig)\n" +
                "child = pico.makeChildContainer()\n" +
                "child.addComponent(WebServerImpl)\n" +
                "pico.addComponent('wayOfPassingSomethingToTestEnv', child.getComponent(WebServerImpl), DefaultConstructorParameter.INSTANCE)");
        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        assertNotNull(pico.getComponent("wayOfPassingSomethingToTestEnv"));
    }

    @Test(expected=UnsatisfiableDependenciesException.class)
    public void testDependenciesAreUnsatisfiable() {
        Reader script = new StringReader("" +
                "from com.picocontainer.script.testmodel import *\n" +
                "from com.picocontainer.classname import *\n" +
                "from com.picocontainer.script import *\n" +
                "pico = DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent(WebServerImpl)\n");
        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");
        pico.getComponent(WebServer.class);
    }

    @Test public void testContainerCanBeBuiltWithParent() {
        Reader script = new StringReader("" +
                "from com.picocontainer.script import *\n" +
                "from com.picocontainer.classname import *\n" +
                "from com.picocontainer.script.testmodel import *\n" +
                "pico = ScriptedBuilder(parent).withLifecycle().build()\n");
        PicoContainer parent = new DefaultPicoContainer();
        PicoContainer pico = buildContainer(new JythonContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");
        //pico.getParent() is immutable
        assertNotSame(parent, pico.getParent());
    }

	@Test public void testAutoStartingContainerBuilderStarts() {
        X.reset();
        Reader script = new StringReader("" +
                "from com.picocontainer.script import *\n" +
                "from com.picocontainer.classname import *\n" +
                "from com.picocontainer.script.testmodel import *\n" +
                "pico = parent.makeChildContainer() \n" +
                "pico.addComponent(A)\n" +
                "");
        PicoContainer parent = new PicoBuilder().withLifecycle().withCaching().build();
        PicoContainer pico = buildContainer(
    				new JythonContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");
        //PicoContainer.getParent() is immutable
        assertNotSame(parent, pico.getParent());
        assertEquals("<A",X.componentRecorder);
        X.reset();
	}

	@Test public void testNonAutoStartingContainerBuildDoesntAutostart() {
        X.reset();
        Reader script = new StringReader("" +
                "from com.picocontainer.script.testmodel import *\n" +
                "from com.picocontainer.classname import *\n" +
                "from com.picocontainer.script import *\n" +
                "pico = parent.makeChildContainer() \n" +
                "pico.addComponent(A)\n" +
                "");
        PicoContainer parent = new PicoBuilder().withLifecycle().withCaching().build();
        ContainerBuilder containerBuilder = new JythonContainerBuilder(script,
        		getClass().getClassLoader()).setPostBuildAction(new NoOpPostBuildContainerAction());
        PicoContainer pico = buildContainer(containerBuilder, parent, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
        assertEquals("",X.componentRecorder);
        X.reset();
    }

}
