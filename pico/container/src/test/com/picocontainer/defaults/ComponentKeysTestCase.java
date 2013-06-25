/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.defaults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import com.picocontainer.testmodel.DecoratedTouchable;
import com.picocontainer.testmodel.DependsOnTouchable;
import com.picocontainer.testmodel.SimpleTouchable;
import com.picocontainer.testmodel.Touchable;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.parameters.ComponentParameter;

/**
 * @author Thomas Heller
 * @author Aslak Helles&oslash;y
 */
public class ComponentKeysTestCase {

    @Test public void testComponensRegisteredWithClassKeyTakePrecedenceOverOthersWhenThereAreMultipleImplementations() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent("default", SimpleTouchable.class);

        /**
         * By using a class as key, this should take precedence over the other Touchable
         */
        pico.addComponent(Touchable.class, DecoratedTouchable.class, new ComponentParameter("default"));

        Touchable touchable = pico.getComponent(Touchable.class);
        assertEquals(DecoratedTouchable.class, touchable.getClass());
    }

    @Test public void testComponentAdapterResolutionIsFirstLookedForByClassKeyToTheTopOfTheContainerHierarchy() {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent("default", SimpleTouchable.class);

        // Use the List variant instead, so we get better test coverage.
        pico.addComponent(Touchable.class, DecoratedTouchable.class, new ComponentParameter("default"));

        DefaultPicoContainer grandChild = new DefaultPicoContainer(new DefaultPicoContainer(new DefaultPicoContainer(pico)));

        Touchable touchable = grandChild.getComponent(Touchable.class);
        assertEquals(DecoratedTouchable.class, touchable.getClass());

    }

    @Test public void testComponentKeysFromParentCannotConfuseTheChild() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent("test", SimpleTouchable.class);

        DefaultPicoContainer child = new DefaultPicoContainer(pico);

        child.addComponent("test", DependsOnTouchable.class);

        DependsOnTouchable dot = (DependsOnTouchable) child.getComponent("test");

        assertNotNull(dot);
    }

}
