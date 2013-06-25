/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.web.struts;

import org.apache.struts.action.Action;

/**
 * @author Stephen Molitor
 */
public class StrutsTestAction extends Action implements TestAction {
    private final TestService service;

    public StrutsTestAction(TestService service) {
        this.service = service;
    }

    public TestService getService() {
        return service;
    }

}
