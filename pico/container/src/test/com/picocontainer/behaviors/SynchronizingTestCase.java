/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.behaviors;


import static com.picocontainer.Characteristics.NO_SYNCHRONIZE;
import static com.picocontainer.Characteristics.SYNCHRONIZE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import com.picocontainer.tck.AbstractComponentFactoryTest;

import com.picocontainer.ComponentFactory;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.behaviors.Synchronizing;
import com.picocontainer.injectors.AdaptingInjection;

public class SynchronizingTestCase extends AbstractComponentFactoryTest {

	private final ComponentFactory synchronizing = new Synchronizing().wrap(new AdaptingInjection());


	@Override
	protected ComponentFactory createComponentFactory() {
		return synchronizing;
	}

	@Test
	public void testPicoContainerPropertiesIntegration() {
		MutablePicoContainer mpc = new PicoBuilder().withBehaviors(new Synchronizing()).build();
		mpc.as(SYNCHRONIZE).addComponent("a", "This is a test");
		mpc.as(NO_SYNCHRONIZE).addComponent("b","This is a test");

		assertNotNull(mpc.getComponentAdapter("a").findAdapterOfType(Synchronizing.Synchronized.class));
		assertNull(mpc.getComponentAdapter("b").findAdapterOfType(Synchronizing.Synchronized.class));
	}



}
