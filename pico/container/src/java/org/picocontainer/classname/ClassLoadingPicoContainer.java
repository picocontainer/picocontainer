/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file. 
 ******************************************************************************/
package org.picocontainer.classname;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.classname.ClassPathElement;

import java.net.URL;

/**
 * A ClassLoadingPicoContainer extends PicoContainer with classloader juggling capability
 * 
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 */
public interface ClassLoadingPicoContainer extends MutablePicoContainer {

    /**
     * Adds a new URL that will be used in classloading
     * 
     * @param url url of the jar to find components in.
     * @return ClassPathElement to add permissions to (subject to security
     *         policy)
     */
    ClassPathElement addClassLoaderURL(URL url);

    /**
     * Returns class loader that is the aggregate of the URLs added.
     * 
     * @return A ClassLoader
     */
    ClassLoader getComponentClassLoader();

    /**
     * Make a child container with a given name
     * 
     * @param name the container name
     * @return The ScriptedPicoContainer
     */
    ClassLoadingPicoContainer makeChildContainer(String name);

    /**
     * Addes a child container with a given name
     * 
     * @param name the container name
     * @param child the child PicoContainer
     */
    ClassLoadingPicoContainer addChildContainer(String name, PicoContainer child);

}
