/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.containers;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import junit.framework.AssertionFailedError;

import org.junit.Test;
import com.picocontainer.tck.AbstractImplementationHidingPicoContainerTest;

import com.picocontainer.Characteristics;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.behaviors.ImplementationHiding;
import com.picocontainer.injectors.ConstructorInjection;

/**
 *
 * @author Aslak Helles&oslash;y
 */
public class ImplementationHidingWithDefaultPicoContainerTestCase extends AbstractImplementationHidingPicoContainerTest {

    @Override
	protected MutablePicoContainer createImplementationHidingPicoContainer() {
        return createPicoContainer(null);
    }

    @Override
	protected Properties[] getProperties() {
        return new Properties[] {Characteristics.NO_CACHE, Characteristics.NO_HIDE_IMPL};
    }

    // TODO (PH) should IH do caching at all and CtorInjection instead of AdaptingInjection ?

    @Override
	protected void addDefaultComponentFactories(final List expectedList) {
        expectedList.add(Caching.class);
        expectedList.add(ImplementationHiding.class);
        expectedList.add(ConstructorInjection.class);
    }

    @Override
	protected MutablePicoContainer createPicoContainer(final PicoContainer parent) {
        return new DefaultPicoContainer(parent, new Caching().wrap(new ImplementationHiding().wrap(new ConstructorInjection())));
    }

    @Override
	@Test
    public void testAggregatedVerificationException() {
        super.testAggregatedVerificationException();
    }

    @Override
	@Test public void testSameInstanceCanBeUsedAsDifferentTypeWhenCaching() {
        // we're choosing a CAF for DPC, thus Caching (a default) not enabled.
        try {
            super.testSameInstanceCanBeUsedAsDifferentTypeWhenCaching();
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().indexOf("expected same:<com.picocontainer.testmodel.WashableTouchable@") > -1);
            assertTrue(e.getMessage().indexOf("was not:<com.picocontainer.testmodel.WashableTouchable@") > -1);
        }

    }

    @Override
	@Test public void testAcceptImplementsBreadthFirstStrategy() {
        super.testAcceptImplementsBreadthFirstStrategy();
    }

}
