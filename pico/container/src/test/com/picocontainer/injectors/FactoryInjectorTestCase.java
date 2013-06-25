/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.lang.reflect.Type;
import java.util.Map;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.annotations.Inject;
import com.picocontainer.injectors.FactoryInjector;
import com.picocontainer.injectors.InjectInto;
import com.picocontainer.injectors.MultiInjection;

public class FactoryInjectorTestCase {

    public static interface Swede {
    }

    @Test
    public void brendansNullTypeCase() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(BrendansComponent.class);
        pico.addAdapter(new BrendansLoggerInjector());

        BrendansComponent bc = pico.getComponent(BrendansComponent.class);
        assertEquals("com.picocontainer.injectors.InjectInto", bc.logger.canonicalName);
    }

    public static class BrendansLogger {
        private final String canonicalName;

        public BrendansLogger(final String canonicalName) {
            this.canonicalName = canonicalName;
        }

        public static BrendansLogger getLogger(final String canonicalName) {
            return new BrendansLogger(canonicalName);
        }
    }

    public static class BrendansComponent {
        BrendansLogger logger;

        public BrendansComponent(final BrendansLogger logger) {
            this.logger = logger;
        }
    }

    public static class BrendansLoggerInjector extends FactoryInjector<BrendansLogger> {

        @Override
        public BrendansLogger getComponentInstance(final PicoContainer arg0, final Type arg1)
                throws PicoCompositionException {
            return BrendansLogger.getLogger(arg1.getClass().getCanonicalName());
        }

    }


    public static class Turnip2 {
        Swede swede;
        private final String foo;
        public Turnip2(final String foo, final Swede swede) {
            this.foo = foo;
            assertNotNull(swede);
            this.swede = swede;
        }
        public Swede getSwede() {
            return swede;
        }

        public String getFoo() {
            return foo;
        }
    }

    public static class Turnip {
        @Inject
        Swede swede;
        private final String foo;

        public Turnip(final String foo) {
            this.foo = foo;
        }

        public Swede getSwede() {
            return swede;
        }

        public String getFoo() {
            return foo;
        }
    }

    @Test
    public void testThatComponentCanHaveAProvidedDependency() {
        MutablePicoContainer container = new DefaultPicoContainer(new MultiInjection());
        container.addComponent(String.class, "foo");
        container.addComponent(Turnip.class);
        container.addAdapter(new SwedeFactoryInjector());
        Turnip t = container.getComponent(Turnip.class);
        assertNotNull(t);
        assertEquals("Swede for " + Turnip.class.getName(), t.getSwede().toString());
        assertEquals("foo", t.getFoo());

    }

    @Test
    public void testThatComponentCanHaveAProvidedDependencyWithInlinedFactoryInjector() {
        MutablePicoContainer container = new DefaultPicoContainer(new MultiInjection());
        container.addComponent(String.class, "foo");
        container.addComponent(Turnip.class);
        container.addAdapter(new FactoryInjector<Swede>() {
            @Override
			public Swede getComponentInstance(final PicoContainer container, final Type into) {
                return new Swede() {
                    @Override
					public String toString() {
                        return "Swede for " + ((InjectInto) into).getIntoClass().getName();
                    }
                };
            }
        });
        Turnip t = container.getComponent(Turnip.class);
        assertNotNull(t);
        assertEquals("Swede for " + Turnip.class.getName(), t.getSwede().toString());
        assertEquals("foo", t.getFoo());

    }

    @Test
    public void testThatComponentCanHaveAProvidedDependencyWithInlinedFactoryInjector2() {
        MutablePicoContainer container = new DefaultPicoContainer(new MultiInjection());
        container.addComponent(String.class, "foo");
        container.addComponent(Turnip.class);
        container.addAdapter(new FactoryInjector<Swede>(Swede.class) {
            @Override
			public Swede getComponentInstance(final PicoContainer container, final Type into) {
                return new Swede() {
                    @Override
					public String toString() {
                        return "Swede for " + ((InjectInto) into).getIntoClass().getName();
                    }
                };
            }
        });
        Turnip t = container.getComponent(Turnip.class);
        assertNotNull(t);
        assertEquals("Swede for " + Turnip.class.getName(), t.getSwede().toString());
        assertEquals("foo", t.getFoo());

    }

    @Test
    public void testThatComponentCanHaveAProvidedDependencyWithInlinedFactoryInjector3() {
        MutablePicoContainer container = new DefaultPicoContainer(new MultiInjection());
        container.addComponent(String.class, "foo");
        container.addComponent(Turnip.class);
        container.addAdapter(new FactoryInjector<Swede>(Swede.class) {
            @Override
			public Swede getComponentInstance(final PicoContainer container, final Type into) {
                return new Swede() {
                    @Override
					public String toString() {
                        return "Swede for " + ((InjectInto) into).getIntoClass().getName();
                    }
                };
            }
        });
        Turnip t = container.getComponent(Turnip.class);
        assertNotNull(t);
        assertEquals("Swede for " + Turnip.class.getName(), t.getSwede().toString());
        assertEquals("foo", t.getFoo());

    }


    @Test
    public void testThatComponentCanHaveAProvidedDependencyViaConstructor() {
        MutablePicoContainer container = new DefaultPicoContainer();
        container.addComponent(String.class, "foo");
        container.addComponent(Turnip2.class);
        container.addAdapter(new SwedeFactoryInjector());
        Turnip2 t = container.getComponent(Turnip2.class);
        assertNotNull(t);
        assertEquals("Swede for " + Turnip2.class.getName(), t.getSwede().toString());
        assertEquals("foo", t.getFoo());

    }

    @Test
    public void testThatComponentCanHaveAProvidedDependencyViaConstructorADifferentWay() {
        MutablePicoContainer container = new DefaultPicoContainer();
        container.addComponent(String.class, "foo");
        container.addComponent(Turnip2.class);
        container.addAdapter(new Swede2FactoryInjector()); // this injector defines Swede2 as key in its ctor
        Turnip2 t = container.getComponent(Turnip2.class);
        assertNotNull(t);
        assertEquals("Swede for " + Turnip2.class.getName(), t.getSwede().toString());
        assertEquals("foo", t.getFoo());

    }

    private static class SwedeFactoryInjector extends FactoryInjector<Swede> {
        @Override
		public Swede getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            // Mauro: you can do anything in here by way of startegy for injecting a specific logger :-)
            return new Swede() {
                @Override
				public String toString() {
                    return "Swede for " + ((InjectInto) into).getIntoClass().getName();
                }
            };
        }
    }

    private static class Swede2FactoryInjector extends FactoryInjector {
        private Swede2FactoryInjector() {
            super(Swede.class);
        }

        @Override
		public Swede getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            // Mauro: you can do anything in here by way of startegy for injecting a specific logger :-)
            return new Swede() {
                @Override
				public String toString() {
                    return "Swede for " + ((InjectInto) into).getIntoClass().getName();
                }
            };
        }
    }

    private abstract class Footle<T> {
        private class ServiceConnectionInjector extends FactoryInjector<T> {
            @Override
			public T getComponentInstance(final PicoContainer container, final Type into) {
                System.out.println("**** injector called for " + into);
                return null;
            }
        }

        private void addAdapter(final MutablePicoContainer mpc) {
            mpc.addAdapter(new ServiceConnectionInjector());
        }
    }

    public static interface Tree {
        String leafColor();
    }
    public static class OakTree implements Tree {
        private final String leafColor;

        public OakTree(final String leafColor) {
            this.leafColor = leafColor;
        }

        public String leafColor() {
            return leafColor;
        }
    }

    @Test public void ensureSophistcatedFactorInjectorCaseIsPossible() {

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addConfig("leafColor", "green");
        pico.addComponent(Tree.class, OakTree.class);

        Footle<Map> ft = new Footle<Map>() {};

        ft.addAdapter(pico);

        Tree tree = pico.getComponent(Tree.class);
        assertNotNull(tree);
    }

    private static class KeyAwareSwedeFactoryInjector extends FactoryInjector<Swede> {

    	@Override
		public Swede getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            return new Swede() {
                @Override
				public String toString() {
                    InjectInto intoType = (InjectInto) into;
                    return "Swede for " + intoType.getIntoClass().getName() + " " + intoType.getIntoKey();
                }
            };
        }
    }

    @Test
    public void testThatFactoryCanUseTargetComponentKey() {
        MutablePicoContainer container = new DefaultPicoContainer(new MultiInjection());
        container.addComponent(String.class, "foo");
        container.addComponent("turnip1", Turnip.class);
        container.addComponent("turnip2", Turnip.class);
        container.addAdapter(new KeyAwareSwedeFactoryInjector());
        Turnip turnip1 = (Turnip)container.getComponent("turnip1");
        Turnip turnip2 = (Turnip)container.getComponent("turnip2");
        assertNotNull(turnip1);
        assertNotNull(turnip2);
        assertNotSame(turnip1, turnip2);
        assertNotSame(turnip1.getSwede(), turnip2.getSwede());
        assertEquals("Swede for " + Turnip.class.getName() + " turnip1", turnip1.getSwede().toString());
        assertEquals("Swede for " + Turnip.class.getName() + " turnip2", turnip2.getSwede().toString());
    }
}
