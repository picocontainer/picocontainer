/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/

package com.picocontainer.gems.behaviors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import com.picocontainer.tck.AbstractComponentFactoryTest;
import com.picocontainer.testmodel.AlternativeTouchable;
import com.picocontainer.testmodel.CompatibleTouchable;
import com.picocontainer.testmodel.SimpleTouchable;
import com.picocontainer.testmodel.Touchable;

import com.picocontainer.ComponentFactory;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.injectors.ConstructorInjection;



/**
 * @author J&ouml;rg Schaible
 */
public class AssimilatingTestCase extends AbstractComponentFactoryTest {

    /**
     * @see com.picocontainer.tck.AbstractComponentFactoryTestCase#createComponentFactory()
     */
    @Override
	protected ComponentFactory createComponentFactory() {
        return new Assimilating(Touchable.class).wrap(new ConstructorInjection());
    }

    /**
     * Test automatic assimilation of registered components.
     */
    @Test
    public void testAutomaticAssimilation() {
        picoContainer = new DefaultPicoContainer(createComponentFactory());
        picoContainer.addComponent(SimpleTouchable.class);
        picoContainer.addComponent(AlternativeTouchable.class);
        picoContainer.addComponent(CompatibleTouchable.class);
        final List list = picoContainer.getComponents(Touchable.class);
        assertEquals(3, list.size());
    }

    /**
     * Test automatic assimilation of registered components.
     */
    @Test
    public void testOnlyOneTouchableComponentKeyPossible() {
        picoContainer = new DefaultPicoContainer(createComponentFactory());
        picoContainer.addComponent(Touchable.class, SimpleTouchable.class);
        try {
            picoContainer.addComponent(CompatibleTouchable.class);
            fail("DuplicateComponentKeyRegistrationException expected");
        } catch (final PicoCompositionException e) {
            assertTrue(e.getMessage().startsWith("Duplicate"));
            // fine
        }
    }

    /**
     * Test automatic assimilation of registered components.
     */
    @Test
    public void testMultipleAssimilatedComponentsWithUserDefinedKeys() {
        picoContainer = new DefaultPicoContainer(createComponentFactory());
        picoContainer.addComponent(Touchable.class, SimpleTouchable.class);
        picoContainer.addComponent("1", CompatibleTouchable.class);
        picoContainer.addComponent("2", CompatibleTouchable.class);
        picoContainer.addComponent("3", CompatibleTouchable.class);
        final List list = picoContainer.getComponents(Touchable.class);
        assertEquals(4, list.size());
    }

}
