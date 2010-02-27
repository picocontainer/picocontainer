package org.picocontainer.jetty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Test;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.util.IO;
import org.picocontainer.containers.EmptyPicoContainer;

public class JspTestCase {

    PicoJettyServer server;

    @After public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        Thread.sleep(1000);
    }


    @Test public void testCanInstantiateWebContainerContextAndSimpleJspPage() throws InterruptedException, IOException {

        File warFile = TestHelper.getTestWarFile();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());


        //server.addRequestLog(new NCSARequestLog("./logs/jetty-yyyy-mm-dd.log"));


        PicoContext barContext = server.createContext("/bar", true);
        String absolutePath = warFile.getParentFile().getAbsolutePath();
        String scratchDir = warFile.getParentFile().getParentFile().getParentFile().getAbsolutePath() + File.separator + "target" + File.separator + "work";
        new File(scratchDir).mkdirs();
        barContext.setDefaultHandling(absolutePath + "/", scratchDir, "*.jsp");

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

        Thread.sleep(1000);


    }

    @Test public void testCanInstantiateWebContainerContextAndMissingJspPageHandled() throws InterruptedException, IOException {

        File warFile = TestHelper.getTestWarFile();

        server = new PicoJettyServer("localhost", 8080, new EmptyPicoContainer());

        PicoContext barContext = server.createContext("/bar", true);
        barContext.addErrorHandler(new MyErrorHandler());
        String absolutePath = warFile.getParentFile().getAbsolutePath();
        String scratchDir = warFile.getParentFile().getParentFile().getParentFile().getAbsolutePath() + File.separator + "target" + File.separator + "work";
        new File(scratchDir).mkdirs();
        barContext.setDefaultHandling(absolutePath + "/", scratchDir, "*.jsp");

        server.start();

        Thread.sleep(2 * 1000);


        Socket socket = new Socket("localhost", 8080);
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        writer.write("GET /bar/barfs.jsp HTTP/1.0\n\n\n");
        writer.flush();
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
        String line = lnr.readLine();
        String result = "";
        while(line != null) {
            result = result + line + "\n";
            line = lnr.readLine();
        }

        assertTrue(result.indexOf("Banzai") != -1);
        assertTrue(result.indexOf("HTTP/1.1 500") != -1);

        Thread.sleep(1000);


    }
    private static class MyErrorHandler extends ErrorHandler {
        protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message)
            throws IOException
        {
            writer.write("<br>Banzai!<br><br>");
            super.handleErrorPage(request, writer, code, message);
        }

    }

}
