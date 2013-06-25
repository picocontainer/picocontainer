/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import com.picocontainer.script.ScriptedContainerBuilder;
import com.picocontainer.script.ScriptedPicoContainerMarkupException;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.classname.ClassLoadingPicoContainer;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;
import com.picocontainer.containers.EmptyPicoContainer;

/**
 * {@inheritDoc}
 * The groovy script has to return an instance of {@link com.picocontainer.classname.ClassLoadingPicoContainer}.
 * There is an implicit variable named "parent" that may contain a reference to a parent
 * container.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public class GroovyContainerBuilder extends ScriptedContainerBuilder {
    private Class<?> scriptClass;

    public GroovyContainerBuilder(final Reader script, final ClassLoader classLoader) {
    	super(script,classLoader);
    	createGroovyClass();
    }

    public GroovyContainerBuilder(final URL script, final ClassLoader classLoader) {
        super(script, classLoader);
        createGroovyClass();
    }

    @Override
	protected PicoContainer createContainerFromScript(PicoContainer parentContainer, final Object assemblyScope) {

        Binding binding = new Binding();
        if (parentContainer == null) {
            parentContainer = new DefaultClassLoadingPicoContainer(getClassLoader(), new DefaultPicoContainer(new EmptyPicoContainer(), new Caching()));
        }
        binding.setVariable("parent", parentContainer);
        binding.setVariable("builder", createNodeBuilder());
        binding.setVariable("assemblyScope", assemblyScope);
        handleBinding(binding);
        return runGroovyScript(binding);
    }

    /**
     * Allows customization of the groovy node builder in descendants.
     * @return GroovyNodeBuilder
     */
    protected GroovyObject createNodeBuilder() {
        return new GroovyNodeBuilder();
    }

    /**
     * This allows children of this class to add to the default binding.
     * Might want to add similar or a more generic implementation of this
     * method to support the other scripting languages.
     * @param binding the binding
     */
    protected void handleBinding(final Binding binding) {
        // does nothing but adds flexibility for children
    }


    /**
     * Parses the groovy script into a class.  We store the Class instead
     * of the script proper so that it doesn't invoke race conditions on
     * multiple executions of the script.
     */
    private void createGroovyClass() {
        try {
            GroovyClassLoader loader = new GroovyClassLoader(getClassLoader());
            GroovyCodeSource groovyCodeSource = new GroovyCodeSource(getScriptReader(),
            		"picocontainer.groovy",
            		"groovyGeneratedForPicoContainer");
            scriptClass = loader.parseClass(groovyCodeSource);
        } catch (CompilationFailedException e) {
            throw new GroovyCompilationException("Compilation Failed '" + e.getMessage() + "'", e);
        } catch (IOException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        }

    }

    /**
     * Executes the groovy script with the given binding.
     * @param binding Binding
     * @return PicoContainer
     */
    private PicoContainer runGroovyScript(final Binding binding) {
        Script script = createGroovyScript(binding);

        Object result = script.run();
        Object picoVariable;
        try {
            picoVariable = binding.getVariable("pico");
        } catch (MissingPropertyException e) {
            picoVariable = result;
        }
        if (picoVariable == null) {
            throw new NullPointerException("Groovy Script Variable: pico");
        }

        if (picoVariable instanceof PicoContainer) {
            return (PicoContainer) picoVariable;
        } else if (picoVariable instanceof ClassLoadingPicoContainer) {
            return ((ClassLoadingPicoContainer) picoVariable);
        } else {
            throw new ScriptedPicoContainerMarkupException("Bad type for pico:" + picoVariable.getClass().getName());
        }

    }

    private Script createGroovyScript(final Binding binding) {
        return  InvokerHelper.createScript(scriptClass, binding);
    }
}
