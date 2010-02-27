/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/

package org.picocontainer.gems.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.testmodel.AlternativeTouchable;
import org.picocontainer.testmodel.DecoratedTouchable;
import org.picocontainer.testmodel.DependsOnArray;
import org.picocontainer.testmodel.DependsOnList;
import org.picocontainer.testmodel.DependsOnTouchable;
import org.picocontainer.testmodel.DependsOnTwoComponents;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;

/**
 * Integration tests using Constraints.
 *
 * @author Nick Sieger
 */
public class ConstraintIntegrationTestCase {

    MutablePicoContainer container;

    @Before
    public void setUp() throws Exception {       
        container = new DefaultPicoContainer(new Caching());
        container.addComponent(SimpleTouchable.class);
        container.addComponent(DependsOnTouchable.class);
        container.addComponent(DependsOnTwoComponents.class);
        container.addComponent(ArrayList.class, new ArrayList());
        container.addComponent(Object[].class, new Object[0]);
    }


    @Test public void testAmbiguouTouchableDependency() {
        container.addComponent(AlternativeTouchable.class);
        container.addComponent(DecoratedTouchable.class);

        try {
            container.getComponent(DecoratedTouchable.class);
            fail("AmbiguousComponentResolutionException expected");
        } catch (AbstractInjector.AmbiguousComponentResolutionException acre) {
            // success
        }
    }

    @Test public void testTouchableDependencyWithComponentKeyParameter() {
        container.addComponent(AlternativeTouchable.class);
        container.addComponent(DecoratedTouchable.class,
                                                  DecoratedTouchable.class,
                                                  new ComponentParameter(SimpleTouchable.class));

        Touchable t = container.getComponent(DecoratedTouchable.class);
        assertNotNull(t);
    }

    @Test public void testTouchableDependencyInjectedViaConstraint() {
        container.addComponent(AlternativeTouchable.class);
        container.addComponent(DecoratedTouchable.class,
                                                  DecoratedTouchable.class,
                                                  new Not(new IsType(SimpleTouchable.class)));
        Touchable t = container.getComponent(DecoratedTouchable.class);
        assertNotNull(t);
    }

    @Test public void testComponentDependsOnCollectionOfEverythingElse() {
        container.addComponent(DependsOnList.class,
                                                  DependsOnList.class,
                                                  new CollectionConstraint(Anything.ANYTHING));
        DependsOnList dol = container.getComponent(DependsOnList.class);
        assertNotNull(dol);
        List dependencies = dol.getDependencies();
        assertEquals(5, dependencies.size());
    }

    @Test public void testComponentDependsOnCollectionOfTouchables() {
        container.addComponent(AlternativeTouchable.class);
        container.addComponent(DecoratedTouchable.class,
                                                  DecoratedTouchable.class,
                                                  new Not(new IsType(SimpleTouchable.class)));
        container.addComponent(DependsOnList.class,
                                                  DependsOnList.class,
                                                  new CollectionConstraint(new IsType(Touchable.class)));
        DependsOnList dol = container.getComponent(DependsOnList.class);
        assertNotNull(dol);
        List dependencies = dol.getDependencies();
        assertEquals(3, dependencies.size());
    }

    @Test public void testComponentDependsOnCollectionOfSpecificTouchables() {
        container.addComponent(AlternativeTouchable.class);
        container.addComponent(DecoratedTouchable.class,
                                                  DecoratedTouchable.class,
                                                  new Not(new IsType(SimpleTouchable.class)));
        container.addComponent(DependsOnList.class,
                                                  DependsOnList.class,
                                                  new CollectionConstraint(new Or(new IsType(AlternativeTouchable.class),
                                                                                  new IsType(DecoratedTouchable.class))));

        DependsOnList dol = container.getComponent(DependsOnList.class);
        AlternativeTouchable at = container.getComponent(AlternativeTouchable.class);
        DecoratedTouchable dt = container.getComponent(DecoratedTouchable.class);
        assertNotNull(dol);
        List dependencies = dol.getDependencies();
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains(at));
        assertTrue(dependencies.contains(dt));
    }

    @Test public void testComponentDependsOnArrayOfEverythingElse() {
        container.addComponent(DependsOnArray.class,
                                                  DependsOnArray.class,
                                                  new CollectionConstraint(Anything.ANYTHING));
        DependsOnArray doa = container.getComponent(DependsOnArray.class);
        assertNotNull(doa);
        Object[] dependencies = doa.getDependencies();
        assertEquals(5, dependencies.length);
    }

}
