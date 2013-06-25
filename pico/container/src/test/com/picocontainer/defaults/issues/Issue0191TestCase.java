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

import static org.junit.Assert.fail;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.injectors.AbstractInjector;

public final class Issue0191TestCase {

    static int sharkCount = 0 ;
    static int codCount = 0 ;

    /*
      This bug as descripbed in the bug report, cannot be reproduced. Needs work.
    */
    @Test public void testTheBug()
    {
        MutablePicoContainer pico = new DefaultPicoContainer() ;
        pico.addComponent(Shark.class);
        pico.addComponent(Cod.class);
        try {
            pico.addComponent(Bowl.class);
            Bowl bowl = pico.getComponent(Bowl.class);
            fail("Should have barfed here with UnsatisfiableDependenciesException");
            Fish[] fishes = bowl.getFishes();
            for(int i = 0 ; i < fishes.length ; i++) {
				System.out.println("fish["+i+"]="+fishes[i]);
			}
        } catch (AbstractInjector.UnsatisfiableDependenciesException e) {
            // expected, well except that there is supposed to be a different bug here.
        }
    }


     class Bowl
    {
        private final Fish[] fishes;
        private final Cod[] cods;
        public Bowl(final Fish[] fishes, final Cod[] cods)
        {
            this.fishes = fishes;
            this.cods = cods;
        }
        public Fish[] getFishes()
        {
            return fishes;
        }
        public Cod[] getCods()
        {
            return cods;
        }

    }

    public interface Fish
    {
    }

    final class Cod implements Fish
    {
        final int instanceNum ;
        public Cod() { instanceNum = codCount++ ; }

        @Override
		public String toString() {
            return "Cod #" + instanceNum ;
        }
    }

    final class Shark implements Fish
    {
        final int instanceNum ;
        public Shark() { instanceNum = sharkCount++ ; }

        @Override
		public String toString() {
            return "Shark #" + instanceNum ;
        }
    }

}
