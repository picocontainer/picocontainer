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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.ConstructorInjector;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.CollectionComponentParameter;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;

/**
 * @author Aslak Helles&oslash;y
 * @author J&ouml;rg Schaible
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
@RunWith(JMock.class)
public class CollectionComponentParameterTestCase {

	private Mockery mockery = mockeryWithCountingNamingScheme();

	@Test
	public void testShouldInstantiateArrayOfStrings() {
		CollectionComponentParameter ccp = new CollectionComponentParameter();
		final ComponentAdapter forAdapter = mockery
				.mock(ComponentAdapter.class);
		final PicoContainer pico = mockery.mock(PicoContainer.class);
		mockery.checking(new Expectations() {
			{
				atLeast(1).of(forAdapter).getComponentKey();
				will(returnValue("x"));
				one(pico).getComponentAdapters();
				will(returnValue(new HashSet()));
				one(pico).getComponentAdapters(
						with(equal(String.class)));
				will(returnValue(Arrays.asList(new InstanceAdapter("y",
						"Hello", new NullLifecycleStrategy(),
						new NullComponentMonitor()), new InstanceAdapter("z",
						"World", new NullLifecycleStrategy(),
						new NullComponentMonitor()))));
				one(pico).getComponent(with(equal("z")));
				will(returnValue("World"));
				one(pico).getComponent(with(equal("y")));
				will(returnValue("Hello"));
				one(pico).getParent();
				will(returnValue(null));
			}
		});
		List expected = Arrays.asList("Hello", "World");
		Collections.sort(expected);
        Parameter.Resolver resolver = ccp.resolve(pico, forAdapter, null, String[].class, null, false, null);
        List actual = Arrays.asList((Object[]) resolver.resolveInstance());
		Collections.sort(actual);
		assertEquals(expected, actual);
	}

	static public interface Fish {
	}

	static public class Cod implements Fish {
		public String toString() {
			return "Cod";
		}
	}

	static public class Shark implements Fish {
		public String toString() {
			return "Shark";
		}
	}

	static public class Bowl {
		private final Cod[] cods;
		private final Fish[] fishes;

		public Bowl(Cod cods[], Fish fishes[]) {
			this.cods = cods;
			this.fishes = fishes;
		}
	}

	private MutablePicoContainer getDefaultPicoContainer() {
		MutablePicoContainer mpc = new DefaultPicoContainer(new Caching());
		mpc.addComponent(Bowl.class);
		mpc.addComponent(Cod.class);
		mpc.addComponent(Shark.class);
		return mpc;
	}

	@Test
	public void testNativeArrays() {
		MutablePicoContainer mpc = getDefaultPicoContainer();
		Cod cod = mpc.getComponent(Cod.class);
		Bowl bowl = mpc.getComponent(Bowl.class);
		assertEquals(1, bowl.cods.length);
		assertEquals(2, bowl.fishes.length);
		assertSame(cod, bowl.cods[0]);
		assertNotSame(bowl.fishes[0], bowl.fishes[1]);
	}

	@Test
	public void testCollectionsAreGeneratedOnTheFly() {
		MutablePicoContainer mpc = new DefaultPicoContainer();
		mpc.addAdapter(new ConstructorInjector(Bowl.class, Bowl.class,
						null, new NullComponentMonitor(), false));
		mpc.addComponent(Cod.class);
		Bowl bowl = mpc.getComponent(Bowl.class);
		assertEquals(1, bowl.cods.length);
		mpc.addComponent("Nemo", new Cod());
		bowl = mpc.getComponent(Bowl.class);
		assertEquals(2, bowl.cods.length);
		assertNotSame(bowl.cods[0], bowl.cods[1]);
	}

	static public class CollectedBowl {
		private final Cod[] cods;
		private final Fish[] fishes;

		public CollectedBowl(Collection cods, Collection fishes) {
			this.cods = (Cod[]) cods.toArray(new Cod[cods.size()]);
			this.fishes = (Fish[]) fishes.toArray(new Fish[fishes.size()]);
		}
	}

	static public class GenericCollectedBowl extends CollectedBowl {

		public GenericCollectedBowl(Collection<Cod> cods, Collection<Fish> fishes) {
            super(cods, fishes);
        }
	}

	@Test
	public void testCollections() {
		MutablePicoContainer mpc = new DefaultPicoContainer(new Caching());
		mpc.addComponent(CollectedBowl.class, CollectedBowl.class,
				new ComponentParameter(Cod.class, false),
				new ComponentParameter(Fish.class, false));
		mpc.addComponent(Cod.class);
		mpc.addComponent(Shark.class);
		Cod cod = mpc.getComponent(Cod.class);
		CollectedBowl bowl = mpc.getComponent(CollectedBowl.class);
		assertEquals(1, bowl.cods.length);
		assertEquals(2, bowl.fishes.length);
		assertSame(cod, bowl.cods[0]);
		assertNotSame(bowl.fishes[0], bowl.fishes[1]);
	}

	@Test
	public void testGenericCollections() {
		MutablePicoContainer mpc = new DefaultPicoContainer(new Caching());
		mpc.addComponent(GenericCollectedBowl.class);
		mpc.addComponent(Cod.class);
		mpc.addComponent(Shark.class);
		Cod cod = mpc.getComponent(Cod.class);
		CollectedBowl bowl = mpc.getComponent(CollectedBowl.class);
		assertEquals(1, bowl.cods.length);
		assertEquals(2, bowl.fishes.length);
		assertSame(cod, bowl.cods[0]);
		assertNotSame(bowl.fishes[0], bowl.fishes[1]);
	}

	static public class MappedBowl {
		private final Fish[] fishes;

		public MappedBowl(Map map) {
			Collection collection = map.values();
			this.fishes = (Fish[]) collection.toArray(new Fish[collection
					.size()]);
		}
	}

	@Test
	public void testMaps() {
		MutablePicoContainer mpc = new DefaultPicoContainer();
		mpc.addComponent(MappedBowl.class, MappedBowl.class,
				new ComponentParameter(Fish.class, false));
		mpc.addComponent(Cod.class);
		mpc.addComponent(Shark.class);
		MappedBowl bowl = mpc.getComponent(MappedBowl.class);
		assertEquals(2, bowl.fishes.length);
		assertNotSame(bowl.fishes[0], bowl.fishes[1]);
	}

	public static class UngenericCollectionBowl {
		public UngenericCollectionBowl(Collection fish) {
		}
	}

	@Test
	public void testShouldNotInstantiateCollectionForUngenericCollectionParameters() {
		MutablePicoContainer pico = getDefaultPicoContainer();
		pico.addComponent(UngenericCollectionBowl.class);
		try {
			pico.getComponent(UngenericCollectionBowl.class);
			fail();
		} catch (AbstractInjector.UnsatisfiableDependenciesException e) {
			// expected
		}
	}

	public static class AnotherGenericCollectionBowl {
		private final String[] strings;

		public AnotherGenericCollectionBowl(String[] strings) {
			this.strings = strings;
		}

		public String[] getStrings() {
			return strings;
		}
	}

	@Test
	public void testShouldFailWhenThereAreNoComponentsToPutInTheArray() {
		MutablePicoContainer pico = getDefaultPicoContainer();
		pico.addComponent(AnotherGenericCollectionBowl.class);
		try {
			pico.getComponent(AnotherGenericCollectionBowl.class);
			fail();
		} catch (AbstractInjector.UnsatisfiableDependenciesException e) {
			// expected
		}
	}

	@Test
	public void testAllowsEmptyArraysIfEspeciallySet() {
		MutablePicoContainer pico = getDefaultPicoContainer();
		pico.addComponent(AnotherGenericCollectionBowl.class,
				AnotherGenericCollectionBowl.class,
				ComponentParameter.ARRAY_ALLOW_EMPTY);
		AnotherGenericCollectionBowl bowl = pico
				.getComponent(AnotherGenericCollectionBowl.class);
		assertNotNull(bowl);
		assertEquals(0, bowl.strings.length);
	}

	public static class TouchableObserver implements Touchable {
		private final Touchable[] touchables;

		public TouchableObserver(Touchable[] touchables) {
			this.touchables = touchables;

		}

		public void touch() {
			for (Touchable touchable : touchables) {
				touchable.touch();
			}
		}
	}

	@Test
	public void testWillOmitSelfFromCollection() {
		MutablePicoContainer pico = getDefaultPicoContainer();
		pico.addComponent(SimpleTouchable.class);
		pico.addComponent(TouchableObserver.class);
		Touchable observer = pico.getComponent(TouchableObserver.class);
		assertNotNull(observer);
		observer.touch();
		SimpleTouchable touchable = pico.getComponent(SimpleTouchable.class);
		assertTrue(touchable.wasTouched);
	}

	@Test
	public void testWillRemoveComponentsWithMatchingKeyFromParent() {
		MutablePicoContainer parent = new DefaultPicoContainer();
		parent.addComponent("Tom", Cod.class);
		parent.addComponent("Dick", Cod.class);
		parent.addComponent("Harry", Cod.class);
		MutablePicoContainer child = new DefaultPicoContainer(parent);
		child.addComponent("Dick", Shark.class);
		child.addComponent(Bowl.class);
		Bowl bowl = child.getComponent(Bowl.class);
		assertEquals(3, bowl.fishes.length);
		assertEquals(2, bowl.cods.length);
	}

	@Test
	public void testBowlWithoutTom() {
		MutablePicoContainer mpc = new DefaultPicoContainer();
		mpc.addComponent("Tom", Cod.class);
		mpc.addComponent("Dick", Cod.class);
		mpc.addComponent("Harry", Cod.class);
		mpc.addComponent(Shark.class);
		mpc.addComponent(CollectedBowl.class, CollectedBowl.class,
				new CollectionComponentParameter(Cod.class, false) {
					protected boolean evaluate(ComponentAdapter adapter) {
						return !"Tom".equals(adapter.getComponentKey());
					}
				}, new CollectionComponentParameter(Fish.class, false));
		CollectedBowl bowl = mpc.getComponent(CollectedBowl.class);
		Cod tom = (Cod) mpc.getComponent("Tom");
		assertEquals(4, bowl.fishes.length);
		assertEquals(2, bowl.cods.length);
		assertFalse(Arrays.asList(bowl.cods).contains(tom));
	}

	public static class DependsOnAll {
		public DependsOnAll(Set set, SortedSet sortedSet,
				Collection collection, List list, SortedMap sortedMap, Map map
		// , ConcurrentMap concurrentMap, Queue queue, BlockingQueue
		// blockingQueue
		) {
			assertNotNull(set);
			assertNotNull(sortedSet);
			assertNotNull(collection);
			assertNotNull(list);
			assertNotNull(sortedMap);
			assertNotNull(map);
			// assertNotNull(concurrentMap);
			// assertNotNull(queue);
			// assertNotNull(blockingQueue);
		}
	}

	@Test
	public void testDifferentCollectiveTypesAreResolved() {
		MutablePicoContainer pico = new DefaultPicoContainer();
		CollectionComponentParameter parameter = new CollectionComponentParameter(
				Fish.class, true);
		pico.addComponent(DependsOnAll.class, DependsOnAll.class, parameter,
				parameter, parameter, parameter, parameter, parameter);
		assertNotNull(pico.getComponent(DependsOnAll.class));
	}

	@Test
	public void testVerify() {
		MutablePicoContainer pico = new DefaultPicoContainer();
		CollectionComponentParameter parameterNonEmpty = CollectionComponentParameter.ARRAY;
		pico.addComponent(Shark.class);
		parameterNonEmpty.verify(pico, null, Fish[].class, null, false, null);
		try {
			parameterNonEmpty
					.verify(pico, null, Cod[].class, null, false, null);
			fail("(PicoCompositionException expected");
		} catch (PicoCompositionException e) {
			assertTrue(e.getMessage().indexOf(Cod.class.getName()) > 0);
		}
		CollectionComponentParameter parameterEmpty = CollectionComponentParameter.ARRAY_ALLOW_EMPTY;
		parameterEmpty.verify(pico, null, Fish[].class, null, false, null);
		parameterEmpty.verify(pico, null, Cod[].class, null, false, null);
	}

	// PICO-243 : this test will fail if executed on jdk1.3 without
	// commons-collections
	@Test
	public void testOrderOfElementsOfAnArrayDependencyIsPreserved() {
		MutablePicoContainer pico = new DefaultPicoContainer();
		pico.addComponent("first", "first");
		pico.addComponent("second", "second");
		pico.addComponent("third", "third");
		pico.addComponent("fourth", "fourth");
		pico.addComponent("fifth", "fifth");
		pico.addComponent(Truc.class);

		final List strings = pico.getComponents(String.class);
		assertEquals("first", strings.get(0));
		assertEquals("second", strings.get(1));
		assertEquals("third", strings.get(2));
		assertEquals("fourth", strings.get(3));
		assertEquals("fifth", strings.get(4));

		pico.getComponent(Truc.class);
	}

	public static final class Truc {
		public Truc(String[] s) {
			assertEquals("first", s[0]);
			assertEquals("second", s[1]);
			assertEquals("third", s[2]);
			assertEquals("fourth", s[3]);
			assertEquals("fifth", s[4]);
		}
	}

}