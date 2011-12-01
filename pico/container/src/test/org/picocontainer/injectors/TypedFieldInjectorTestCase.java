/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.monitors.NullComponentMonitor;

public class TypedFieldInjectorTestCase {

    public static class Helicopter {
        private PogoStick pogo;
    }


    public static class PogoStick {
    }

    @Test public void testFieldInjectionByType() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new TypedFieldInjection.TypedFieldInjector(Helicopter.class, Helicopter.class, new NullComponentMonitor(), Integer.class.getName() + " " + PogoStick.class.getName() + " " + Float.class.getName(), null
        ));
        pico.addComponent(PogoStick.class, new PogoStick());
        Helicopter chopper = pico.getComponent(Helicopter.class);
        assertNotNull(chopper);
        assertNotNull(chopper.pogo);
    }


}