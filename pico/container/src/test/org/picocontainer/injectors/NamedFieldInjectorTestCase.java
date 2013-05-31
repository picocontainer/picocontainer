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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.monitors.NullComponentMonitor;

public class NamedFieldInjectorTestCase {

    public static class Helicopter {
        private PogoStick pogo;
    }

    public static class Biplane {
        private String wing1;
        private String wing2;
    }

    
    public static class Monoplane {
        private String wing1;
    }    

    public static class PogoStick {
    }

    @Test public void testFieldInjectionByType() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new NamedFieldInjection.NamedFieldInjector<Helicopter>(Helicopter.class, Helicopter.class, new NullComponentMonitor(), " aa bb cc pogo dd ", true))
         	.addComponent(PogoStick.class, new PogoStick());
        Helicopter chopper = pico.getComponent(Helicopter.class);
        assertNotNull(chopper);
        assertNotNull(chopper.pogo);
    }

    @Test public void testFieldInjectionByName() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addAdapter(new NamedFieldInjection.NamedFieldInjector<Biplane>(Biplane.class, Biplane.class, new NullComponentMonitor(), " aa wing1 cc wing2 dd ", true));
        pico.addConfig("wing1", "hello");
        pico.addConfig("wing2", "goodbye");
        Biplane biplane = pico.getComponent(Biplane.class);
        assertNotNull(biplane);
        assertNotNull(biplane.wing1);
        assertEquals("hello", biplane.wing1);
        assertNotNull(biplane.wing2);
        assertEquals("goodbye", biplane.wing2);
    }


    @Test public void testFieldInjectionByTypeWhereNoMatch() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.setName("parent");
        pico.addAdapter(new NamedFieldInjection.NamedFieldInjector<Monoplane>(Monoplane.class, Monoplane.class,
                new NullComponentMonitor(), " aa wing1 cc wing2 dd ", true));
        try {
            pico.getComponent(Monoplane.class);
            fail("should have barfed");
        } catch (AbstractInjector.UnsatisfiableDependenciesException e) {
            String expected = "Monoplane has unsatisfied dependency for fields [ Monoplane.wing1 (field's type is String) ] from parent:1<|";
            String actual = e.getMessage().replace("java.lang.","");
            actual = actual.replace(NamedFieldInjectorTestCase.class.getName() + "$", "");
            assertEquals(expected, actual);
        }
    }

}