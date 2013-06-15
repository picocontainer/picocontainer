/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by the committers                                           *
 *****************************************************************************/

package org.picocontainer.containers;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoContainer;

import com.googlecode.jtype.Generic;

/**
 * @author Mauro Talevi
 */
public class EmptyPicoContainerTestCase {

    @Test public void testReturnValues() {
        PicoContainer pico = new EmptyPicoContainer();
        assertNull(pico.getComponentAdapter((Generic)null, (NameBinding) null));
        assertNull(pico.getComponentAdapter((Generic)null, (NameBinding) null));
        assertTrue(pico.getComponentAdapters().isEmpty());
        assertTrue(pico.getComponentAdapters((Generic) null).isEmpty());
        assertNull(pico.getComponent((Class) null));
        assertNull(pico.getComponent((Class) null));
        assertTrue(pico.getComponents().isEmpty());
        assertTrue(pico.getComponents(null).isEmpty());
        assertNull(pico.getParent());
    }
}
