/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.gems.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.Disposable;
import org.picocontainer.Startable;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.testmodel.RecordingLifecycle;

import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.factory.StandardProxyFactory;

/**
 * @author Aslak Helles&oslash;y
 */
public class MulticasterTestCase {
    @Test public void testOrderOfInstantiationShouldBeDependencyOrder() throws Exception {

        DefaultPicoContainer pico = new DefaultPicoContainer(new Caching());
        pico.addComponent("recording", StringBuffer.class);
        pico.addComponent(RecordingLifecycle.Four.class);
        pico.addComponent(RecordingLifecycle.Two.class);
        pico.addComponent(RecordingLifecycle.One.class);
        pico.addComponent(RecordingLifecycle.Three.class);

        ProxyFactory proxyFactory = new StandardProxyFactory();
        Startable startable = (Startable) Multicaster.object(pico, true, proxyFactory);
        Startable stoppable = (Startable) Multicaster.object(pico, false, proxyFactory);
        Disposable disposable = (Disposable) Multicaster.object(pico, false, proxyFactory);

        startable.start();
        stoppable.stop();
        disposable.dispose();

        assertEquals("<One<Two<Three<FourFour>Three>Two>One>!Four!Three!Two!One", pico.getComponent("recording").toString());
    }

}