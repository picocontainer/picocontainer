/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
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
import org.apache.struts.tiles.TilesRequestProcessor;

/**
 * Uses Pico to produce Actions and inject dependencies into them. Use this
 * class if you are using the Tiles library. If not, you can use the
 * {@link org.picocontainer.web.struts.PicoRequestProcessor} instead.
 * 
 * @author Stephen Molitor
 * @see PicoActionFactory
 * @see org.picocontainer.web.struts.PicoRequestProcessor
 */
public class PicoTilesRequestProcessor extends TilesRequestProcessor {

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
        return actionFactory.getAction(request, mapping, servlet);
    }

}
