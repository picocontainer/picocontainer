/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy & Joerg Schaible                                       *
 *****************************************************************************/
package org.picocontainer.gems.util;

import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.toys.multicast.Multicasting;
import org.picocontainer.PicoContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Factory for creating a multicaster object that multicasts calls to all
 * components in a PicoContainer instance.
 *
 * @author Aslak Helles&oslash;y
 * @author Chris Stevenson
 * @author Paul Hammant
 */
public class Multicaster {
    /**
     * Create a {@link Multicasting} proxy for the components of a {@link PicoContainer}.
     * 
     * @param pico the container
     * @param callInInstantiationOrder <code>true</code> if the components will be called in instantiation order
     * @param proxyFactory the ProxyFactory to use
     * @return the Multicasting proxy
     */
    public static Object object(final PicoContainer pico, boolean callInInstantiationOrder, final ProxyFactory proxyFactory) {
        List copy = new ArrayList(pico.getComponents());

        if (!callInInstantiationOrder) {
            // reverse the list
            Collections.reverse(copy);
        }
        Object[] targets = copy.toArray();
        return Multicasting.object(proxyFactory, targets);
    }
}