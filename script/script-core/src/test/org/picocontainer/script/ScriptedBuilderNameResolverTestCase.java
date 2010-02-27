package org.picocontainer.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.picocontainer.script.ScriptedBuilderNameResolver.DEFAULT_XML_BUILDER;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * @author Michael Rimov
 * @author Mauro Talevi
 */
public class ScriptedBuilderNameResolverTestCase {
    private ScriptedBuilderNameResolver scriptBuilderResolver = new ScriptedBuilderNameResolver();

    @Test
    public void testGetAllSupportedExtensions() {
        Set<String> allExtensions = new TreeSet<String>();

        allExtensions.add(ScriptedBuilderNameResolver.XML);

        String[] actualReturn = scriptBuilderResolver.getAllSupportedExtensions();
        assertNotNull(actualReturn);

        List<String> returnAsList = Arrays.asList(actualReturn);
        boolean someMerged = allExtensions.removeAll(returnAsList);
        assertTrue(someMerged);
        assertTrue(allExtensions.size() == 0);
    }

    @Test
    public void testGetBuilderClassNameForFile() {
        File compositionFile = new File("test.xml");
        assertEquals("return value", ScriptedBuilderNameResolver.DEFAULT_XML_BUILDER, scriptBuilderResolver.getBuilderClassName(compositionFile));
    }

    @Test
    public void testGetBuilderClassNameForResource() {
        final String resourceName = "/org/picocontainer/script/xml/picocontainer.xml";
        URL compositionURL = this.getClass().getResource(resourceName);
        assertEquals("return value", DEFAULT_XML_BUILDER, scriptBuilderResolver.getBuilderClassName(compositionURL));
    }

    @Test
    public void canGetBuilderClassNameForExtension() throws UnsupportedScriptTypeException {
        assertEquals("return value", DEFAULT_XML_BUILDER, scriptBuilderResolver.getBuilderClassName(".xml"));
    }

    @Test(expected = UnsupportedScriptTypeException.class)
    public void cannotGetBuilderClassNameForUnknownExtension() {
        scriptBuilderResolver.getBuilderClassName(".foo");
    }

    @Test
    public void canRegisterBuilder() {
        scriptBuilderResolver.registerBuilder(".foo", "org.example.FooBar");
        assertEquals("org.example.FooBar", scriptBuilderResolver.getBuilderClassName(".foo"));
    }

    @Test(expected = UnsupportedScriptTypeException.class)
    public void cannotGetBuilderClassNameAfterReset() {
        scriptBuilderResolver.registerBuilder(".foo", "org.example.FooBar");
        scriptBuilderResolver.resetBuilders();
        scriptBuilderResolver.getBuilderClassName(".foo");
    }

}
