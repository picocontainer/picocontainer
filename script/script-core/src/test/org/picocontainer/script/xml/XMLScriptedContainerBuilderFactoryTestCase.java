package org.picocontainer.script.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import org.junit.Test;
import org.picocontainer.script.ScriptedContainerBuilder;
import org.picocontainer.script.ScriptedContainerBuilderFactory;

public class XMLScriptedContainerBuilderFactoryTestCase {

    private static final String TEST_SCRIPT_PATH = "/org/picocontainer/script/xml/picocontainer.xml";

    @Test public void testScriptedContainerBuilderFactoryWithUrl() throws ClassNotFoundException {
        URL resource = getClass().getResource(TEST_SCRIPT_PATH);
        assertNotNull("Could not find script resource '+ TEST_SCRIPT_PATH + '.", resource);

        ScriptedContainerBuilderFactory result = new ScriptedContainerBuilderFactory(resource);
        ScriptedContainerBuilder builder = result.getContainerBuilder();
        assertNotNull(builder);
        assertEquals(XMLContainerBuilder.class.getName(), builder.getClass().getName());
    }

    @Test public void testBuildWithReader() throws ClassNotFoundException {
        Reader script = new StringReader("" +
            "<?xml version='1.0'?>"+
            "<container>"+
            " <container> " +
            "  <component class='java.util.ArrayList' />"+
            " </container> "+
            "</container>" +
            "");

        ScriptedContainerBuilderFactory result = new ScriptedContainerBuilderFactory(script,
                XMLContainerBuilder.class.getName());
        ScriptedContainerBuilder builder = result.getContainerBuilder();
        assertNotNull(builder);
        assertEquals(XMLContainerBuilder.class.getName(), builder.getClass().getName());
    }

    @Test
    public void testBuildWithFile() throws IOException {
        File resource = File.createTempFile("picocontainer", ".xml");
        FileWriter writer = new FileWriter(resource);
        writer.write("<?xml version='1.0'?>\n"+
                     "<container/>");
        writer.close();
        assertNotNull("Could not find script resource '+ TEST_SCRIPT_PATH + '.", resource);

        ScriptedContainerBuilderFactory result = new ScriptedContainerBuilderFactory(resource);
        ScriptedContainerBuilder builder = result.getContainerBuilder();
        assertNotNull(builder);
        assertEquals(XMLContainerBuilder.class.getName(), builder.getClass().getName());

    }


}
