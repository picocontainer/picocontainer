/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.struts;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;

import javax.servlet.http.HttpServletRequest;

/**
 * Uses Pico to produce Actions and inject dependencies into them. Only use this
 * class if you are using Struts 1.0. If you are using Struts 1.1, use
 * {@link PicoRequestProcessor} or {@link PicoTilesRequestProcessor} instead.
 * 
 * @author Stephen Molitor
 * @see PicoActionFactory
 * @see PicoRequestProcessor
 * @see PicoTilesRequestProcessor
 */
@SuppressWarnings("serial")
public class PicoActionServlet extends ActionServlet {

    private final PicoActionFactory actionFactory = new PicoActionFactory();

    /**
     * Creates or retrieves the action instance. The action is retrieved from
     * the actions Pico container, using the mapping path as the component key.
     * If no such action exists, a new one will be instantiated and placed in
     * the actions container, thus injecting its dependencies.
     * 
     * @param mapping the action mapping.
     * @param request the HTTP request.
     * @return the action instance.
     */
    protected Action processActionCreate(ActionMapping mapping, HttpServletRequest request) {
        return actionFactory.getAction(request, mapping, this);
    }

}
