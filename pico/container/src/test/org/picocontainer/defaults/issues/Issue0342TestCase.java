/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.defaults.issues;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.Assert;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.AbstractInjector;

public final class Issue0342TestCase {

    interface Interface {
    }

    interface SubInterface extends Interface {
    }

    public static class Generic<I extends Interface> {
        private final I iface;

        public Generic(final I iface) {
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
        Generic<Implementation> generic1 = new Generic<Implementation>(new Implementation());
        Assert.assertNotNull(generic1);
        Generic<SubImplementation> generic2 = new Generic<SubImplementation>(new SubImplementation());
        Assert.assertNotNull(generic2);
    }


    @Test
    public void testTheBug() {

        //using picocontainer
        DefaultPicoContainer container = new DefaultPicoContainer();
        container.addComponent(Implementation.class);
        container.addComponent(Generic.class);
        Generic result = container.getComponent(Generic.class); // fails here.
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.iface);

    }


    @Test
    public void testTheBug2() {

        DefaultPicoContainer container = new DefaultPicoContainer();
        container.addComponent(SubImplementation.class);
        container.addComponent(Generic.class);
        //should be Generic<SubImplementation> but requires unsafe cast
        Generic<?> result2 = container.getComponent(Generic.class); // fails here
        Assert.assertNotNull(result2);
        Assert.assertNotNull(result2.iface);

    }

}