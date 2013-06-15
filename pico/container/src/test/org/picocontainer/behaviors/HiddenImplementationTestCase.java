/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.injectors.ConstructorInjection;

public class HiddenImplementationTestCase {

    @Test public void testMultipleInterfacesCanBeHidden() {
        ComponentAdapter ca = new ConstructorInjection.ConstructorInjector(new Class[]{ActionListener.class, MouseListener.class}, Footle.class);
        ImplementationHiding.HiddenImplementation ihca = new ImplementationHiding.HiddenImplementation(ca);
        Object comp = ihca.getComponentInstance(null, null);
        assertNotNull(comp);
        assertTrue(comp instanceof ActionListener);
        assertTrue(comp instanceof MouseListener);
    }

    @Test public void testNonInterfaceInArrayCantBeHidden() {
        ComponentAdapter ca = new ConstructorInjection.ConstructorInjector(new Class[]{String.class}, Footle.class);
        ImplementationHiding.HiddenImplementation ihca = new ImplementationHiding.HiddenImplementation(ca);
        try {
            ihca.getComponentInstance(null, null);
            fail("PicoCompositionException expected");
        } catch (PicoCompositionException e) {
            // expected
        }
    }



    public class Footle implements ActionListener, MouseListener {
        public void actionPerformed(final ActionEvent e) {
        }

        public void mouseClicked(final MouseEvent e) {
        }

        public void mouseEntered(final MouseEvent e) {
        }

        public void mouseExited(final MouseEvent e) {
        }

        public void mousePressed(final MouseEvent e) {
        }

        public void mouseReleased(final MouseEvent e) {
        }

    }

}
