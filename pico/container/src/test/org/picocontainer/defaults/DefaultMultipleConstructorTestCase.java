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

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.tck.AbstractMultipleConstructorTest;

/**
 * @author Aslak Helles&oslash;y
 */
public class DefaultMultipleConstructorTestCase extends AbstractMultipleConstructorTest {
    protected MutablePicoContainer createPicoContainer() {
        return new DefaultPicoContainer();
    }
    
    // Eclipse need at least *one* fixture as direct class member ...
    @Test public void testEclipseDummy() {
    }

    @Test
    public void testMultiWithSatisfyingDependencyAndParametersWorks() {
        super.testMultiWithSatisfyingDependencyAndParametersWorks();
    }


}
