package com.picocontainer.script.groovy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import org.junit.Test;
import com.picocontainer.script.ScriptedContainerBuilder;
import com.picocontainer.script.ScriptedContainerBuilderFactory;

public class GroovyScriptedContainerBuilderFactoryTestCase {

    private static final String TEST_SCRIPT_PATH = "/com/picocontainer/script/groovy/picocontainer.groovy";

    @Test public void testScriptedContainerBuilderFactoryWithUrl() throws ClassNotFoundException {
        URL resource = getClass().getResource(TEST_SCRIPT_PATH);
        assertNotNull("Could not find script resource '+ TEST_SCRIPT_PATH + '.", resource);

        ScriptedContainerBuilderFactory result = new ScriptedContainerBuilderFactory(resource);
        ScriptedContainerBuilder builder = result.getContainerBuilder();
        assertNotNull(builder);
        assertEquals(GroovyContainerBuilder.class.getName(), builder.getClass().getName());
    }

    @Test public void testBuildWithReader() throws ClassNotFoundException {
        Reader script = new StringReader("" +
            "import com.picocontainer.script.testmodel.*\n" +
            "X.reset()\n" +
            "builder = new com.picocontainer.script.groovy.GroovyNodeBuilder()\n" +
            "scripted = builder.container {\n" +
            "    component(A)\n" +
            "}");

        ScriptedContainerBuilderFactory result = new ScriptedContainerBuilderFactory(script,
            GroovyContainerBuilder.class.getName());
        ScriptedContainerBuilder builder = result.getContainerBuilder();
        assertNotNull(builder);
        assertEquals(GroovyContainerBuilder.class.getName(), builder.getClass().getName());
    }

    @Test
    public void testBuildWithFile() throws IOException {
        File resource = File.createTempFile("picocontainer", ".groovy");
        FileWriter writer = new FileWriter(resource);
        writer.write("pico = new com.picocontainer.DefaultPicoContainer()");
        writer.close();
        assertNotNull("Could not find script resource '+ TEST_SCRIPT_PATH + '.", resource);
        ScriptedContainerBuilderFactory result = new ScriptedContainerBuilderFactory(resource);
        ScriptedContainerBuilder builder = result.getContainerBuilder();
        assertNotNull(builder);
        assertEquals(GroovyContainerBuilder.class.getName(), builder.getClass().getName());

    }


}
