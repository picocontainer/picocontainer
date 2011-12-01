/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the license.txt file.                                                    *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/
package org.picocontainer.gems.util;


import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

/**
 * constructable properties. 
 *
 * @author Konstantin Pribluda
 */
@SuppressWarnings("serial")
public class ConstructableProperties extends Properties {

	/**
     * create properties from classpath resource using context classloader
     *
     * @param resource         resource name
     * @exception IOException passed from Properties.load()
     */
    public ConstructableProperties(final String resource) throws IOException {
        super();
        load(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource));
    }
    /**
     * 
     * @param resource resource name
     * @param defaults default properties
     * @throws IOException can be thrown if something goes wrong
     */
    public ConstructableProperties(final String resource, final Properties defaults) throws IOException {
        super(defaults);
        load(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource));   
    }
    
    /**
     * create properties from input stream
     * @param stream to read from 
     * @throws IOException can be thrown by properties objkect
     */
    public ConstructableProperties(final InputStream stream) throws IOException {
        super();
        load(stream);
    }
    /**
     * create from inpiut stream with default properties
     * @param stream to read from 
     * @param defaults default properties
     * @throws IOException can be thrown by properties object
     */
    public ConstructableProperties(final InputStream stream, final Properties defaults) throws IOException {
        super(defaults);
        load(stream);
    }
}
