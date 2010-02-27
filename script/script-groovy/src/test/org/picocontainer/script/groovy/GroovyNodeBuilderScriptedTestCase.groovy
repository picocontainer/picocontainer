package org.picocontainer.script.groovy

import org.picocontainer.defaults.ComponentParameter
import org.picocontainer.defaults.UnsatisfiableDependenciesException

import org.picocontainer.script.groovy.NanoContainerBuilder
import org.picocontainer.script.testmodel.DefaultWebServerConfig
import org.picocontainer.script.testmodel.WebServer
import org.picocontainer.script.testmodel.WebServerConfig
import org.picocontainer.script.testmodel.WebServerConfigBean
import org.picocontainer.script.testmodel.WebServerImpl
import java.io.File

class GroovyNodeBuilderScriptedTestCase extends GroovyTestCase {

    void testInstantiateBasicComponent() {

        A.reset()

        def builder = new GroovyNodeBuilder()
        def scripted = builder.container() {
            component(A)
        }

        startAndDispose(scripted)

        assertEquals("Should match the expression", "<A!A", A.componentRecorder)
    }

    void testInstantiateBasicComponentInDeeperTree() {

        A.reset()

        def builder = new GroovyNodeBuilder()
        def scripted = builder.container {
            container() {
                component(A)
            }
        }

        startAndDispose(scripted)

        assertEquals("Should match the expression", "<A!A", A.componentRecorder)
    }

    void testInstantiateWithImpossibleComponentDependanciesConsideringTheHierarchy() {

        // A and C have no no dependencies. B Depends on A.

        try {
            def builder = new GroovyNodeBuilder()
            def scripted = builder.container {
                component(B)
                container() {
                    component(A)
                }
                component(C)
            }

            startAndDispose(scripted)

            fail("Should not have been able to instansiate component tree due to visibility/parent reasons.")
        }
        catch (UnsatisfiableDependenciesException e) {
        }
    }

    void testInstantiateWithBespokeComponentAdapter() {

        def sb = new StringBuffer();

        def builder = new GroovyNodeBuilder()
        def componentFactory = new TestComponentAdapterFactory(sb)
        def scripted = builder.container(componentFactory:componentFactory) {
            component(key:WebServerConfig, class:DefaultWebServerConfig)
            component(key:WebServer, class:WebServerImpl)
        }

        startAndDispose(scripted)

        assertTrue(sb.toString().indexOf("called") != -1)
    }

    void testInstantiateWithInlineConfiguration() {

        def builder = new GroovyNodeBuilder()
        def scripted = builder.container {
            bean(beanClass:WebServerConfigBean, host:'foobar.com', port:4321)
            component(key:WebServer, class:WebServerImpl)
        }

        startAndDispose(scripted)

        assertTrue("WebServerConfigBean and WebServerImpl expected", scripted.pico.getComponentInstances().size() == 2)

        def wsc = scripted.pico.getComponentInstanceOfType(WebServerConfig)
        assertEquals("foobar.com", wsc.getHost())
        assertTrue(wsc.getPort() == 4321)
    }

    void testSoftInstantiateWithChildContainerWithDynamicClassPath() {

        File testCompJar = new File(System.getProperty("testcomp.jar"))
        def testCompJar2 = new File(testCompJar.getParent(),"TestComp2.jar")
        def compJarPath = testCompJar.getCanonicalPath()
        def compJarPath2 = testCompJar2.getCanonicalPath()

        def builder = new GroovyNodeBuilder()
        def child = null
        def parent = builder.container {
            classPathElement(path:compJarPath)
            component(class:StringBuffer)
            component(class:"TestComp")
            child = container() {
                classPathElement(path:compJarPath2)
                component(class:"TestComp2")
            }
        }
        assertTrue(parent.pico.getComponentInstances().size() == 2)
        assertTrue(child.pico.getComponentInstances().size() == 1)
        assertNotNull(child.getComponentInstanceOfType("TestComp2"))

    }

    /*
    Now that the builder is building NanoContainer instances, we must agree on what the class
    paramter means - is it the NanoContainer type or the nested PicoContainer type?
    Is it even desirable to have bespoke implementations? Can we get the same flexibility
    by specifying various delegation classes rather than - sigh - inheritance? Inheritance
    is always painful to deal with. It's a pattern as bad as singletons...
    Aslak
    */
    void FIXMEtestInstantiateBasicComponentInCustomContainer() {

        A.reset()

        def builder = new GroovyNodeBuilder()
        def scripted = builder.container(class:TestContainer) {
            component(A)
        }

        startAndDispose(scripted)
        assertEquals("Should match the expression", "<A!A", A.componentRecorder)
        assertEquals("org.picocontainer.script.groovy.TestContainer",scripted.getClass().getName())
    }

    void testInstantiateBasicComponentWithDeepTree() {

        A.reset()

        def builder = new GroovyNodeBuilder()
        def scripted = builder.container {
            container() {
                container() {
                    component(A)
                }
            }
            component(HashMap.class)
        }

        startAndDispose(scripted)

        assertEquals("Should match the expression", "<A!A", A.componentRecorder)
    }

    void FIXMEtestInstantiateBasicComponentWithDeepNamedTree() {

        A.reset()

        def builder = new GroovyNodeBuilder()
        def scripted = builder.container {
            container(name:"huey") {
                container(name:"duey") {
                    component(key:"Luis", class:A)
                }
            }
            component(HashMap.class)
        }

        startAndDispose(scripted)

        assertEquals("Should match the expression", "<A!A", A.componentRecorder)
        Object o = scripted.pico.getComponentInstance("huey/duey/Luis")
        assertNotNull(o)
    }

    public void testComponentInstances() {

        def builder = new GroovyNodeBuilder()
        def scripted = builder.container {
            component(key:"Louis", instance:"Armstrong")
            component(key:"Duke", instance: "Ellington")
        }

        assertEquals("Armstrong", scripted.pico.getComponentInstance("Louis"))
        assertEquals("Ellington", scripted.pico.getComponentInstance("Duke"))
    }

    public void testConstantParameters() {

        def builder = new GroovyNodeBuilder()
        def scripted = builder.container {
            component(key:"cat", class:HasParams, parameters:[ "c", "a", "t" ])
            component(key:"dog", class:HasParams, parameters:[ "d", "o", "g" ])
        }

        def cat = scripted.pico.getComponentInstance("cat");
        def dog = scripted.pico.getComponentInstance("dog");
        assertEquals("cat", cat.getParams());
        assertEquals("dog", dog.getParams());
    }

    public void testComponentParameters() {

        def builder = new GroovyNodeBuilder()
        def scripted = builder.container {
            component(key:"a", class:A)
            component(key:"b", class:B, parameters:[ new ComponentParameter("a") ])
        }

        def a = scripted.pico.getComponentInstance("a");
        def b = scripted.pico.getComponentInstance("b");
        assertSame(a, b.getA())
    }

    protected void startAndDispose(scripted) {
        scripted.pico.start()
        scripted.pico.dispose()
    }

}
