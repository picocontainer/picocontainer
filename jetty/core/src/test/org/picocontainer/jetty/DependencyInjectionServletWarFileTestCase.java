package org.picocontainer.jetty;

import org.junit.After;
import org.junit.Test;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.util.IO;
import org.picocontainer.DefaultPicoContainer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DependencyInjectionServletWarFileTestCase {

    PicoJettyServer server;
    @After public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Thread.sleep(1000);
    }

    @Test public void testCanInstantiateWebContainerContextAndServlet()
            throws InterruptedException, IOException {

        File testWar = TestHelper.getTestWarFile();

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.addComponent(String.class, "Fred");
        StringBuffer sb = new StringBuffer();
        parentContainer.addComponent(StringBuffer.class, sb);
        parentContainer.addComponent(Integer.class, new Integer(5));

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        WebAppContext wac = server.addWebApplication("/bar", testWar.getAbsolutePath().replace('\\','/'));
        assertNotNull(wac);

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/foo");
        assertEquals("hello Fred bar", IO.toString(url.openStream()));

        assertEquals("-contextInitialized", sb.toString());

    }

    @Test public void testCanHostJspPage()
            throws InterruptedException, IOException {

        File testWar = TestHelper.getTestWarFile();

        final DefaultPicoContainer parentContainer = new DefaultPicoContainer();
        parentContainer.addComponent(String.class, "Fred");
        parentContainer.addComponent(StringBuffer.class, new StringBuffer());
        parentContainer.addComponent(Integer.class, new Integer(5));

        server = new PicoJettyServer("localhost", 8080, parentContainer);
        WebAppContext wac = server.addWebApplication("/bar", testWar.getAbsolutePath().replace('\\','/'));
        assertNotNull(wac);

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/test.jsp");
        assertEquals("<HTML>\n" +
                "  <HEAD>\n" +
                "    <TITLE>Test JSP</TITLE>\n" +
                "  </HEAD>\n" +
                "  <BODY>\n" +
                "    hello\n" +
                "  </BODY>\n" +
                "</HTML>", IO.toString(url.openStream()));


    }


}
