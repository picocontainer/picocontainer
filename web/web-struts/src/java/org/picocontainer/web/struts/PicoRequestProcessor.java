/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.RequestProcessor;

/**
 * Uses Pico to produce Actions and inject dependencies into them. If you are
 * using the Tiles library, use {@link PicoTilesRequestProcessor} instead.
 * 
 * @author Stephen Molitor
 * @see PicoActionFactory
 * @see PicoTilesRequestProcessor
 */
public class PicoRequestProcessor extends RequestProcessor {

    private final PicoActionFactory actionFactory = new PicoActionFactory();

    /**
     * Creates or retrieves the action instance. The action is retrieved from
     * the actions Pico container, using the mapping path as the component key.
     * If no such action exists, a new one will be instantiated and placed in
     * the actions container, thus injecting its dependencies.
     * 
     * @param request the HTTP request object.
     * @param response the HTTP response object.
     * @param mapping the action mapping.
     * @return the action instance.
     */
    protected Action processActionCreate(HttpServletRequest request, HttpServletResponse response, ActionMapping mapping) {
        return actionFactory.getAction(request, mapping, this.servlet);
    }

}
