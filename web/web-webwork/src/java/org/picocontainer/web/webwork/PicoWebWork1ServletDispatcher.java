/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.webwork;

import webwork.action.factory.ActionFactory;
import webwork.dispatcher.ServletDispatcher;

/**
 * Extension to the standard WebWork 1 ServletDispatcher that instantiates 
 * a new container in the request scope for each request and disposes of it 
 * correctly at the end of the request.
 * <p/>
 * To use, replace the WebWork ServletDispatcher in web.xml with this.
 *
 * @author Joe Walnes
 */
@SuppressWarnings("serial")
public class PicoWebWork1ServletDispatcher extends ServletDispatcher {

    public PicoWebWork1ServletDispatcher() {
        super();
        ActionFactory.setActionFactory(new WebWorkActionFactory());
    }

}
