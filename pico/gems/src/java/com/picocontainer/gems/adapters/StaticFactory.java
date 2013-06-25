package com.picocontainer.gems.adapters;

/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Leo Simmons & J&ouml;rg Schaible                              *
 *****************************************************************************/
/**
 * Interface for a static factory wrapper used by the {@link StaticFactoryAdapter}.
 *
 * @author J&ouml;rg Schaible
 * @author Leo Simmons
 */
public interface StaticFactory<T> {
    /**
     * @return Returns the instance created by the factory.
     */
    public T get();
}