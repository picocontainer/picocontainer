/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file. 
 ******************************************************************************/
package org.picocontainer.web.chain;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.servlet.ServletContext;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.classname.ClassName;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.script.ContainerBuilder;

/**
 * <p>
 * ServletChainBuilder builds ContainerChains from servlet path and caches
 * container recorders for later use.
 * </p>
 * 
 * @author Kontantin Pribluda
 * @author Mauro Talevi
 */
public final class ServletChainBuilder {

    private final ServletContext context;
    private final String containerBuilderClassName;
    private final String containerScriptName;
    private final String emptyContainerScript;

    /**
     * Constructor for the ServletChainBuilder object
     * 
     * @param context the ServletContext
     * @param containerBuilderClassName the class name of the ContainerBuilder
     * @param containerScriptName the name of the container script resource
     * @param emptyContainerScript the script for empty container if the
     *            container config is not found
     */
    public ServletChainBuilder(ServletContext context, String containerBuilderClassName, String containerScriptName,
            String emptyContainerScript) {
        this.context = context;
        this.containerBuilderClassName = containerBuilderClassName;
        this.containerScriptName = containerScriptName;
        this.emptyContainerScript = emptyContainerScript;
    }

    /**
     * populate container for given path. cache result in container recorders
     * 
     * @param container the MutablePicoContainer used by the recorder
     * @param path the String representing the servlet path used as key for the
     *            recorder cache
     */
    private void populateContainer(String resourcePath, MutablePicoContainer container) {
        PicoContainer buildContainer = buildContainer(resourcePath, container.getParent());
        for (ComponentAdapter<?> adapter : buildContainer.getComponentAdapters()) {
            container.addAdapter(adapter);
        }
    }

    private PicoContainer buildContainer(String resourcePath, PicoContainer parent) {
        ContainerBuilder builder = createContainerBuilder(obtainReader(resourcePath));
        return builder.buildContainer(parent, null, false);
    }

    private ContainerBuilder createContainerBuilder(Reader reader) {
        ClassLoadingPicoContainer scripted = new DefaultClassLoadingPicoContainer(getClassLoader());
        Parameter[] parameters = new Parameter[] { new ConstantParameter(reader),
                new ConstantParameter(getClassLoader()) };
        scripted.addComponent(containerBuilderClassName, new ClassName(containerBuilderClassName), parameters);
        return scripted.getComponent(ContainerBuilder.class);
    }




    /**
     * Obtain reader from servlet context path, by appending container script
     * name to path. If not found, returns reader for empty container instead.
     * 
     * @param path the String representing the path in servlet context
     * @return A Reader for corresponding script or for empty container if
     *         script not found
     */
    private Reader obtainReader(String path) {
        InputStream is = context.getResourceAsStream(path + containerScriptName);
        if (is != null) {
            return new InputStreamReader(is);
        } else {
            return new StringReader(emptyContainerScript);
        }
    }
    
    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
