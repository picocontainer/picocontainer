/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.containers;

import java.util.Properties;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.tck.AbstractPicoContainerTest;


public class DelegatingMutablePicoContainerTestCase extends AbstractPicoContainerTest {

    protected MutablePicoContainer createPicoContainer(PicoContainer parent) {
        return new MyDelegatingMutablePicoContainer(new DefaultPicoContainer());
    }

    protected Properties[] getProperties() {
        return new Properties[] { Characteristics.NO_CACHE, Characteristics.NO_HIDE_IMPL};
    }

    @SuppressWarnings("serial")
	private static class MyDelegatingMutablePicoContainer extends AbstractDelegatingMutablePicoContainer {
        public MyDelegatingMutablePicoContainer(MutablePicoContainer parent) {
            super(parent);
        }

        public MutablePicoContainer makeChildContainer() {
            return new MyDelegatingMutablePicoContainer(this);
        }
    }


    @Test public void testAcceptImplementsBreadthFirstStrategy() {
        // don't run this one.
    }

}
