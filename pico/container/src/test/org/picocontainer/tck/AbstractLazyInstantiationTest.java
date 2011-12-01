/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.tck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;

/**
 * @author Aslak Helles&oslash;y
 */
public abstract class AbstractLazyInstantiationTest {

    protected abstract MutablePicoContainer createPicoContainer();

    public static class Kilroy {
        public Kilroy(Havana havana) {
            havana.graffiti("Kilroy was here");
        }
    }

    public static class Havana {
        public String paint = "Clean wall";

        public void graffiti(String paint) {
            this.paint = paint;
        }
    }

    @Test public void testLazyInstantiation() throws PicoException {
        MutablePicoContainer pico = createPicoContainer();

        pico.as(Characteristics.CACHE).addComponent(Kilroy.class);
        pico.as(Characteristics.CACHE).addComponent(Havana.class);

        assertSame(pico.getComponent(Havana.class), pico.getComponent(Havana.class));
        assertNotNull(pico.getComponent(Havana.class));
        assertEquals("Clean wall", pico.getComponent(Havana.class).paint);
        assertNotNull(pico.getComponent(Kilroy.class));
        assertEquals("Kilroy was here", pico.getComponent(Havana.class).paint);
    }
}
