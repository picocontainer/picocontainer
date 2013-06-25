/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.script.rhino;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;
import org.mozilla.javascript.JavaScriptException;
import com.picocontainer.script.AbstractScriptedContainerBuilderTestCase;
import com.picocontainer.script.ContainerBuilder;
import com.picocontainer.script.NoOpPostBuildContainerAction;
import com.picocontainer.script.TestHelper;
import com.picocontainer.script.testmodel.WebServer;
import com.picocontainer.script.testmodel.WebServerConfig;
import com.picocontainer.script.testmodel.X;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.containers.ImmutablePicoContainer;

public class JavascriptContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {

    @Test public void testInstantiateBasicScriptable() throws IOException, ClassNotFoundException, PicoCompositionException, JavaScriptException {

        Reader script = new StringReader("" +
        		"importPackage(Packages.com.picocontainer.script) \n" +
                "importPackage(Packages.com.picocontainer.classname) \n" +
                "var pico = new DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent(Packages.com.picocontainer.script.testmodel.DefaultWebServerConfig)\n");

        PicoContainer pico = buildContainer(new JavascriptContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        assertNotNull(pico.getComponent(WebServerConfig.class).getClass());
    }

    @Test public void testInstantiateWithBespokeComponentAdapter() throws IOException, ClassNotFoundException, PicoCompositionException, JavaScriptException {

        Reader script = new StringReader("" +
        		"importPackage(Packages.com.picocontainer.script) \n" +
        		"importPackage(Packages.com.picocontainer.injectors) \n" +
                "importPackage(Packages.com.picocontainer.classname) \n" +
                "var pico = new DefaultClassLoadingPicoContainer(new ConstructorInjection())\n" +
                "pico.addComponent(Packages.com.picocontainer.script.testmodel.DefaultWebServerConfig)\n" +
                "pico.addComponent(Packages.com.picocontainer.script.testmodel.WebServerImpl)\n");

        PicoContainer pico = buildContainer(new JavascriptContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        Object wsc = pico.getComponent(WebServerConfig.class);
        Object ws1 = pico.getComponent(WebServer.class);
        Object ws2 = pico.getComponent(WebServer.class);

        assertNotSame(ws1, ws2);

        assertEquals("ClassLoader should be the same for both components", ws1.getClass().getClassLoader(), wsc.getClass().getClassLoader());
    }

    @Test public void testClassLoaderHierarchy() throws ClassNotFoundException, IOException, PicoCompositionException, JavaScriptException {
        File testCompJar = TestHelper.getTestCompJarFile();
        assertTrue(testCompJar.isFile());

        final String testCompJarPath = testCompJar.getCanonicalPath().replace('\\', '/');
        Reader script = new StringReader(
        		"importPackage(Packages.java.io) \n" +
        		"importPackage(Packages.com.picocontainer.script) \n" +
        		"importPackage(Packages.com.picocontainer) \n" +
        		"importPackage(Packages.com.picocontainer.injectors) \n" +
                "importPackage(Packages.com.picocontainer.classname) \n" +
                "\n" +
                "var pico = new DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent('parentComponent', Packages." + FooTestComp.class.getName() + ", Parameter.ZERO)\n" +
                "child = pico.makeChildContainer()\n" +
                "url = new File('" + testCompJarPath + "').toURL()\n" +
                "child.addClassLoaderURL(url)\n" +
                "child.addComponent('childComponent', new ClassName('TestComp'), Parameter.ZERO)\n" +
                "pico.addComponent('wayOfPassingSomethingToTestEnv', child.getComponent('childComponent'), Parameter.ZERO)"); // ugly hack for testing
        JavascriptContainerBuilder builder = new JavascriptContainerBuilder(script, getClass().getClassLoader());
        PicoContainer pico = buildContainer(builder, null, "SOME_SCOPE");

        Object parentComponent = pico.getComponent("parentComponent");

        Object childComponent = pico.getComponent("wayOfPassingSomethingToTestEnv");



        ClassLoader classLoader1 = parentComponent.getClass().getClassLoader();
        ClassLoader classLoader2 = childComponent.getClass().getClassLoader();
        assertNotSame(classLoader1, classLoader2);
        /*
        system cl -> loads FooTestComp
          parent container cl
            child container cl -> loads TestComp
        */
        Class<?> aClass = childComponent.getClass();
        ClassLoader loader2 = aClass.getClassLoader();
        ClassLoader loader1 = loader2.getParent();
        ClassLoader loader = loader1.getParent();
        assertSame(parentComponent.getClass().getClassLoader(), loader);
    }

    @Test
    public void testExecutionWithinCustomClassLoader() throws MalformedURLException, ClassNotFoundException {
        File testCompJar = TestHelper.getTestCompJarFile();
        assertTrue(testCompJar.isFile());
        URL compJarURL = testCompJar.toURI().toURL();
        URLClassLoader cl  = new URLClassLoader(new URL[] {compJarURL}, getClass().getClassLoader());
        assertNotNull(cl.loadClass("TestComp"));
        Reader script = new StringReader(
        		"importPackage(Packages.java.io) \n" +
        		"importPackage(Packages.com.picocontainer.script) \n" +
        		"importPackage(Packages.com.picocontainer) \n" +
        		"importPackage(Packages.com.picocontainer.injectors) \n" +
                "importPackage(Packages.com.picocontainer.classname) \n" +
                "\n" +
                "var pico = new PicoBuilder().withCaching().withLifecycle().build();\n" +
                "pico.addComponent('TestComp', Packages.TestComp);\n"
           );

        JavascriptContainerBuilder builder = new JavascriptContainerBuilder(script, cl);
        PicoContainer pico = buildContainer(builder, null, "SOME_SCOPE");
        assertNotNull(pico.getComponent("TestComp"));
        assertEquals("TestComp", pico.getComponent("TestComp").getClass().getName());
    }

    @Test public void testRegisterComponentInstance() throws JavaScriptException, IOException {
        Reader script = new StringReader("" +
        		"importPackage(Packages.com.picocontainer.script) \n" +
        		"importPackage(Packages.com.picocontainer) \n" +
                "importPackage(Packages.com.picocontainer.classname) \n" +
                "var pico = new DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent(new Packages." + FooTestComp.class.getName() + "())\n" +
                "pico.addComponent('foo', new Packages." + FooTestComp.class.getName() + "(), java.lang.reflect.Array.newInstance(Parameter,0))\n");

        PicoContainer pico = buildContainer(new JavascriptContainerBuilder(script, getClass().getClassLoader()), null, "SOME_SCOPE");

        assertEquals(FooTestComp.class, pico.getComponents().get(0).getClass());
        assertEquals(FooTestComp.class, pico.getComponents().get(1).getClass());
    }

    public static class FooTestComp {

    }

    @Test public void testContainerCanBeBuiltWithParent() {
        Reader script = new StringReader("" +
                "importPackage(Packages.com.picocontainer.classname) \n" +
                "var pico = new DefaultClassLoadingPicoContainer(parent)\n");
        PicoContainer parent = new DefaultPicoContainer();
        PicoContainer ipc = new ImmutablePicoContainer(parent);
        PicoContainer pico = buildContainer(new JavascriptContainerBuilder(script, getClass().getClassLoader()), ipc, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
    }


    @Test public void testAutoStartingContainerBuilderStarts() {
        X.reset();
        Reader script = new StringReader("" +
                "var pico = parent.makeChildContainer() \n" +
                "pico.addComponent(Packages.com.picocontainer.script.testmodel.A)\n" +
                "");
        PicoContainer parent = new PicoBuilder().withLifecycle().withCaching().build();
        PicoContainer pico = buildContainer(
        			new JavascriptContainerBuilder(script, getClass().getClassLoader())
        		, parent, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
        assertEquals("<A",X.componentRecorder);
        X.reset();
	}

    @Test public void testNonAutoStartingContainerBuildDoesntAutostart() {
        X.reset();
        Reader script = new StringReader("" +
                "var pico = parent.makeChildContainer() \n" +
                "pico.addComponent(Packages.com.picocontainer.script.testmodel.A)\n" +
                "");
        PicoContainer parent = new PicoBuilder().withLifecycle().withCaching().build();
        ContainerBuilder containerBuilder = new JavascriptContainerBuilder(script, getClass().getClassLoader()).setPostBuildAction(new NoOpPostBuildContainerAction());
        PicoContainer pico = buildContainer(containerBuilder, parent, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
        assertEquals("",X.componentRecorder);
        X.reset();
    }

    @Test
    public void testApiJavascriptImpedenceMismatch() {
        X.reset();
        Reader script = new StringReader("" +
        		"importClass(Packages.com.picocontainer.NameBinding);\n\n" +
                "var pico = parent.makeChildContainer() \n" +
                "pico.addComponent(Packages.com.picocontainer.script.testmodel.A)\n" +
                "var ca = pico.getComponentAdapter(Packages.com.picocontainer.script.testmodel.A, NameBinding.NULL);\n" +
                "");
        PicoContainer parent = new PicoBuilder().withLifecycle().withCaching().build();
        ContainerBuilder containerBuilder = new JavascriptContainerBuilder(script, getClass().getClassLoader()).setPostBuildAction(new NoOpPostBuildContainerAction());
        PicoContainer pico = buildContainer(containerBuilder, parent, "SOME_SCOPE");
        assertNotNull(pico);
    }

}
