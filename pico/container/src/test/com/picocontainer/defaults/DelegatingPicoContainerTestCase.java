/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package com.picocontainer.defaults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import com.picocontainer.testmodel.DependsOnTouchable;
import com.picocontainer.testmodel.SimpleTouchable;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.injectors.AbstractInjector;

public class DelegatingPicoContainerTestCase {
    private MutablePicoContainer parent;
    private DefaultPicoContainer child;

    @Before
    public void setUp() throws PicoCompositionException {
        parent = new DefaultPicoContainer();
        child = new DefaultPicoContainer(parent);
    }

    @Test public void testChildGetsFromParent() {
        parent.addComponent(SimpleTouchable.class);
        child.addComponent(DependsOnTouchable.class);
        DependsOnTouchable dependsOnTouchable = child.getComponent(DependsOnTouchable.class);

        assertNotNull(dependsOnTouchable);
    }

    @Test public void testParentDoesntGetFromChild() {
        child.addComponent(SimpleTouchable.class);
        parent.addComponent(DependsOnTouchable.class);
        try {
            parent.getComponent(DependsOnTouchable.class);
            fail();
        } catch (AbstractInjector.UnsatisfiableDependenciesException e) {
        }
    }

    @Test public void testChildOverridesParent() {
        parent.addComponent(SimpleTouchable.class);
        child.addComponent(SimpleTouchable.class);

        SimpleTouchable parentTouchable = parent.getComponent(SimpleTouchable.class);
        SimpleTouchable childTouchable = child.getComponent(SimpleTouchable.class);
        assertEquals(1, child.getComponents().size());
        assertNotSame(parentTouchable, childTouchable);
    }
}
