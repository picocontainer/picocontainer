/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.defaults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.testmodel.AlternativeTouchable;
import org.picocontainer.testmodel.DependsOnTouchable;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;

/**
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Rafal Krzewski
 */
public class ChildContainerTestCase {

    @Test public void testParentContainerWithComponentWithEqualKeyShouldBeShadowedByChild() throws Exception {
        DefaultPicoContainer parent = new DefaultPicoContainer();
        DefaultPicoContainer child = new DefaultPicoContainer(parent);

        parent.addComponent("key", AlternativeTouchable.class);
        child.addComponent("key", SimpleTouchable.class);
        child.addComponent(DependsOnTouchable.class);

        DependsOnTouchable dot = child.getComponent(DependsOnTouchable.class);
        assertEquals(SimpleTouchable.class, dot.getTouchable().getClass());
    }

    @Test public void testParentComponentRegisteredAsClassShouldBePreffered() throws Exception {
        DefaultPicoContainer parent = new DefaultPicoContainer();
        DefaultPicoContainer child = new DefaultPicoContainer(parent);

        parent.addComponent(Touchable.class, AlternativeTouchable.class);
        child.addComponent("key", SimpleTouchable.class);
        child.addComponent(DependsOnTouchable.class);

        DependsOnTouchable dot = child.getComponent(DependsOnTouchable.class);
        assertEquals(AlternativeTouchable.class, dot.getTouchable().getClass());
    }

    @Test public void testResolveFromParentByType() {
        MutablePicoContainer parent = new DefaultPicoContainer();
        parent.addComponent(Touchable.class, SimpleTouchable.class);

        MutablePicoContainer child = new DefaultPicoContainer(parent);
        child.addComponent(DependsOnTouchable.class);

        assertNotNull(child.getComponent(DependsOnTouchable.class));
    }

    @Test public void testResolveFromParentByKey() {
        MutablePicoContainer parent = new DefaultPicoContainer();
        parent.addComponent(Touchable.class, SimpleTouchable.class);

        MutablePicoContainer child = new DefaultPicoContainer(parent);
        child.addComponent(DependsOnTouchable.class, DependsOnTouchable.class,
                           new ComponentParameter(Touchable.class));

        assertNotNull(child.getComponent(DependsOnTouchable.class));
    }

    @Test public void testResolveFromGrandParentByType() {
        MutablePicoContainer grandParent = new DefaultPicoContainer();
        grandParent.addComponent(Touchable.class, SimpleTouchable.class);

        MutablePicoContainer parent = new DefaultPicoContainer(grandParent);

        MutablePicoContainer child = new DefaultPicoContainer(parent);
        child.addComponent(DependsOnTouchable.class);

        assertNotNull(child.getComponent(DependsOnTouchable.class));
    }

    @Test public void testResolveFromGrandParentByKey() {
        MutablePicoContainer grandParent = new DefaultPicoContainer();
        grandParent.addComponent(Touchable.class, SimpleTouchable.class);

        MutablePicoContainer parent = new DefaultPicoContainer(grandParent);

        MutablePicoContainer child = new DefaultPicoContainer(parent);
        child.addComponent(DependsOnTouchable.class, DependsOnTouchable.class,
                           new ComponentParameter(Touchable.class));

        assertNotNull(child.getComponent(DependsOnTouchable.class));
    }

    @Test public void testResolveFromGrandParentAndParentOnlyIfChildCannotResolve() {
        MutablePicoContainer grandParent = new DefaultPicoContainer();
        SimpleTouchable gp = new SimpleTouchable();
        grandParent.addComponent(Touchable.class, gp);

        MutablePicoContainer parent = new DefaultPicoContainer(grandParent);
        SimpleTouchable p = new SimpleTouchable();
        parent.addComponent(Touchable.class, p);

        MutablePicoContainer child = new DefaultPicoContainer(parent);
        SimpleTouchable c = new SimpleTouchable();
        child.addComponent(Touchable.class, c);
        child.addComponent(DependsOnTouchable.class);

        DependsOnTouchable dot = child.getComponent(DependsOnTouchable.class);
        assertNotNull(dot);
        assertEquals(c, dot.getTouchable());
    }


}
