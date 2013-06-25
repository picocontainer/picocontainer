/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.script.groovy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import groovy.lang.Binding;

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
import com.picocontainer.script.testmodel.A;
import com.picocontainer.script.testmodel.X;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoContainer;

/**
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 */
public class GroovyContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {

    @Test public void testContainerCanBeBuiltWithParent() {
        Reader script = new StringReader("" +
                "builder = new com.picocontainer.script.groovy.GroovyNodeBuilder()\n" +
                "def pico = builder.container(parent:parent) { \n" +
                "  component(StringBuffer)\n" +
                "}");
        PicoContainer parent = new DefaultPicoContainer();
        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
        assertEquals(StringBuffer.class, pico.getComponent(StringBuffer.class).getClass());
    }

    @Test public void testAdditionalBindingViaSubClassing() {
                Reader script = new StringReader("" +
                "builder = new com.picocontainer.script.groovy.GroovyNodeBuilder()\n" +
                "def pico = builder.container(parent:parent) { \n" +
                "  component(key:String.class, instance:foo)\n" +
                "}");

		PicoContainer parent = new DefaultPicoContainer();
		PicoContainer pico = buildContainer(new SubclassGroovyContainerBuilder(script, getClass().getClassLoader()),
		        parent, "SOME_SCOPE");

		assertNotSame(parent, pico.getParent());
		assertEquals("bar", pico.getComponent(String.class));
	}

    @Test public void testBuildingWithDefaultBuilder() {
        // NOTE script does NOT define a "builder"
        Reader script = new StringReader("" +
                "def pico = builder.container(parent:parent) { \n" +
                "  component(key:String.class, instance:'foo')\n" +
                "}");

		PicoContainer parent = new DefaultPicoContainer();
		PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), parent,
		        "SOME_SCOPE");

		assertNotSame(parent, pico.getParent());
		assertEquals("foo", pico.getComponent(String.class));
	}

    @Test public void testBuildingWithAppendingNodes() {
        Reader script = new StringReader("" +
                "def pico = builder.container(parent:parent) { \n" +
                			"}\n" +
                			"\n" +
                			"builder.append(container:pico) {" +
                			"  component(key:String.class, instance:'foo')\n" +
                			"}\n"
		        + "");

		PicoContainer parent = new DefaultPicoContainer();
		PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), parent,
		        "SOME_SCOPE");

		assertNotSame(parent, pico.getParent());
		assertEquals("foo", pico.getComponent(String.class));
	}

    @Test public void testBuildingWithPicoSyntax() {
        Reader script = new StringReader("" +
                "parent.addComponent('foo', java.lang.String)\n"  +
                "def pico = new com.picocontainer.DefaultPicoContainer(parent)\n" +
                "pico.addComponent(com.picocontainer.script.testmodel.A)\n" +
                "");

        PicoContainer parent = new DefaultPicoContainer();
        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");

        assertNotSame(parent, pico.getParent());
        assertNotNull(pico.getComponent(A.class));
        assertNotNull(pico.getComponent("foo"));
    }



    @Test public void testBuildingWithPicoSyntaxAndNullParent() {
        Reader script = new StringReader("" +
                "def pico = new com.picocontainer.DefaultPicoContainer(parent)\n" +
                "pico.addComponent(com.picocontainer.script.testmodel.A)\n" +
                "");

        PicoContainer parent = null;
        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");

        assertNotSame(parent, pico.getParent());
        assertNotNull(pico.getComponent(A.class));
    }

	/**
	 * Child SubclassGroovyContainerBuilder which adds additional bindings
	 */
	private class SubclassGroovyContainerBuilder extends GroovyContainerBuilder {
		public SubclassGroovyContainerBuilder(final Reader script, final ClassLoader classLoader) {
			super(script, classLoader);
		}

		@Override
		protected void handleBinding(final Binding binding) {
			super.handleBinding(binding);

			binding.setVariable("foo", "bar");
		}

	}

	@Test
	public void testRunningGroovyScriptWithinCustomClassLoader() throws MalformedURLException, ClassNotFoundException {
		Reader script = new StringReader("" +
				"def pico = new com.picocontainer.PicoBuilder().withCaching().withLifecycle().build();\n" +
				"pico.addComponent(\"TestComp\", TestComp);"
			);
        File testCompJar = TestHelper.getTestCompJarFile();
        assertTrue(testCompJar.isFile());
        URL compJarURL = testCompJar.toURI().toURL();
        URLClassLoader cl  = new URLClassLoader(new URL[] {compJarURL}, getClass().getClassLoader());
        assertNotNull(cl.loadClass("TestComp"));

        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, cl), null, null);
        assertNotNull(pico.getComponent("TestComp"));
        assertEquals("TestComp", pico.getComponent("TestComp").getClass().getName());
	}


	@Test public void testAutoStar1tingContainerBuilderStarts() {
        X.reset();
        Reader script = new StringReader("" +
                "def pico = builder.container(parent:parent) { \n" +
                "  component(com.picocontainer.script.testmodel.A)\n" +
                "}");
        PicoContainer parent = new PicoBuilder().withLifecycle().withCaching().build();
        PicoContainer pico = buildContainer(new GroovyContainerBuilder(script, getClass().getClassLoader()), parent, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
        assertEquals("<A",X.componentRecorder);
        X.reset();
	}

    @Test public void testNonAutoStartingContainerBuildDoesntAutostart() {
        X.reset();
        Reader script = new StringReader("" +
        		"import com.picocontainer.script.testmodel.A\n" +
                "def pico = builder.container(parent:parent) { \n" +
                "  component(A)\n" +
                "}");
        PicoContainer parent = new PicoBuilder().withLifecycle().withCaching().build();
        ContainerBuilder containerBuilder = new GroovyContainerBuilder(script, getClass().getClassLoader()).setPostBuildAction(new NoOpPostBuildContainerAction());
        PicoContainer pico = buildContainer(containerBuilder, parent, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
        assertEquals("",X.componentRecorder);
        X.reset();
    }


}
