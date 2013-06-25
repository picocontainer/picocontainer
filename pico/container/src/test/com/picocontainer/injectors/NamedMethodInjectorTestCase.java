package com.picocontainer.injectors;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.injectors.AbstractInjector;
import com.picocontainer.injectors.NamedMethodInjection;
import com.picocontainer.monitors.NullComponentMonitor;

public class NamedMethodInjectorTestCase {

    public static class Windmill {
        private String wind;
        public void setWind(final String eeeeee) { // it is important to note here that 'eeeee' is not going to match any named comp
            this.wind = eeeeee;
        }
    }

    @Test
    public void shouldMatchBasedOnMethodNameIfComponentAvailableAndNonOptional() {
        final String expected = "use this one pico, its key matched the method name (ish)";
        NamedMethodInjection.NamedMethodInjector nmi = new NamedMethodInjection.NamedMethodInjector(Windmill.class, Windmill.class, new NullComponentMonitor(), false, null
       );
        Windmill windmill = new DefaultPicoContainer()
                .addAdapter(nmi)
                .addConfig("attemptToConfusePicoContainer", "ha ha, confused you")
                .addConfig("wind", expected) // matches setWind(..)
                .addConfig("woo look here another string", "yup, really fooled you this time")
                .getComponent(Windmill.class);
        assertNotNull(windmill);
        assertNotNull(windmill.wind);
        assertEquals(expected, windmill.wind);
    }

    @Test
    public void shouldBeAmbigiousMultipleComponentAvailableOfRightTypeWithoutMatchingName() {
        NamedMethodInjection.NamedMethodInjector nmi = new NamedMethodInjection.NamedMethodInjector(Windmill.class, Windmill.class, new NullComponentMonitor(), null
       );
        try {
            new DefaultPicoContainer()
                    .addAdapter(nmi)
                    .addConfig("attemptToConfusePicoContainer", "ha ha, confused you")
                    .addConfig("woo look here another", "yup, really fooled you this time")
                    .getComponent(Windmill.class);
            fail("should have barfed");
        } catch (AbstractInjector.AmbiguousComponentResolutionException e) {
            // expected
        }
    }

    @Test
    public void shouldBeUnsatisfiedIfNoComponentAvailableOfTheRightTypeAndNonOptional() {
        NamedMethodInjection.NamedMethodInjector nmi = new NamedMethodInjection.NamedMethodInjector(Windmill.class, Windmill.class, new NullComponentMonitor(), false, null
       );
        try {
            new DefaultPicoContainer()
                    .addAdapter(nmi)
                    .addConfig("attemptToConfusePicoContainer", 123)
                    .addConfig("woo look here another", 456)
                    .getComponent(Windmill.class);
            fail("should have barfed");
        } catch (AbstractInjector.UnsatisfiableDependenciesException e) {
            // expected
        }
    }

    @Test
    public void withoutNameMatchWillBeOKTooIfOnlyOneOfRightTypeAndNonOptional() {
        NamedMethodInjection.NamedMethodInjector nmi = new NamedMethodInjection.NamedMethodInjector(Windmill.class, Windmill.class, new NullComponentMonitor(), false, null
       );
        Windmill windmill = new DefaultPicoContainer()
                .addAdapter(nmi)
                .addConfig("anything", "hello")
                .getComponent(Windmill.class);
        assertNotNull(windmill);
        assertNotNull(windmill.wind);
        assertEquals("hello", windmill.wind);
    }

    @Test
    public void withoutNameMatchWillBeOKTooIfNoneOfRightTypeAndOptional() {
        NamedMethodInjection.NamedMethodInjector nmi = new NamedMethodInjection.NamedMethodInjector(Windmill.class, Windmill.class, new NullComponentMonitor(), true, null
       );
        Windmill windmill = new DefaultPicoContainer()
                .addAdapter(nmi)
                .getComponent(Windmill.class);
        assertNotNull(windmill);
        assertNull(windmill.wind);
    }

}
