/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * ScriptedBuilderNameResolver handles the task of resolving a file name to a builder
 * name. Typical default resolution is for Groovy, BeanShell, JavaScript,
 * Jython, and XML script names. However, you can register/replace your own
 * builder implementations by using the registerBuilder() function.
 * 
 * @author Michael Rimov
 */
public class ScriptedBuilderNameResolver {

    public static final String GROOVY = ".groovy";
    public static final String BEANSHELL = ".bsh";
    public static final String JAVASCRIPT = ".js";
    public static final String JYTHON = ".py";
    public static final String XML = ".xml";
    public static final String RUBY = ".rb";
    
    public static final String DEFAULT_GROOVY_BUILDER = "org.picocontainer.script.groovy.GroovyContainerBuilder";
    public static final String DEFAULT_BEANSHELL_BUILDER = "org.picocontainer.script.bsh.BeanShellContainerBuilder";
    public static final String DEFAULT_JAVASCRIPT_BUILDER = "org.picocontainer.script.rhino.JavascriptContainerBuilder";
    public static final String DEFAULT_XML_BUILDER = "org.picocontainer.script.xml.XMLContainerBuilder";
    public static final String DEFAULT_JYTHON_BUILDER = "org.picocontainer.script.jython.JythonContainerBuilder";
    public static final String DEFAULT_RUBY_BUILDER = "org.picocontainer.script.jruby.JRubyContainerBuilder";
    
    private final Map<String, String> extensionToBuilders = new HashMap<String, String>();

    public ScriptedBuilderNameResolver() {
        resetBuilders();
    }

    /**
     * Returns the classname of the ScriptedContainerBuilder from the file.
     * 
     * @param compositionFile the composition File
     * @return The builder class name
     */
    public String getBuilderClassName(File compositionFile) {
        String language = getExtension(compositionFile.getAbsolutePath());
        return getBuilderClassName(language);
    }

    /**
     * Returns the classname of the ScriptedContainerBuilder from the URL.
     * 
     * @param compositionURL the composition URL
     * @return The builder class name
     */
    public String getBuilderClassName(URL compositionURL) {
        String language = getExtension(compositionURL.getFile());
        return getBuilderClassName(language);
    }

    /**
     * Retrieve the classname of the builder to use given the provided
     * extension.  Example: 
     * <pre>
     * ScriptedContainerBuilderFactory factory = new ScriptedContainerBuilderFactory(.....);
     * String groovyBuilderName = factory.getBuilderClassName(&quot;.groovy&quot;);
     * assert &quot;org.picocontainer.script.groovy.GroovyContainerBuilder&quot;.equals(groovyBuilderName);
     * </pre>
     * 
     * @param extension the extension 
     * @return The builder class name
     * @throws UnsupportedScriptTypeException
     */
    public synchronized String getBuilderClassName(final String extension) throws UnsupportedScriptTypeException {
        String resultingBuilderClassName = extensionToBuilders.get(extension);
        if (resultingBuilderClassName == null) {
            throw new UnsupportedScriptTypeException(extension, this.getAllSupportedExtensions());
        }
        return resultingBuilderClassName;
    }

    /**
     * Function to allow the resetting of the builder map to defaults. Allows
     * testing of the static resource a bit better.
     */
    public synchronized void resetBuilders() {
        extensionToBuilders.clear();

        // This is a bit clunky compared to just registering the items
        // directly into the map, but this way IMO it provides a single access
        // point into the extensionToBuilders map.
        registerBuilder(GROOVY, DEFAULT_GROOVY_BUILDER);
        registerBuilder(BEANSHELL, DEFAULT_BEANSHELL_BUILDER);
        registerBuilder(JAVASCRIPT, DEFAULT_JAVASCRIPT_BUILDER);
        registerBuilder(XML, DEFAULT_XML_BUILDER);
        registerBuilder(JYTHON, DEFAULT_JYTHON_BUILDER);
        registerBuilder(RUBY, DEFAULT_RUBY_BUILDER);
    }

    /**
     * Registers/replaces a new handler for a given extension. Allows for
     * customizable behavior in the various builders or the possibility to
     * dynamically add handlers for new file types. Example: 
     * <pre>
     * ScriptedContainerBuilderFactory factory = new ScriptedContainerBuilderFactory(...)
     * factory.registerBuilder(&quot;.groovy&quot;, &quot;org.picocontainer.script.groovy.GroovyContainerBuilder&quot;);
     * ScriptedContainerBuilder builder = factory.getContainerBuilder();
     * assertNotNull(builder);
     * </pre>
     * <p>
     * The internal code now requires synchronization of the builder extension
     * map since who knows what is using it when a new builder is registered.
     * </p>
     * 
     * @param extension String the extension to register under.
     * @param className String the classname to use for the given extension.
     */
    public synchronized void registerBuilder(final String extension, final String className) {
        extensionToBuilders.put(extension, className);
    }

    /**
     * Returns a list of all supported extensions.
     * 
     * @return A String[] of extensions including the period in the name.
     */
    public synchronized String[] getAllSupportedExtensions() {
        return extensionToBuilders.keySet().toArray(new String[extensionToBuilders.size()]);
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

}
