package org.picocontainer.jetty;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Test;
import org.mortbay.util.IO;
import org.picocontainer.DefaultPicoContainer;

public class DependencyInjectionFilterTestCase {

    PicoJettyServer server;
    @After public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Thread.sleep(1000);
    }

    @Test public void testCanInstantiateWebContainerContextAndFilter() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.addComponent(String.class, "Fred");
        parentContainer.addComponent(Integer.class, new Integer(5));


        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContext barContext = server.createContext("/bar", false);
        PicoFilterHolder filterHolder = barContext.addFilterWithMapping(DependencyInjectionTestFilter.class, "/*", 0);
        filterHolder.setInitParameter("foo", "bau");
        barContext.addServletWithMapping(DependencyInjectionTestServlet.class, "/foo2");
        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/foo2");

        assertEquals("hello Fred Filtered!(int= 5 bau)", IO.toString(url.openStream()));


    }

    @Test public void testCanInstantiateWebContainerContextAndFilterInstance() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.addComponent(String.class, "Fred");


        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContext barContext = server.createContext("/bar", false);
        DependencyInjectionTestFilter filter = (DependencyInjectionTestFilter) barContext.addFilterWithMapping(new DependencyInjectionTestFilter(
            5), "/*", 0);
        filter.setFoo("bau");
        barContext.addServletWithMapping(DependencyInjectionTestServlet.class, "/foo2");
        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/foo2");

        assertEquals("hello Fred Filtered!(int= 5 bau)", IO.toString(url.openStream()));


    }

    @Test public void testCanInstantiateWebContainerContextAndServlet() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.addComponent(String.class, "Fred");
        parentContainer.addComponent(Integer.class, new Integer(5));

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContext barContext = server.createContext("/bar", false);
        barContext.addFilterWithMapping(DependencyInjectionTestFilter.class, "/*", 0);
        PicoServletHolder holder = barContext.addServletWithMapping(DependencyInjectionTestServlet.class, "/foo2");
        holder.setInitParameter("foo", "bau");
        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/foo2");

        assertEquals("hello Fred Filtered!(int= 5) bau", IO.toString(url.openStream()));

    }


}
