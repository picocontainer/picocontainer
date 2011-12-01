/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.containers.EmptyPicoContainer;

/**
 * Helper for ScriptedPicoContainer
 * 
 * @author Paul Hammant
 */
public class ContainerElementHelper {

    public static ClassLoadingPicoContainer makeScriptedPicoContainer(ComponentFactory componentFactory,
            PicoContainer parent, ClassLoader classLoader) {
        if (parent == null) {
            parent = new EmptyPicoContainer();
        }
        if (componentFactory == null) {
            componentFactory = new Caching();
        }
        return new DefaultClassLoadingPicoContainer(classLoader, new DefaultPicoContainer(parent, componentFactory));

    }

    public static void debug(List<?> arg0, Map<?,?> arg1) {
        System.out.println("-->debug " + arg0.size() + " " + arg1.size());
        for (int i = 0; i < arg0.size(); i++) {
            Object o = arg0.get(i);
            System.out.println("--> arg0[" + i + "] " + o);

        }
        Set<?> keys = arg1.keySet();
        int i = 0;
        for (Object o : keys) {
            System.out.println("--> arg1[" + i++ + "] " + o + ", " + arg1.get(o));

        }
    }

}
