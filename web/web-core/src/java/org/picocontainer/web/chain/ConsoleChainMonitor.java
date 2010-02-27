/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.web.chain;

/**
 * Implementation of ChainMonitor that logs to console
 * 
 * @author Mauro Talevi
 */
public class ConsoleChainMonitor implements ChainMonitor {

    /**
     * @see org.picocontainer.web.chain.ChainMonitor#filteringURL(java.lang.String)
     */
    public void filteringURL(String originalUrl) {
        System.err.println("Filtering "+originalUrl);
    }

    /**
     * @see org.picocontainer.web.chain.ChainMonitor#exceptionOccurred(java.lang.Exception)
     */
    public void exceptionOccurred(Exception e) {
        e.printStackTrace();
    }

    /**
     * @see org.picocontainer.web.chain.ChainMonitor#pathAdded(java.lang.String, java.lang.String)
     */
    public void pathAdded(String path, String url) {
        System.err.println("Added path "+path+" from URL "+url);
    }

}
