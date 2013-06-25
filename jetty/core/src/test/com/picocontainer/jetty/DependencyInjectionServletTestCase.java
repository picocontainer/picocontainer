/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.jetty;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.IO;
import org.junit.After;
import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.containers.EmptyPicoContainer;

public class DependencyInjectionServletTestCase {

    PicoJettyServer server;
    @After public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Thread.sleep(1000);
    }

    @Test public void testCanInstantiateWebContainerContextAndServlet() throws InterruptedException, IOException {

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.addComponent(String.class, "Fred");

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        PicoContext barContext = server.createContext("/bar", false);
        Class<DependencyInjectionTestServlet> servletClass = DependencyInjectionTestServlet.class;
        ServletHolder holder = barContext.addServletWithMapping(servletClass, "/foo");
        holder.setInitParameter("foo", "bar");

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/foo");
        assertEquals("hello Fred bar", IO.toString(url.openStream()));


    }

    @Test public void testCanInstantiateWebContainerContextAndServletInstance() throws InterruptedException, IOException {


        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContext barContext = server.createContext("/bar", false);

        DependencyInjectionTestServlet servlet0 = new DependencyInjectionTestServlet("Fred");
        DependencyInjectionTestServlet servlet1 = (DependencyInjectionTestServlet)
                barContext.addServletWithMapping(servlet0, "/foo");
        servlet1.setFoo("bar");

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/foo");
        assertEquals("hello Fred bar", IO.toString(url.openStream()));


    }



}