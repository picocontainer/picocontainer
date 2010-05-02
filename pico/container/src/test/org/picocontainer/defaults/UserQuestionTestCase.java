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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Type;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.AbstractAdapter;
import org.picocontainer.injectors.ConstructorInjection;

/**
 * This class can be used to test out various things asked on the mailing list.
 * Or to answer questions.
 *
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("serial")
public final class UserQuestionTestCase {

    // From Scott Farquahsr
    public static final class CheeseAdapter<T extends Cheese> extends AbstractAdapter<T> {
        private final Map<String,?> bla;

        public CheeseAdapter(Object key, Class<T> impl, Map<String,?> cheeseMap) throws PicoCompositionException {
            super(key, impl);
            this.bla = cheeseMap;
        }

        @SuppressWarnings("unchecked")
        public T getComponentInstance(PicoContainer pico, Type into) throws PicoCompositionException {
            return (T) bla.get("cheese");
        }

        public void verify(PicoContainer pico) {
        }

        public String getDescriptor() {
            return null;
        }
    }

    public static interface Cheese {
        String getName();
    }

    public static class Gouda implements Cheese {
        public String getName() {
            return "Gouda";
        }
    }

    public static class Roquefort implements Cheese {
        public String getName() {
            return "Roquefort";
        }
    }

    public static class Omelette {
        private final Cheese cheese;

        public Omelette(Cheese cheese) {
            this.cheese = cheese;
        }

        public Cheese getCheese() {
            return cheese;
        }
    }

    @Test public void testOmeletteCanHaveDifferentCheeseWithAFunnyComponentAdapter() {
        Map<String,Cheese> cheeseMap = new HashMap<String,Cheese>();

        MutablePicoContainer pico = new DefaultPicoContainer(new ConstructorInjection());
        pico.addComponent(Omelette.class);
        pico.addAdapter(new CheeseAdapter<Gouda>("scott", Gouda.class, cheeseMap));

        Cheese gouda = new Gouda();
        cheeseMap.put("cheese", gouda);
        Omelette goudaOmelette = pico.getComponent(Omelette.class);
        assertSame(gouda, goudaOmelette.getCheese());

        Cheese roquefort = new Roquefort();
        cheeseMap.put("cheese", roquefort);
        Omelette roquefortOmelette = pico.getComponent(Omelette.class);
        assertSame(roquefort, roquefortOmelette.getCheese());
    }

    public static interface InterfaceX {
        String getIt();
    }

    public static class Enabled implements InterfaceX {
        public String getIt() {
            return "Enabled";
        }
    }

    public static class Disabled implements InterfaceX {
        public String getIt() {
            return "Disabled";
        }
    }

    @SuppressWarnings("unchecked")
    public static class Something implements InterfaceX {
        private final Disabled disabled;
        private final Enabled enabled;
        private final Map map;

        public Something(Disabled disabled, Enabled enabled, Map map) {
            this.disabled = disabled;
            this.enabled = enabled;
            this.map = map;
        }

        public String getIt() {
            if (map.get("enabled") == null) {
                return disabled.getIt();
            } else {
                return enabled.getIt();
            }
        }
    }

    public static class NeedsInterfaceX {
        private final InterfaceX interfaceX;

        public NeedsInterfaceX(InterfaceX interfaceX) {
            this.interfaceX = interfaceX;
        }

        public String getIt() {
            return interfaceX.getIt();
        }
    }

    @Test public void testMoreWeirdness() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        Map<String,String> map = new HashMap<String,String>();
        pico.addComponent(map);
        // See class level javadoc in DefaultPicoContainer - about precedence. 
        pico.addComponent(InterfaceX.class, Something.class);
        pico.addComponent(Disabled.class);
        pico.addComponent(Enabled.class);
        pico.addComponent(NeedsInterfaceX.class);

        NeedsInterfaceX needsInterfaceX = pico.getComponent(NeedsInterfaceX.class);
        assertEquals("Disabled", needsInterfaceX.getIt());
        map.put("enabled", "blah");
        assertEquals("Enabled", needsInterfaceX.getIt());
    }

    // From John Tal 23/03/2004
    public static interface ABC {
    }

    public static interface DEF {
    }

    public static class ABCImpl implements ABC {
        public ABCImpl(DEF def) {
        }
    }

    public static class DEFImpl implements DEF {
        public DEFImpl() {
        }
    }

    @Test public void testJohnTalOne() {
        MutablePicoContainer picoContainer = new DefaultPicoContainer();

        picoContainer.addComponent("ABC", ABCImpl.class);
        picoContainer.addComponent("DEF", DEFImpl.class);

        assertEquals(ABCImpl.class, picoContainer.getComponent("ABC").getClass());
    }

    public static interface Foo {
    }

    public static interface Bar {
    }

    public static class FooBar implements Foo, Bar {
    }

    public static class NeedsFoo {
        private final Foo foo;

        public NeedsFoo(Foo foo) {
            this.foo = foo;
        }

        public Foo getFoo() {
            return foo;
        }
    }

    public static class NeedsBar {
        private final Bar bar;

        public NeedsBar(Bar bar) {
            this.bar = bar;
        }

        public Bar getBar() {
            return bar;
        }
    }

    @Test public void testShouldBeAbleShareSameReferenceForDifferentTypes() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.as(Characteristics.CACHE).addComponent(FooBar.class);
        pico.addComponent(NeedsFoo.class);
        pico.addComponent(NeedsBar.class);
        NeedsFoo needsFoo = pico.getComponent(NeedsFoo.class);
        NeedsBar needsBar = pico.getComponent(NeedsBar.class);
        assertSame(needsFoo.getFoo(), needsBar.getBar());
    }

    @Test public void testSeveralDifferentInstancesCanBeCreatedWithOnePreconfiguredContainer() {
        // create a container that doesn't cache instances
        MutablePicoContainer container = new DefaultPicoContainer(new ConstructorInjection());
        container.addComponent(NeedsBar.class);

        Bar barOne = new FooBar();
        container.addComponent(Bar.class, barOne);
        NeedsBar needsBarOne = container.getComponent(NeedsBar.class);
        assertSame(barOne, needsBarOne.getBar());

        // reuse the same container - just flip out the existing foo.
        Bar barTwo = new FooBar();
        container.removeComponent(Bar.class);
        container.addComponent(Bar.class, barTwo);
        NeedsBar needsBarTwo = container.getComponent(NeedsBar.class);
        assertSame(barTwo, needsBarTwo.getBar());

        assertNotSame(needsBarOne, needsBarTwo);
    }
}