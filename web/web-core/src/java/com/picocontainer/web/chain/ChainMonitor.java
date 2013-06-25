/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.web.chain;

/**
 * @author Mauro Talevi
 */
public interface ChainMonitor {

    /**
     * Filtering the original URL
     * @param originalUrl
     */
    void filteringURL(String originalUrl);

    /**
     * An exception occurred in the filter chain
     * @param e
     */
    void exceptionOccurred(Exception e);

    /**
     * A path has been added from the original URL
     * @param path
     * @param url
     */
    void pathAdded(String path, String url);

}
