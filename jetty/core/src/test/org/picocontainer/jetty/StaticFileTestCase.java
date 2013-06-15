package org.picocontainer.jetty;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.eclipse.jetty.util.IO;
import org.junit.After;
import org.junit.Test;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.testhelper.PlatformAssert;

public class StaticFileTestCase {

    PicoJettyServer server;
    @After public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test public void testStaticFile() throws InterruptedException, IOException {

        File warFile = TestHelper.getTestWarFile();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContext barContext = server.createContext("/bar", false);
        barContext.setStaticContext(warFile.getParentFile().getAbsolutePath());

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/hello.html");
        PlatformAssert.assertSameExceptCarriageReturns("<html>\n" +
                " <body>\n" +
                "   hello\n" +
                " </body>\n" +
                "</html>", IO.toString(url.openStream()));

        Thread.sleep(1000);

    }

   @Test public void testDifferentWelcomePage() throws InterruptedException, IOException {

        File warFile = TestHelper.getTestWarFile();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContext barContext = server.createContext("/bar", false);
        barContext.setStaticContext(warFile.getParentFile().getAbsolutePath(), "hello.html");

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/");
        PlatformAssert.assertSameExceptCarriageReturns("<html>\n" +
                " <body>\n" +
                "   hello\n" +
                " </body>\n" +
                "</html>", IO.toString(url.openStream()));

        Thread.sleep(1000);

    }

    @Test public void testMissingPage() throws InterruptedException, IOException {

        File warFile = TestHelper.getTestWarFile();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());
        PicoContext barContext = server.createContext("/bar", false);
        barContext.setStaticContext(warFile.getParentFile().getAbsolutePath());

        server.start();

        Thread.sleep(2 * 1000);

        URL url = new URL("http://localhost:8080/bar/HearMeRoar!");
        try {
            url.openStream();
            fail("should have barfed");
        } catch (FileNotFoundException e) {
            // expected
        }

        Thread.sleep(1000);

    }

}
