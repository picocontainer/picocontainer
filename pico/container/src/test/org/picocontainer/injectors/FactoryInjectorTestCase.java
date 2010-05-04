/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.annotations.Inject;

import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class FactoryInjectorTestCase {

    public static interface Swede {
    }

    public static class Turnip2 {
        Swede swede;
        private final String foo;
        public Turnip2(String foo, Swede swede) {
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

        public Turnip(String foo) {
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
            public Swede getComponentInstance(PicoContainer container, final Type into) {
                return new Swede() {
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
            public Swede getComponentInstance(PicoContainer container, final Type into) {
                return new Swede() {
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
            public Swede getComponentInstance(PicoContainer container, final Type into) {
                return new Swede() {
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
        public Swede getComponentInstance(PicoContainer container, final Type into) throws PicoCompositionException {
            // Mauro: you can do anything in here by way of startegy for injecting a specific logger :-)
            return new Swede() {
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

        public Swede getComponentInstance(PicoContainer container, final Type into) throws PicoCompositionException {
            // Mauro: you can do anything in here by way of startegy for injecting a specific logger :-)
            return new Swede() {
                public String toString() {
                    return "Swede for " + ((InjectInto) into).getIntoClass().getName();
                }
            };
        }
    }

    private abstract class Footle<T> {
        private class ServiceConnectionInjector extends FactoryInjector<T> {
            public T getComponentInstance(PicoContainer container, Type into) {
                System.out.println("**** injector called for " + into);
                return null;
            }
        }

        private void addAdapter(MutablePicoContainer mpc) {
            mpc.addAdapter(new ServiceConnectionInjector());
        }
    }

    public static interface Tree {
        String leafColor();
    }
    public static class OakTree implements Tree {
        private String leafColor;

        public OakTree(String leafColor) {
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

        Footle<Map> ft = new Footle<Map>(){};

        ft.addAdapter(pico);

        Tree tree = pico.getComponent(Tree.class);
        assertNotNull(tree);
    }

    private static class KeyAwareSwedeFactoryInjector extends FactoryInjector<Swede> {

    	public Swede getComponentInstance(PicoContainer container, final Type into) throws PicoCompositionException {
            return new Swede() {
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
