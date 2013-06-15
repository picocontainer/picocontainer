/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.util;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.classname.ClassPathElement;
import org.picocontainer.script.ScriptedPicoContainerMarkupException;

public class ClassPathElementHelper {
    public static final String HTTP = "http://";

    public static ClassPathElement addClassPathElement(final String path, final ClassLoadingPicoContainer container) {
        URL pathURL;
        try {
            if (path.toLowerCase().startsWith(HTTP)) {
                pathURL = new URL(path);
            } else {
                Object rVal = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        try {
                            File file = new File(path);
                            if (!file.exists()) {
                                return new ScriptedPicoContainerMarkupException("classpath '" + path + "' does not exist ");
                            }
                            return file.toURI().toURL();
                        } catch (MalformedURLException e) {
                            return e;
                        }

                    }
                });
                if (rVal instanceof MalformedURLException) {
                    throw (MalformedURLException) rVal;
                }
                if (rVal instanceof ScriptedPicoContainerMarkupException) {
                    throw (ScriptedPicoContainerMarkupException) rVal;
                }
                pathURL = (URL) rVal;
            }
        } catch (MalformedURLException e) {
            throw new ScriptedPicoContainerMarkupException("classpath '" + path + "' malformed ", e);
        }
        return container.addClassLoaderURL(pathURL);
    }
}
