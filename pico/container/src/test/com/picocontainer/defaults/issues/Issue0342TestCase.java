/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.defaults.issues;

import org.junit.Assert;
import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;

public final class Issue0342TestCase {

    interface Interface {
    }

    interface SubInterface extends Interface {
    }

    public static class AGeneric<I extends Interface> {
        private final I iface;

        public AGeneric(final I iface) {
            this.iface = iface;
        }
    }

    public static class Implementation implements Interface {
    }

    public static class SubImplementation implements SubInterface {
    }


    @Test
    public void testNotTheBug() {
        //hard coded instantitation
        AGeneric<Implementation> generic1 = new AGeneric<Implementation>(new Implementation());
        Assert.assertNotNull(generic1);
        AGeneric<SubImplementation> generic2 = new AGeneric<SubImplementation>(new SubImplementation());
        Assert.assertNotNull(generic2);
    }


    @Test
    public void testTheBug() {

        //using picocontainer
        DefaultPicoContainer container = new DefaultPicoContainer();
        container.addComponent(Implementation.class);
        container.addComponent(AGeneric.class);
        AGeneric result = container.getComponent(AGeneric.class); // fails here.
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.iface);

    }


    @Test
    public void testTheBug2() {

        DefaultPicoContainer container = new DefaultPicoContainer();
        container.addComponent(SubImplementation.class);
        container.addComponent(AGeneric.class);
        //should be Generic<SubImplementation> but requires unsafe cast
        AGeneric<?> result2 = container.getComponent(AGeneric.class); // fails here
        Assert.assertNotNull(result2);
        Assert.assertNotNull(result2.iface);

    }

}