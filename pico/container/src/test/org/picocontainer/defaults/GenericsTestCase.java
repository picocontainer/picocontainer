/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.defaults;

import org.junit.Test;

/**
 * Uncomment all the tests in this class (as well as the obvious places in
 * ConstructorInjectionComponentAdapter) in order to run with generics support
 * Requires JDK 1.5 with generics enabled.
 * 
 * @author Aslak Helles&oslash;y
 */
public class GenericsTestCase {
    @Test public void testDummy() {

    }

    /*
    private MutablePicoContainer pico;
    private Shark shark;
    private Cod cod;
    private Bowl bowl;

    protected void setUp() throws Exception {
        pico = new DefaultPicoContainer();

        shark = new Shark();
        cod = new Cod();

        pico.addAdapter("shark", shark);
        pico.addAdapter(cod);
        pico.addAdapter(Bowl.class);

        bowl = (Bowl) pico.getComponent(Bowl.class);
    }

    public static interface Fish {
    }

    public static class Cod implements Fish{
    }

    public static class Shark implements Fish{
    }

    public static class Bowl {
        private final Collection<Fish> fishes;
        private final Set<Cod> cods;
        private final Map<String, Fish> stringFishMap;
        private final Map<Object, Shark> objectSharkMap;

        public Bowl(Collection<Fish> fishes, Set<Cod> cods, Map<String,Fish> stringFishMap, Map<Object,Shark> objectSharkMap) {
            this.fishes = fishes;
            this.cods = cods;
            this.stringFishMap = stringFishMap;
            this.objectSharkMap = objectSharkMap;
        }

        public Collection<Fish> getFishes() {
            return fishes;
        }

        public Set<Cod> getCods() {
            return cods;
        }

        public Map<String,Fish> getStringFishMap() {
            return stringFishMap;
        }

        public Map<Object, Shark> getObjectSharkMap() {
            return objectSharkMap;
        }
    }

    @Test public void testShouldCreateBowlWithFishCollection() {
        Collection<Fish> fishes = bowl.getFishes();
        assertEquals(2, fishes.size());
        assertTrue(fishes.contains(shark));
        assertTrue(fishes.contains(cod));

        Set<Cod> cods = bowl.getCods();
        assertEquals(1, cods.size());
        assertTrue(cods.contains(cod));
    }

    @Test public void testShouldFilterMapByKeyType() {
        Map<String, Fish> fishMap = bowl.getStringFishMap();
        assertEquals(1, fishMap.size());
        assertSame(shark, fishMap.get("shark"));
    }

    @Test public void testShouldFilterMapByValueType() {
        Map<Object, Shark> fishMap = bowl.getObjectSharkMap();
        assertEquals(1, fishMap.size());
        assertSame(shark, fishMap.get("shark"));
    }

    public static class UngenericCollectionBowl {
        public UngenericCollectionBowl(Collection fish) {
        }
    }

    @Test public void testShouldNotInstantiateCollectionForUngenericCollectionParameters() {
        pico.addAdapter(UngenericCollectionBowl.class);
        try {
            pico.getComponent(UngenericCollectionBowl.class);
            fail();
        } catch (UnsatisfiableDependenciesException e) {
            // expected
        }
    }

    public static class UngenericMapBowl {
        public UngenericMapBowl(Map fish) {
        }
    }

    @Test public void testShouldNotInstantiateMapForUngenericMapParameters() {
        pico.addAdapter(UngenericMapBowl.class);
        try {
            pico.getComponent(UngenericMapBowl.class);
            fail();
        } catch (UnsatisfiableDependenciesException e) {
            // expected
        }
    }

    public static class AnotherGenericCollectionBowl {
        private final Collection<String> strings;

        public AnotherGenericCollectionBowl(Collection<String> strings) {
            this.strings = strings;
        }

        public Collection<String> getStrings() {
            return strings;
        }
    }

    @Test public void testShouldInstantiateAmptyCollectionForAnotherGenericCollection() {
        pico.addAdapter(AnotherGenericCollectionBowl.class);
        AnotherGenericCollectionBowl anotherGenericCollectionBowl = (AnotherGenericCollectionBowl) pico.getComponent(AnotherGenericCollectionBowl.class);
        assertEquals(0, anotherGenericCollectionBowl.getStrings().size());
    }
*/
}