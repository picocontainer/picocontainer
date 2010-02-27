/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.script.LifecycleMode;
import org.picocontainer.script.ScriptedPicoContainerMarkupException;
import org.picocontainer.script.ScriptedContainerBuilder;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.DefaultPicoContainer;

/**
 * {@inheritDoc}
 * The groovy script has to return an instance of {@link org.picocontainer.classname.ClassLoadingPicoContainer}.
 * There is an implicit variable named "parent" that may contain a reference to a parent
 * container. 
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public class GroovyContainerBuilder extends ScriptedContainerBuilder {
    private Class<?> scriptClass;

    public GroovyContainerBuilder(final Reader script, ClassLoader classLoader) {
        this(script,classLoader, LifecycleMode.AUTO_LIFECYCLE);
    }
    
    public GroovyContainerBuilder(final Reader script, ClassLoader classLoader, LifecycleMode lifecycleMode) {
    	super(script,classLoader, lifecycleMode);
    	createGroovyClass();
    }

    public GroovyContainerBuilder(final URL script, ClassLoader classLoader) {
        this(script,classLoader, LifecycleMode.AUTO_LIFECYCLE);
    }

    public GroovyContainerBuilder(final URL script, ClassLoader classLoader, LifecycleMode lifecycleMode) {
        super(script, classLoader, lifecycleMode);
        createGroovyClass();
    }
    
    protected PicoContainer createContainerFromScript(PicoContainer parentContainer, Object assemblyScope) {

        Binding binding = new Binding();
        if ( parentContainer == null ){
            parentContainer = new DefaultClassLoadingPicoContainer(getClassLoader(), new DefaultPicoContainer(new Caching(), new EmptyPicoContainer()));
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
    protected void handleBinding(Binding binding) {
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
            InputStream scriptIs = getScriptInputStream();
            GroovyCodeSource groovyCodeSource = new GroovyCodeSource(scriptIs,"picocontainer.groovy","groovyGeneratedForPicoContainer");
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
    private PicoContainer runGroovyScript(Binding binding){
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

    private Script createGroovyScript(Binding binding) {
        return  InvokerHelper.createScript(scriptClass, binding);
    }
}
