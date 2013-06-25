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

import java.util.Properties;

import org.junit.Test;
import com.picocontainer.tck.AbstractPicoContainerTest;

import com.picocontainer.Characteristics;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoContainer;
import com.picocontainer.containers.AbstractDelegatingMutablePicoContainer;


public class DelegatingMutablePicoContainerTestCase extends AbstractPicoContainerTest {

    @Override
	protected MutablePicoContainer createPicoContainer(final PicoContainer parent) {
        return new MyDelegatingMutablePicoContainer(new PicoBuilder(parent)
        			.withCaching()
        			.withLifecycle()
        			.build());
    }

	@Override
	protected Properties[] getProperties() {
		return new Properties[] {Characteristics.CACHE};
	}

    @SuppressWarnings("serial")
	private static class MyDelegatingMutablePicoContainer extends AbstractDelegatingMutablePicoContainer {
        public MyDelegatingMutablePicoContainer(final MutablePicoContainer parent) {
            super(parent);
        }

        @Override
		public MutablePicoContainer makeChildContainer() {
            return new MyDelegatingMutablePicoContainer(this);
        }
    }


    public static class A {

    }

    public static class B {

    }

    @Test
    public void testAddComponentReturnsOutermostContainer() {
    	MutablePicoContainer outer = createPicoContainer(null);

    	MutablePicoContainer resultOfAddComponents = outer.addComponent(A.class)
    													  .addComponent(B.class);
    	assertTrue(resultOfAddComponents == outer);
    }


    @Override
	@Test public void testAcceptImplementsBreadthFirstStrategy() {
    	//Ignore this one.
    }
}
