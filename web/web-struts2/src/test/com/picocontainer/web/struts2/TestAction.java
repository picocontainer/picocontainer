/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.web.struts2;

import com.opensymphony.xwork2.Action;


public final class TestAction implements Action {
    final String foo;
    public TestAction(String foo) {
        this.foo = foo;
    }
    
    public String getFoo() {
        return foo;
    }
    
    public String execute() {
        return foo;
    }
}
