package com.picocontainer.script;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import com.picocontainer.script.testmodel.WebServer;
import com.picocontainer.script.testmodel.WebServerConfig;
import com.picocontainer.script.testmodel.X;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.containers.ImmutablePicoContainer;

/**
 * Most of these tests were copied from the JavascriptContainerBuilder.
 *
 */
public class JdkScriptingContainerBuilderTestCase extends AbstractScriptedContainerBuilderTestCase {

    @Test public void testInstantiateBasicScriptable() throws IOException, ClassNotFoundException, PicoCompositionException {

        Reader script = new StringReader("" +
        		"importPackage(Packages.com.picocontainer.script) \n" +
                "importPackage(Packages.com.picocontainer.classname) \n" +
                "var pico = new DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent(Packages.com.picocontainer.script.testmodel.DefaultWebServerConfig)\n");

        PicoContainer pico = buildContainer(new JdkScriptingContainerBuilder("js", script, getClass().getClassLoader()), null, "SOME_SCOPE");

        assertNotNull(pico.getComponent(WebServerConfig.class).getClass());
    }

    @Test public void testInstantiateWithBespokeComponentAdapter() throws IOException, ClassNotFoundException, PicoCompositionException {

        Reader script = new StringReader("" +
        		"importPackage(Packages.com.picocontainer.script) \n" +
        		"importPackage(Packages.com.picocontainer.injectors) \n" +
                "importPackage(Packages.com.picocontainer.classname) \n" +
                "var pico = new DefaultClassLoadingPicoContainer(new ConstructorInjection())\n" +
                "pico.addComponent(Packages.com.picocontainer.script.testmodel.DefaultWebServerConfig)\n" +
                "pico.addComponent(Packages.com.picocontainer.script.testmodel.WebServerImpl)\n");

        PicoContainer pico = buildContainer(new JdkScriptingContainerBuilder("js", script, getClass().getClassLoader()), null, "SOME_SCOPE");

        Object wsc = pico.getComponent(WebServerConfig.class);
        Object ws1 = pico.getComponent(WebServer.class);
        Object ws2 = pico.getComponent(WebServer.class);

        assertNotSame(ws1, ws2);

        assertEquals("ClassLoader should be the same for both components", ws1.getClass().getClassLoader(), wsc.getClass().getClassLoader());
    }

    @Test public void testClassLoaderHierarchy() throws ClassNotFoundException, IOException, PicoCompositionException  {
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
        JdkScriptingContainerBuilder builder = new JdkScriptingContainerBuilder("js",script, getClass().getClassLoader());
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

    @Test public void testRegisterComponentInstance() throws IOException {
        Reader script = new StringReader("" +
        		"importPackage(Packages.com.picocontainer.script) \n" +
        		"importPackage(Packages.com.picocontainer) \n" +
                "importPackage(Packages.com.picocontainer.classname) \n" +
                "var pico = new DefaultClassLoadingPicoContainer()\n" +
                "pico.addComponent(new Packages." + FooTestComp.class.getName() + "())\n" +
                "pico.addComponent('foo', new Packages." + FooTestComp.class.getName() + "(), java.lang.reflect.Array.newInstance(Parameter,0))\n");

        PicoContainer pico = buildContainer(new JdkScriptingContainerBuilder("js",script, getClass().getClassLoader()), null, "SOME_SCOPE");

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
        PicoContainer pico = buildContainer(new JdkScriptingContainerBuilder("js",script, getClass().getClassLoader()), ipc, "SOME_SCOPE");
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
        PicoContainer pico = buildContainer(new JdkScriptingContainerBuilder("js",script, getClass().getClassLoader()), parent, "SOME_SCOPE") ;
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
        ContainerBuilder containerBuilder = new JdkScriptingContainerBuilder("js",script, getClass().getClassLoader()).setPostBuildAction(new NoOpPostBuildContainerAction());
        PicoContainer pico = buildContainer(containerBuilder, parent, "SOME_SCOPE");
        //PicoContainer.getParent() is now ImmutablePicoContainer
        assertNotSame(parent, pico.getParent());
        assertEquals("",X.componentRecorder);
        X.reset();
    }


}
