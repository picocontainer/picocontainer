/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Johan Hoogenboezem (thanks Johan)                        *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;

public class IntoTypeTestCase {

    @Test
    public void testThatIntoSetupCorrectlyForNestedInjectionViaAFactory() throws Exception {
        MutablePicoContainer pico = new DefaultPicoContainer(new Caching());
        pico.addAdapter(new AliceFactory());
        pico.addComponent(Bob.class);
        System.out.println("Going to ask pico for a Bob");
        assertTrue(Bob.class.isAssignableFrom(Bob.class));
        Bob bob = pico.getComponent(Bob.class);
        assertNotNull(bob);
        assertNotNull(bob.getAlice());
    }


    public static interface Alice {
    }


    public static class AliceImpl implements Alice {
    }

    public static class Bob {

        private final Alice alice;

        public Bob(final Alice alice) {
            System.out.println("Bob gets an Alice: " + alice);
            this.alice = alice;
        }

        public Alice getAlice() {
            return alice;
        }

    }


    public static class AliceFactory extends FactoryInjector<Alice> {
        @Override
        public Alice getComponentInstance(final PicoContainer container, final Type into) {
            // System.out.println("Manufacturing an Alice for " + ((InjectInto) into).getIntoClass());
            if (Bob.class.isAssignableFrom(((InjectInto) into).getIntoClass())) {
                return new AliceImpl();
            } else {
                fail("Expected a " + Bob.class + ", but got a " + into + " instead.");
                return null;
            }
        }

    }


}
