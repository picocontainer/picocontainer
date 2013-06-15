package org.picocontainer.defaults.issues;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.parameters.CollectionComponentParameter;

import com.googlecode.jtype.Generic;

public class Issue0343TestCase {

    @Test
    public void testRegisteringSubsetOfGenericCollectionParameters() {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Cod.class);
        pico.addComponent(Shark.class);
        pico.addComponent(GenericBowl.class, GenericBowl.class,
                new CollectionComponentParameter(Generic.get(Cod.class), false));

        GenericBowl bowl = pico.getComponent(GenericBowl.class);
        // FAILS with PicoContainer 2.7-SNAPSHOT, returns 2
        assertEquals(1, bowl.fishes.size());
    }

    public static interface Fish {
    }

    public static class Cod implements Fish {
    }

    public static class Shark implements Fish {
    }

    public static class GenericBowl {
        List<Fish> fishes;

        public GenericBowl(final List<Fish> fishes) {
            this.fishes = fishes;
        }
    }

}
