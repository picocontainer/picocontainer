package org.picocontainer.jetty;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.containers.EmptyPicoContainer;

public class DependencyInjectionListenerTestCase {

    PicoJettyServer server;
    @After public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Thread.sleep(1000);
    }

    @Test public void testCanInstantiateWebContainerContextAndListener() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        StringBuffer sb = new StringBuffer();
        parentContainer.addComponent(StringBuffer.class, sb);

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContext barContext = server.createContext("/bar", false);
        Class listenerClass = DependencyInjectionTestListener.class;
        barContext.addListener(listenerClass);

        server.start();

        assertEquals("-contextInitialized", sb.toString());

        server.stop();

        assertEquals("-contextInitialized-contextDestroyed", sb.toString());

    }

    @Test public void testListenerInvokedBeforeFilterBeforeServlet() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        StringBuffer sb = new StringBuffer();
        parentContainer.addComponent(StringBuffer.class, sb);

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContext barContext = server.createContext("/bar", false);
        Class listenerClass = DependencyInjectionTestListener.class;
        barContext.addListener(listenerClass);
        barContext.addServletWithMapping(DependencyInjectionTestServlet2.class, "/foo");
        barContext.addFilterWithMapping(DependencyInjectionTestFilter2.class, "/foo", 0);

        server.start();

        URL url = new URL("http://localhost:8080/bar/foo");
        url.openStream();

        assertEquals("-contextInitialized-Filter-Servlet", sb.toString());

        server.stop();

        assertEquals("-contextInitialized-Filter-Servlet-contextDestroyed", sb.toString());

    }



    @Test public void testCanInstantiateWebContainerContextAndListenerInstance() throws InterruptedException, IOException {

        StringBuffer sb = new StringBuffer();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContext barContext = server.createContext("/bar", false);
        barContext.addListener(new DependencyInjectionTestListener(sb));

        server.start();

        assertEquals("-contextInitialized", sb.toString());

        server.stop();

        assertEquals("-contextInitialized-contextDestroyed", sb.toString());

    }





}
