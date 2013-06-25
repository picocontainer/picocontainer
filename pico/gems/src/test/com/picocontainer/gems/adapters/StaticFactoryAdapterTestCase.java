/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Leo Simmons & Joerg Schaible                             *
 *****************************************************************************/
package com.picocontainer.gems.adapters;

import static org.junit.Assert.assertNotNull;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.Test;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;


/**
 * @author J&ouml;rg Schaible
 */
public class StaticFactoryAdapterTestCase {

    @Test public void testStaticFactoryInAction() {
        ComponentAdapter<Registry> componentAdapter = new StaticFactoryAdapter<Registry>(Registry.class, new StaticFactory<Registry>() {
            public Registry get() {
                try {
                    return LocateRegistry.getRegistry();
                } catch (RemoteException e) {
                    return null;
                }
            }
        });

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(componentAdapter).getComponentAdapter(componentAdapter.getComponentKey()).verify(pico);
        Registry registry = pico.getComponent(Registry.class);
        assertNotNull(registry);
    }
}
