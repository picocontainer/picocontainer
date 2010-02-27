package org.picocontainer.injectors;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.monitors.NullComponentMonitor;

import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NamedMethodInjectorTestCase {

    public static class Windmill {
        private String wind;
        public void setWind(String eeeeee) { // it is important to note here that 'eeeee' is not going to match any named comp
            this.wind = eeeeee;
        }
    }

    @Test
    public void shouldMatchBasedOnMethodNameIfComponentAvailableAndNonOptional() {
        final String expected = "use this one pico, its key matched the method name (ish)";
        NamedMethodInjector nmi = new NamedMethodInjector(Windmill.class, Windmill.class, Parameter.DEFAULT,
                new NullComponentMonitor(), false);
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
        NamedMethodInjector nmi = new NamedMethodInjector(Windmill.class, Windmill.class, Parameter.DEFAULT,
                new NullComponentMonitor());
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
        NamedMethodInjector nmi = new NamedMethodInjector(Windmill.class, Windmill.class, Parameter.DEFAULT,
                new NullComponentMonitor(), false);
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
        NamedMethodInjector nmi = new NamedMethodInjector(Windmill.class, Windmill.class, Parameter.DEFAULT,
                new NullComponentMonitor(), false);
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
        NamedMethodInjector nmi = new NamedMethodInjector(Windmill.class, Windmill.class, Parameter.DEFAULT,
                new NullComponentMonitor(), true);
        Windmill windmill = new DefaultPicoContainer()
                .addAdapter(nmi)
                .getComponent(Windmill.class);
        assertNotNull(windmill);
        assertNull(windmill.wind);
    }

}
