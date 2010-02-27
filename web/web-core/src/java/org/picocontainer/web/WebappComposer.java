/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web;

import org.picocontainer.MutablePicoContainer;

import javax.servlet.ServletContext;

/**
 * Allows to compose containers for different webapp scopes. The composer is
 * used by the
 * {@link org.picocontainer.web.PicoServletContainerListener PicoServletContainerListener}
 * after the webapp context is initialised. Users can either implement their
 * composer and register components for each scope directly or load them from a
 * picocontainer script, using the
 * {@link org.picocontainer.web.script.ScriptedWebappComposer ScriptedWebappComposer}.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public interface WebappComposer {

    void composeApplication(MutablePicoContainer container, ServletContext servletContext);

    void composeSession(MutablePicoContainer container);

    void composeRequest(MutablePicoContainer container);

}
