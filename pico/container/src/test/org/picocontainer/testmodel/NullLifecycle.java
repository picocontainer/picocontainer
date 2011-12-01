/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the license.txt file.                                                    *
 *****************************************************************************/
package org.picocontainer.testmodel;

import org.picocontainer.Disposable;
import org.picocontainer.Startable;


public class NullLifecycle implements Startable, Disposable {

    public void start() {
    }

    public void stop() {
    }

    public void dispose() {
    }

}