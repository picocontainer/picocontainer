/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.classname.ClassName;

/**
 * Factory class for scripted container builders of various scripting languages.
 * When using the constructors taking a File, the extensions must be one of the
 * following:
 * <ul>
 * <li>.groovy - Groovy scripts</li>
 * <li>.bsh - BeanShell scripts</li>
 * <li>.js - Rhino scripts (Javascript)</li>
 * <li>.py - Python scripts </li>
 * <li>.xml - XML scripts</li>
 * </ul>
 * with the content of the file of the corresponding scripting language.
 * 
 * @author Paul Hammant
 * @author Aslak Helles&oslah;y
 * @author Obie Fernandez
 * @author Michael Rimov
 * @author Mauro Talevi
 */
public class ScriptedContainerBuilderFactory {

    private ScriptedContainerBuilder containerBuilder;

    /**
     * Creates a ScriptedContainerBuilderFactory
     * 
     * @param compositionFile File The script file.
     * @param classLoader ClassLoader for class resolution once we resolve what
     *            the name of the builder should be.
     * @param scriptedBuilderResolver ScriptedBuilderNameResolver the resolver of
     *            container builder class names from file names.
     * @throws UnsupportedScriptTypeException if the extension of the file does
     *             not match that of any known script.
     * @throws FileNotFoundException if composition file is not found
     */
    public ScriptedContainerBuilderFactory(File compositionFile, ClassLoader classLoader,
            ScriptedBuilderNameResolver scriptedBuilderResolver) throws UnsupportedScriptTypeException, FileNotFoundException {
        this(new FileReader(fileExists(compositionFile)), scriptedBuilderResolver.getBuilderClassName(compositionFile),
                classLoader);
    }

    /**
     * Creates a ScriptedContainerBuilderFactory with default script builder
     * resolver
     * 
     * @param compositionFile File The script file.
     * @param classLoader ClassLoader for class resolution once we resolve what
     *            the name of the builder should be.
     * @see ScriptedContainerBuilderFactory#ScriptedContainerBuilderFactory(File,
     *      ClassLoader, ScriptedBuilderNameResolver)
     */
    public ScriptedContainerBuilderFactory(File compositionFile, ClassLoader classLoader) throws IOException {
        this(compositionFile, classLoader, new ScriptedBuilderNameResolver());
    }

    /**
     * Creates a ScriptedContainerBuilderFactory with default script builder
     * resolver and context class loader
     * 
     * @param compositionFile File The script file.
     * @see ScriptedContainerBuilderFactory#ScriptedContainerBuilderFactory(File,
     *      ClassLoader, ScriptedBuilderNameResolver)
     */
    public ScriptedContainerBuilderFactory(File compositionFile) throws IOException {
        this(compositionFile, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a ScriptedContainerBuilderFactory with default script builder
     * resolver and context class loader
     * 
     * @param compositionURL The script URL.
     * @throws UnsupportedScriptTypeException if the extension of the file does
     *             not match that of any known script.
     */
    public ScriptedContainerBuilderFactory(URL compositionURL) {
        this(compositionURL, Thread.currentThread().getContextClassLoader(), new ScriptedBuilderNameResolver());
    }

    /**
     * Creates a ScriptedContainerBuilderFactory
     * 
     * @param compositionURL The script URL.
     * @param builderClassResolver ScriptedBuilderNameResolver the resolver for
     *            figuring out file names to container builder class names.
     * @param classLoader ClassLoader for class resolution once we resolve what
     *            the name of the builder should be.. the specified builder
     *            using the specified classloader.
     * @throws UnsupportedScriptTypeException if the extension of the file does
     *             not match that of any known script.
     */
    public ScriptedContainerBuilderFactory(URL compositionURL, ClassLoader classLoader,
            ScriptedBuilderNameResolver builderClassResolver) throws UnsupportedScriptTypeException {
        this(compositionURL, builderClassResolver.getBuilderClassName(compositionURL), classLoader);
    }

    /**
     * Creates a ScriptedContainerBuilderFactory
     * 
     * @param compositionURL The script URL.
     * @param builderClassName the class name of the ContainerBuilder to
     *            instantiate
     * @param classLoader ClassLoader for class resolution once we resolve what
     *            the name of the builder should be.. the specified builder
     *            using the specified classloader.
     */
    public ScriptedContainerBuilderFactory(URL compositionURL, String builderClassName, ClassLoader classLoader) {
        createContainerBuilder(compositionURL, builderClassName, classLoader);
    }

    /**
     * Creates a ScriptedContainerBuilderFactory with context class loader
     * 
     * @param composition the Reader encoding the script to create the builder
     *            with
     * @param builderClassName the class name of the ContainerBuilder to
     *            instantiate
     * @see ScriptedContainerBuilderFactory#ScriptedContainerBuilderFactory(Reader,
     *      String, ClassLoader)
     */
    public ScriptedContainerBuilderFactory(Reader composition, String builderClassName) {
        this(composition, builderClassName, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a ScriptedContainerBuilderFactory
     * 
     * @param composition the Reader encoding the script to create the builder
     *            with
     * @param builderClassName the class name of the ContainerBuilder to
     *            instantiate
     * @param classLoader the Classloader to use for instantiation
     */
    public ScriptedContainerBuilderFactory(Reader composition, String builderClassName, ClassLoader classLoader) {
        createContainerBuilder(composition, builderClassName, classLoader);
    }

    /**
     * Performs the actual instantiation of the builder.
     * 
     * @param composition the composition source object - can be either a
     *            Reader, a URL or a File
     * @param builderClassName the class name of the ContainerBuilder to
     *            instantiate
     * @param classLoader the Classloader to use for instantiation
     */
    private void createContainerBuilder(Object composition, String builderClassName, ClassLoader classLoader) {
        DefaultClassLoadingPicoContainer defaultScriptedContainer;
        {
            // transient.
            DefaultPicoContainer factory = new DefaultPicoContainer();
            if (composition == null) {
                throw new NullPointerException("composition can't be null");
            }
            factory.addComponent(composition);

            if (classLoader == null) {
                // Thread.currentThread().getContextClassLoader() MAY return
                // null, while Class.getClassLoader() should NEVER return null.
                // -MR
                classLoader = getClass().getClassLoader();
            }
            factory.addComponent(classLoader);

            // If we don't specify the classloader here, some of the things that
            // make up a scripted container may bomb. And we're only talking a
            // reload within a webapp! -MR
            defaultScriptedContainer = new DefaultClassLoadingPicoContainer(classLoader, factory);
        }
        ClassName className = new ClassName(builderClassName);
        MutablePicoContainer mutablePicoContainer = defaultScriptedContainer.addComponent(className, className);
        ComponentAdapter<?> componentAdapter = mutablePicoContainer.getComponentAdapter(className);
        containerBuilder = (ScriptedContainerBuilder) componentAdapter.getComponentInstance(defaultScriptedContainer, ComponentAdapter.NOTHING.class);
    }

    private static File fileExists(final File file) throws FileNotFoundException {
        if (file.exists()) {
            return file;
        } 
        throw new FileNotFoundException("File " + file.getAbsolutePath() + " does not exist.");
    }

    /**
     * Returns the created container builder instance.
     * 
     * @return The ScriptedContainerBuilder instance
     */
    public ScriptedContainerBuilder getContainerBuilder() {
        return containerBuilder;
    }

}
