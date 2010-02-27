/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/

package org.picocontainer.script.testmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ResourceBundleWebServerConfig implements WebServerConfig {

    String host = "localhost";
    int port = 8080;

    public ResourceBundleWebServerConfig() throws IOException {

        File file = new File(System.getProperty("basedir"), "ResourceBundleWebServerConfig.properties");
        // how do you get this working in intellij, maven and eclipse ?
        if (file.exists()) {
            ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(file));
            host = bundle.getString("host");
            port = Integer.parseInt(bundle.getString("port"));
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}
