package org.picocontainer;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.junit.Test;
import org.picocontainer.behaviors.AdaptingBehavior;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.MultiInjection;

public class GregoryTestCase {

	@Test
	 public void testComponentCanUseConstructorAndAtInjectWithoutAnInitMethod() {
		
	        final MutablePicoContainer pico = new PicoBuilder(new MultiInjection())
	        		.withBehaviors(new AdaptingBehavior())
	        		.build();
	        pico.addComponent(Carrot.class)
	        	.addComponent(Cheese.class)
	        	.addComponent(Mixer.class);

	        // fails with org.picocontainer.PicoCompositionException: java.lang.NoSuchMethodException: ...$Mixer.<init>()
	        final Utensil u = pico.getComponent(Utensil.class);
	        assertEquals("Mixing carrot with some cheese", u.use());
	    }

	    public static final class Cheese {
	    }

	    public static final class Carrot {
	    }

	    public static interface Utensil {
	        String use();
	    }

	    public static class Spoon implements Utensil {
	        public String use() {
	            return "there is no spoon";
	        }
	    }

	    public static class Mixer implements Utensil {
	        private final Cheese cheese;
	        @Inject
	        private Carrot veg;

	        public Mixer(Cheese cheese) {
	            this.cheese = cheese;
	        }

	        public String use() {
	            return "Mixing " + simpleClassNameOrNull(veg) + " with some " + simpleClassNameOrNull(cheese);
	        }
	    }

	    private static String simpleClassNameOrNull(Object o) {
	        return o == null ? null : o.getClass().getSimpleName().toLowerCase();
	    }

}
