/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.script.testmodel;

import org.picocontainer.Disposable;
import org.picocontainer.Startable;

/**
 * An abstract component and three dependancies used for testing.
 */
public abstract class X implements Startable, Disposable {

    public static String componentRecorder = "";

    public static void reset() {
        componentRecorder = "";
    }

    public void start() {
        componentRecorder += "<" + code();
    }

    public void stop() {
        componentRecorder += code() + ">";
    }

    public void dispose() {
        componentRecorder += "!" + code();
    }

    private String code() {
        String name = getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1, name.length());
    }

}
