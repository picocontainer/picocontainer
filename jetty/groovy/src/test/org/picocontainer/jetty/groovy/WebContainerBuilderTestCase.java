/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.jetty.groovy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URL;

import org.junit.After;
import org.junit.Test;
import org.mortbay.util.IO;
import org.picocontainer.jetty.groovy.TestHelper;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.ObjectReference;
import org.picocontainer.PicoContainer;
import org.picocontainer.script.groovy.GroovyContainerBuilder;
import org.picocontainer.references.SimpleReference;

public final class WebContainerBuilderTestCase {

//    private final ObjectReference containerRef = new SimpleReference();

    private MutablePicoContainer pico;

    @After public void tearDown() throws Exception {
        if (pico != null) {
            pico.stop();
        }
        //Thread.sleep(2 * 1000);
    }

    @Test public void testCanComposeWebContainerContextAndFilter() throws InterruptedException, IOException {
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "builder = new GroovyNodeBuilder()\n" +
                "builder.setNode(new "+WebContainerBuilder.class.getName()+"())\n" +
                "nano = builder.container {\n" +
                "    component(instance:'Fred')\n" +
                "    component(instance:new Integer(5))\n" +
                // declare the web container
                "    webContainer(port:8080) {\n" +
                "        context(path:'/bar') {\n" +
                "            initParam(name:'a', value:'b')\n" +
                "            filter(path:'/*', class:org.picocontainer.jetty.groovy.DependencyInjectionTestFilter," +
                "                   dispatchers: new Integer(0)) {\n" +
                "               initParam(name:'foo', value:'bau')\n" +
                "            }\n" +
                "            servlet(path:'/foo2', class:org.picocontainer.jetty.groovy.DependencyInjectionTestServlet)\n" +

                "        }\n" +
                "    }\n" +
                // end declaration
                "}\n");

        assertPageIsHostedWithContents(script, "hello Fred Filtered!(int= 5 bau)<b>", "http://localhost:8080/bar/foo2");
    }

    @Test public void testCanComposeWebContainerContextAndServlet() throws InterruptedException, IOException {
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "builder = new GroovyNodeBuilder()\n" +
                "builder.setNode(new "+WebContainerBuilder.class.getName()+"())\n" +
                "nano = builder.container {\n" +
                "    component(instance:'Fred')\n" +
                // declare the web container
                "    webContainer(port:8080) {\n" +
                "        context(path:'/bar') {\n" +
                "            servlet(path:'/foo', class:org.picocontainer.jetty.groovy.DependencyInjectionTestServlet) {\n" +
                "               initParam(name:'foo', value:'bar')\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                // end declaration
                "}\n");

        assertPageIsHostedWithContents(script, "hello Fred bar<null>", "http://localhost:8080/bar/foo");
    }

    @Test public void testCanComposeWebContainerContextAndServletInstance() throws InterruptedException, IOException {
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "builder = new GroovyNodeBuilder()\n" +
                "builder.setNode(new "+WebContainerBuilder.class.getName()+"())\n" +
                "nano = builder.container {\n" +
                // declare the web container
                "    webContainer(port:8080) {\n" +
                "        context(path:'/bar') {\n" +
                "            servlet(path:'/foo', instance:new org.picocontainer.jetty.groovy.DependencyInjectionTestServlet('Fred')) {\n" +
                //"                setFoo(foo:'bar')\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                // end declaration
                "}\n");

        assertPageIsHostedWithContents(script, "hello Fred<null>", "http://localhost:8080/bar/foo");
    }

    
    @Test public void testCanComposeWebContainerContextWithExplicitConnector() throws InterruptedException, IOException {
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "builder = new GroovyNodeBuilder()\n" +
                "builder.setNode(new "+WebContainerBuilder.class.getName()+"())\n" +
                "nano = builder.container {\n" +
                "    component(instance:'Fred')\n" +
                // declare the web container
                "    webContainer() {\n" +
                "        blockingChannelConnector(host:'localhost', port:8080)\n" +
                "        context(path:'/bar') {\n" +
                "            servlet(path:'/foo', class:org.picocontainer.jetty.groovy.DependencyInjectionTestServlet)\n" +
                "        }\n" +
                "    }\n" +
                // end declaration
                "}\n");

        assertPageIsHostedWithContents(script, "hello Fred<null>", "http://localhost:8080/bar/foo");
    }

    @Test public void testCanComposeWebContainerAndWarFile() throws InterruptedException, IOException {

        File testWar = TestHelper.getTestWarFile();

        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "builder = new GroovyNodeBuilder()\n" +
                "builder.setNode(new "+WebContainerBuilder.class.getName()+"())\n" +
                "nano = builder.container {\n" +
                "    component(instance:'Fred')\n" +
                "    component(instance:new Integer(5))\n" +
                "    component(key:StringBuffer.class, instance:new StringBuffer())\n" +
                // declare the web container
                "    webContainer() {\n" +
                "        blockingChannelConnector(host:'localhost', port:8080)\n" +
                "        xmlWebApplication(path:'/bar', warfile:'"+testWar.getAbsolutePath().replace('\\','/')+"')" +
                "    }\n" +
                // end declaration
                "}\n");

        assertPageIsHostedWithContents(script, "hello Fred bar", "http://localhost:8080/bar/foo");
        assertEquals("-contextInitialized", pico.getComponent(StringBuffer.class).toString());
    }

    @Test public void testCanComposeWebContainerContextAndListener() throws InterruptedException, IOException {

        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "builder = new GroovyNodeBuilder()\n" +
                "builder.setNode(new "+WebContainerBuilder.class.getName()+"())\n" +
                "nano = builder.container {\n" +
                "    component(class:StringBuffer.class)\n" +
                // declare the web container
                "    webContainer(port:8080) {\n" +
                "        context(path:'/bar') {\n" +
                "            listener(class:org.picocontainer.jetty.groovy.DependencyInjectionTestListener)\n" +
                "        }\n" +
                "    }\n" +
                // end declaration
                "}\n");

        assertPageIsHostedWithContents(script, "", "http://localhost:8080/bar/");

        StringBuffer stringBuffer = pico.getComponent(StringBuffer.class);

        assertEquals("-contextInitialized", stringBuffer.toString());

        pico.stop();
        pico = null;

        assertEquals("-contextInitialized-contextDestroyed", stringBuffer.toString());

    }

    @Test public void testStaticContentCanBeServed() throws InterruptedException, IOException {

        File testWar = TestHelper.getTestWarFile();

		String absolutePath = testWar.getParentFile().getAbsolutePath();
		absolutePath = absolutePath.replace('\\', '/');
        
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "builder = new GroovyNodeBuilder()\n" +
                "builder.setNode(new "+WebContainerBuilder.class.getName()+"())\n" +
                "nano = builder.container {\n" +
                // declare the web container
                "    webContainer(port:8080) {\n" +
                "        context(path:'/bar') {\n" +
                "            staticContent(path:'"+absolutePath+"')\n" +
                "        }\n" +
                "    }\n" +
                // end declaration
                "}\n");

        assertPageIsHostedWithContents(script, "<html>\n" +
                " <body>\n" +
                "   hello\n" +
                " </body>\n" +
                "</html>", "http://localhost:8080/bar/hello.html");

        //Thread.sleep(1 * 1000);

        pico.stop();
        pico = null;


    }

    @Test public void testStaticContentCanBeServedWithDefaultWelcomePage() throws InterruptedException, IOException {

        File testWar = TestHelper.getTestWarFile();

        String absolutePath = testWar.getParentFile().getAbsolutePath();
        absolutePath = absolutePath.replace('\\', '/');
        
        Reader script = new StringReader("" +
                "package org.picocontainer.script.groovy\n" +
                "builder = new GroovyNodeBuilder()\n" +
                "builder.setNode(new "+WebContainerBuilder.class.getName()+"())\n" +
                "nano = builder.container {\n" +
                // declare the web container
                "    webContainer(port:8080) {\n" +
                "        context(path:'/bar') {\n" +
                "            staticContent(path:'" + absolutePath + "', welcomePage:'hello.html')\n" +
                "        }\n" +
                "    }\n" +
                // end declaration
                "}\n");

        assertPageIsHostedWithContents(script, "<html>\n" +
                " <body>\n" +
                "   hello\n" +
                " </body>\n" +
                "</html>", "http://localhost:8080/bar/");

        //Thread.sleep(1 * 1000);

        pico.stop();
        pico = null;

    }

    private void assertPageIsHostedWithContents(Reader script, String message, String url) throws InterruptedException, IOException {
        pico = (MutablePicoContainer) buildContainer(script, null, "SOME_SCOPE");
        assertNotNull(pico);
        
        //Thread.sleep(2 * 1000);
        String actual;
        try {
            actual = IO.toString(new URL(url).openStream());
        } catch (ConnectException e) {
            Thread.sleep(1000);
            actual = IO.toString(new URL(url).openStream());
        } catch (FileNotFoundException e) {
            actual = "";
        }
        assertEquals(message, actual);
    }

    private PicoContainer buildContainer(Reader script, PicoContainer parent, Object scope) {
        return new GroovyContainerBuilder(script, getClass().getClassLoader()).buildContainer(parent, scope, true);
    }
}
