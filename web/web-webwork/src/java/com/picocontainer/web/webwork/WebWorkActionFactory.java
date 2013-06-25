/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.web.webwork;

import webwork.action.Action;
import webwork.action.factory.ActionFactory;
import webwork.action.factory.AliasingActionFactoryProxy;
import webwork.action.factory.ChainingActionFactoryProxy;
import webwork.action.factory.CommandActionFactoryProxy;
import webwork.action.factory.ContextActionFactoryProxy;
import webwork.action.factory.ParametersActionFactoryProxy;
import webwork.action.factory.PrefixActionFactoryProxy;
import webwork.action.factory.PrepareActionFactoryProxy;

/**
 * Custom webwork action lifecycle that ensures actions are treated as pico components.
 */
public class WebWorkActionFactory extends ActionFactory {

    private ActionFactory factory;

    public WebWorkActionFactory() {
        // replace standard JavaActionFactory with PicoActionFactory
        factory = new PicoActionFactory();
        // the rest are the standard webwork ActionFactoryProxies
        factory = new PrefixActionFactoryProxy(factory);
        factory = new CommandActionFactoryProxy(factory);
        factory = new AliasingActionFactoryProxy(factory);
        factory = new CommandActionFactoryProxy(factory);
        factory = new ContextActionFactoryProxy(factory);
        factory = new PrepareActionFactoryProxy(factory);
        factory = new ParametersActionFactoryProxy(factory);
        factory = new ChainingActionFactoryProxy(factory);
    }

    public Action getActionImpl(String actionName) throws Exception {
        return factory.getActionImpl(actionName);
    }

}
