package org.picocontainer.script.groovy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.util.ArrayList;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.script.groovy.GroovyContainerBuilder;
import org.picocontainer.script.groovy.GroovyScriptGenerator;

/**
 * @author Aslak Helles&oslash;y
 */
public class GroovyScriptGeneratorTestCase {
    @Test public void testShouldWriteAGroovyScriptThatAllowsToRecreateASimilarContainer() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(ArrayList.class);
        pico.addComponent("Hello", "World");

        GroovyScriptGenerator groovyScriptGenerator = new GroovyScriptGenerator();
        String script = groovyScriptGenerator.generateScript(pico);

        GroovyContainerBuilder groovyContainerBuilder = new GroovyContainerBuilder(new StringReader(script), getClass().getClassLoader());
        PicoContainer newPico = groovyContainerBuilder.createContainerFromScript(null, null);

        assertNotNull(newPico.getComponent(ArrayList.class));
        assertEquals("World", newPico.getComponent("Hello"));
    }
}