/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/
package com.picocontainer;

import static com.picocontainer.Characteristics.CDI;
import static com.picocontainer.Characteristics.SDI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.io.File;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Provider;

import org.junit.Ignore;
import org.junit.Test;

import com.picocontainer.tck.AbstractPicoContainerTest;
import com.picocontainer.tck.AbstractPicoContainerTest.ErrorProne;
import com.picocontainer.testmodel.DecoratedTouchable;
import com.picocontainer.testmodel.DependsOnTouchable;
import com.picocontainer.testmodel.SimpleTouchable;
import com.picocontainer.testmodel.Touchable;
import com.googlecode.jtype.Generic;
import com.picocontainer.ChangedBehavior;
import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.Injector;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;
import com.picocontainer.Startable;
import com.picocontainer.behaviors.AdaptingBehavior;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.injectors.AbstractInjector;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.lifecycle.StartableLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.monitors.WriterComponentMonitor;
import com.picocontainer.parameters.ConstantParameter;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

/**
 * @author Aslak Helles&oslashp;y
 * @author Paul Hammant
 * @author Ward Cunningham
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public final class DefaultPicoContainerTestCase extends AbstractPicoContainerTest {

	@Override
	protected MutablePicoContainer createPicoContainer(final PicoContainer parent) {
		return new DefaultPicoContainer(parent, new StartableLifecycleStrategy(new NullComponentMonitor()), new AdaptingBehavior() );
	}

	@Override
	protected Properties[] getProperties() {
		return new Properties[0];
	}

	@Test public void testInstantiationWithNullComponentFactory() {
		try {
			new DefaultPicoContainer((PicoContainer) null, (ComponentFactory) null);
			fail("NPE expected");
		} catch (NullPointerException e) {
			// expected
		}
	}

	@Test public void testUpDownDependenciesCannotBeFollowed() {
		MutablePicoContainer parent = createPicoContainer(null);
		parent.setName("parent");
		MutablePicoContainer child = createPicoContainer(parent);
		child.setName("child");

		// ComponentF -> ComponentA -> ComponentB+C
		child.addComponent(ComponentF.class);
		parent.addComponent(ComponentA.class);
		child.addComponent(new ComponentB());
		child.addComponent(new ComponentC());

		try {
			Object f = child.getComponent(ComponentF.class);
			fail("Thrown "
					+ AbstractInjector.UnsatisfiableDependenciesException.class
							.getName() + " expected");
		} catch (final AbstractInjector.UnsatisfiableDependenciesException e) {
			assertEquals("A has unsatisfied dependency 'class C' for constructor 'public A(B,C)' from parent:1<|",
					e.getMessage().replace("com.picocontainer.tck.AbstractPicoContainerTest$Component",""));		}

    }

	@Test public void testComponentsCanBeRemovedByInstance() {
		MutablePicoContainer pico = createPicoContainer(null);
		pico.addComponent(HashMap.class);
		pico.addComponent(ArrayList.class);
		List list = pico.getComponent(List.class);
		pico.removeComponentByInstance(list);
		assertEquals(1, pico.getComponentAdapters().size());
		assertEquals(1, pico.getComponents().size());
		assertEquals(HashMap.class, pico.getComponent(Serializable.class)
				.getClass());
	}

	@Test public void testComponentInstancesListIsReturnedForNullType() {
		MutablePicoContainer pico = createPicoContainer(null);
		List componentInstances = pico.getComponents(null);
		assertNotNull(componentInstances);
		assertEquals(0, componentInstances.size());
	}

	@Test public void testComponentsWithCommonSupertypeWhichIsAConstructorArgumentCanBeLookedUpByConcreteType() {
		MutablePicoContainer pico = createPicoContainer(null);
		pico.addComponent(LinkedList.class, LinkedList.class, Parameter.ZERO);
		pico.addComponent(ArrayList.class, ArrayList.class, Parameter.ZERO);
		assertEquals(ArrayList.class, pico
				.getComponent((Class) ArrayList.class).getClass());
	}


	/*
	 * When pico tries to resolve DecoratedTouchable it find as dependency
	 * itself and SimpleTouchable. Problem is basically the same as above. Pico
	 * should not consider self as solution.
	 *
	 * JS fixed it (PICO-222 ) KP
	 */
	@Test public void testUnambiguouSelfDependency() {
		MutablePicoContainer pico = createPicoContainer(null);
		pico.addComponent(SimpleTouchable.class);
		pico.addComponent(DecoratedTouchable.class);
		Touchable t = (Touchable) pico
				.getComponent((Object) DecoratedTouchable.class);
		assertNotNull(t);
	}

	@Test public void testPicoUsedInBuilderStyle() {
		MutablePicoContainer pico = createPicoContainer(null);
		Touchable t = pico.change(Characteristics.CACHE).addComponent(
				SimpleTouchable.class).addComponent(DecoratedTouchable.class)
				.getComponent(DecoratedTouchable.class);
		SimpleTouchable t2 = pico.getComponent(SimpleTouchable.class);
		assertNotNull(t);
		assertNotNull(t2);
		t.touch();
		assertTrue(t2.wasTouched);
	}

	public static class Thingie {
		public Thingie(final List c) {
			assertNotNull(c);
		}
	}

	@Test public void testThangCanBeInstantiatedWithArrayList() {
		MutablePicoContainer pico = new DefaultPicoContainer();
		pico.addComponent(Thingie.class);
		pico.addComponent(ArrayList.class);
		assertNotNull(pico.getComponent(Thingie.class));
	}

	@Test public void testGetComponentAdaptersOfTypeNullReturnsEmptyList() {
		DefaultPicoContainer pico = new DefaultPicoContainer();
		List adapters = pico.getComponentAdapters((Generic)null);
		assertNotNull(adapters);
		assertEquals(0, adapters.size());
	}

	public static class Service {
	}

	public static final class TransientComponent {
		private final Service service;

		public TransientComponent(final Service service) {
			this.service = service;
		}
	}

	@Test public void testDefaultPicoContainerReturnsNewInstanceForEachCallWhenUsingTransientComponentAdapter() {

		DefaultPicoContainer picoContainer = new DefaultPicoContainer(
				new Caching().wrap(new ConstructorInjection()));

		picoContainer.addComponent(Service.class);
		picoContainer.as(Characteristics.NO_CACHE).addAdapter(new ConstructorInjection.ConstructorInjector(TransientComponent.class,
						TransientComponent.class));
		TransientComponent c1 = picoContainer
				.getComponent(TransientComponent.class);
		TransientComponent c2 = picoContainer
				.getComponent(TransientComponent.class);
		assertNotSame(c1, c2);
		assertSame(c1.service, c2.service);
	}

	public static class DependsOnCollection {
		public DependsOnCollection(final Collection c) {
		}
	}

	@Test public void testShouldProvideInfoAboutDependingWhenAmbiguityHappens() {
		MutablePicoContainer pico = this.createPicoContainer(null);
		pico.addComponent(new ArrayList());
		pico.addComponent(new LinkedList());
		pico.addComponent(DependsOnCollection.class);
		try {
			pico.getComponent(DependsOnCollection.class);
			fail();
		} catch (AbstractInjector.AmbiguousComponentResolutionException expected) {
			String doc = DependsOnCollection.class.getName();
			assertEquals(
					"class "
							+ doc
							+ " needs a 'java.util.Collection' injected into parameter #0 (zero based index) of constructor 'public com.picocontainer.DefaultPicoContainerTestCase$DependsOnCollection(java.util.Collection)', but there are too many choices to inject. These:[class java.util.ArrayList, class java.util.LinkedList], refer http://picocontainer.org/ambiguous-injectable-help.html",
					expected.getMessage());
		}
	}

	@Test public void testInstantiationWithMonitorAndParent() {
		StringWriter writer = new StringWriter();
		ComponentMonitor monitor = new WriterComponentMonitor(writer);
		DefaultPicoContainer parent = new DefaultPicoContainer();
		DefaultPicoContainer child = new DefaultPicoContainer(parent, monitor);
		parent.addComponent("st", SimpleTouchable.class);
		child.addComponent("dot", DependsOnTouchable.class);
		DependsOnTouchable dot = (DependsOnTouchable) child.getComponent("dot");
		assertNotNull(dot);
		assertTrue("writer not empty", writer.toString().length() > 0);

    }

    @Test
    public void testRepresentationOfContainerTree() {
        StringWriter writer = new StringWriter();
        DefaultPicoContainer parent = new DefaultPicoContainer();
        parent.setName("parent");
        DefaultPicoContainer child = new DefaultPicoContainer(parent);
        child.setName("child");
        parent.addComponent("st", SimpleTouchable.class);
        child.addComponent("dot", DependsOnTouchable.class);
        assertEquals("child:1<[Immutable]:parent:1<|", child.toString());
    }

    @SuppressWarnings("serial")
	@Test public void testStartCapturedByMonitor() {
		final StringBuffer sb = new StringBuffer();
		DefaultPicoContainer dpc = new DefaultPicoContainer(
				new NullComponentMonitor() {
					@Override
					public Object invoking(final PicoContainer container,
                                           final ComponentAdapter componentAdapter, final Member member,
                                           final Object instance, final Object... args) {
						sb.append(member.toString());
                        return null;
                    }
				});
		dpc.as(Characteristics.CACHE).addComponent(DefaultPicoContainer.class);
		dpc.start();
		assertEquals(
				"ComponentMonitor should have been notified that the component had been started",
				"public abstract void com.picocontainer.Startable.start()", sb
						.toString());
	}

	public static class StartableClazz implements Startable {
		private MutablePicoContainer _pico;

		public void start() {
			List<SimpleTouchable> cps = _pico
					.getComponents(SimpleTouchable.class);
			assertNotNull(cps);
		}

		public void stop() {
		}

	}

	@Test public void testListComponentsOnStart() {

		// This is really discouraged. Breaks basic principals of IoC -
		// components should not refer
		// to their containers
		//
		// Might be deleted in due coure, along with adaptersClone stuff in DPC

		DefaultPicoContainer dpc = new DefaultPicoContainer();
		dpc.addComponent(SimpleTouchable.class);
		StartableClazz cl = new StartableClazz();
		cl._pico = dpc;
		dpc.addComponent(cl);
		dpc.start();
	}

	@Test public void testCanChangeMonitor() {
		StringWriter writer1 = new StringWriter();
		ComponentMonitor monitor1 = new WriterComponentMonitor(writer1);
		DefaultPicoContainer pico = new DefaultPicoContainer(monitor1);
		pico.addComponent("t1", SimpleTouchable.class);
		pico.addComponent("t3", SimpleTouchable.class);
		Touchable t1 = (Touchable) pico.getComponent("t1");
		assertNotNull(t1);
		final String s = writer1.toString();
		assertTrue("writer not empty", s.length() > 0);
		StringWriter writer2 = new StringWriter();

		ComponentMonitor monitor2 = new WriterComponentMonitor(writer2);
		pico.changeMonitor(monitor2);
		pico.addComponent("t2", SimpleTouchable.class);
		Touchable t2 = (Touchable) pico.getComponent("t2");
		assertNotNull(t2);
		final String s2 = writer2.toString();
		assertTrue("writer not empty", s2.length() > 0);
		assertEquals("writers should be of same length",
				writer1.toString().length(), writer2.toString().length());
		Touchable t3 = (Touchable) pico.getComponent("t3");
		assertNotNull(t3);
		assertTrue("old writer was used", writer1.toString().length() < writer2
				.toString().length());
	}

	@Test public void testCanChangeMonitorOfChildContainers() {
		StringWriter writer1 = new StringWriter();
		ComponentMonitor monitor1 = new WriterComponentMonitor(writer1);
		DefaultPicoContainer parent = new DefaultPicoContainer();
		DefaultPicoContainer child = new DefaultPicoContainer(monitor1);
		parent.addChildContainer(child);
		child.addComponent("t1", SimpleTouchable.class);
		child.addComponent("t3", SimpleTouchable.class);
		Touchable t1 = (Touchable) child.getComponent("t1");
		assertNotNull(t1);
		assertTrue("writer not empty", writer1.toString().length() > 0);
		StringWriter writer2 = new StringWriter();
		ComponentMonitor monitor2 = new WriterComponentMonitor(writer2);
		parent.changeMonitor(monitor2);
		child.addComponent("t2", SimpleTouchable.class);
		Touchable t2 = (Touchable) child.getComponent("t2");
		assertNotNull(t2);
		assertTrue("writer not empty", writer2.toString().length() > 0);
		String s1 = writer1.toString();
		String s2 = writer2.toString();
		assertTrue("writers of same length", s1.length() == s2.length());
		Touchable t3 = (Touchable) child.getComponent("t3");
		assertNotNull(t3);
		assertTrue("old writer was used", writer1.toString().length() < writer2
				.toString().length());
	}

	@Test public void testChangeMonitorIsIgnoredIfNotSupportingStrategy() {
		StringWriter writer = new StringWriter();
		ComponentMonitor monitor = new WriterComponentMonitor(writer);
		DefaultPicoContainer parent = new DefaultPicoContainer(
				new ComponentFactoryWithNoMonitor(
						new ComponentAdapterWithNoMonitor(new SimpleTouchable())));
		parent.addChildContainer(new EmptyPicoContainer());
		parent.addComponent("t1", SimpleTouchable.class);
		parent.changeMonitor(monitor);
		assertTrue("writer empty", writer.toString().length() == 0);
	}

	@Test public void testCanReturnCurrentMonitorFromComponentFactory() {
		StringWriter writer1 = new StringWriter();
		ComponentMonitor monitor1 = new WriterComponentMonitor(writer1);
		DefaultPicoContainer pico = new DefaultPicoContainer(monitor1);
		assertEquals(monitor1, pico.currentMonitor());
		StringWriter writer2 = new StringWriter();
		ComponentMonitor monitor2 = new WriterComponentMonitor(writer2);
		pico.changeMonitor(monitor2);
		assertEquals(monitor2, pico.currentMonitor());
	}

	private static final class ComponentFactoryWithNoMonitor implements ComponentFactory {
		private final ComponentAdapter adapter;

		public ComponentFactoryWithNoMonitor(final ComponentAdapter adapter) {
			this.adapter = adapter;
		}

		public ComponentAdapter createComponentAdapter(
				final ComponentMonitor monitor,
				final LifecycleStrategy lifecycle,
				final Properties componentProps, final Object key,
				final Class impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams)
				throws PicoCompositionException {
			return adapter;
		}

        public void verify(final PicoContainer container) {
        }

        public void accept(final PicoVisitor visitor) {
            visitor.visitComponentFactory(this);
        }

		public void dispose() {
			
		}
    }

	private static final class ComponentAdapterWithNoMonitor implements
			ComponentAdapter {
		private final Object instance;

		public ComponentAdapterWithNoMonitor(final Object instance) {
			this.instance = instance;
		}

		public Object getComponentKey() {
			return instance.getClass();
		}

		public Class getComponentImplementation() {
			return instance.getClass();
		}

        public Object getComponentInstance(final PicoContainer container, final Type into)
				throws PicoCompositionException {
			return instance;
		}

		public void verify(final PicoContainer container)
				throws PicoCompositionException {
		}

		public void accept(final PicoVisitor visitor) {
        }

		public ComponentAdapter getDelegate() {
			return null;
		}

		public ComponentAdapter findAdapterOfType(final Class adapterType) {
			return null;
		}

		public String getDescriptor() {
			return null;
		}

	}

	@Test public void testMakeChildContainer() {
		MutablePicoContainer parent = new DefaultPicoContainer();
		parent.addComponent("t1", SimpleTouchable.class);
		MutablePicoContainer child = parent.makeChildContainer();
		Object t1 = child.getParent().getComponent("t1");
		assertNotNull(t1);
		assertTrue(t1 instanceof SimpleTouchable);
	}

    @Test public void testMakeChildContainerPassesMonitorFromParentToChild() {
        final StringBuilder sb = new StringBuilder();
        ComponentMonitor cm = new NullComponentMonitor() {
            @Override
			public <T> void instantiated(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                              final Constructor<T> constructor,
                              final Object instantiated,
                              final Object[] injected,
                              final long duration) {
                sb.append(instantiated.getClass().getName()).append(",");
            }

        };
        MutablePicoContainer parent = new DefaultPicoContainer(cm);
        MutablePicoContainer child = parent.makeChildContainer();
        child.addComponent("t1", SimpleTouchable.class);
        Object t1 = child.getComponent("t1");
        assertNotNull(t1);
        assertTrue(t1 instanceof SimpleTouchable);
        assertEquals("com.picocontainer.testmodel.SimpleTouchable,", sb.toString());
    }



	@Test public void testCanUseCustomLifecycleStrategyForClassRegistrations() {
		DefaultPicoContainer dpc = new DefaultPicoContainer(
                null, new FailingLifecycleStrategy());
		dpc.as(Characteristics.CACHE).addComponent(Startable.class,
				MyStartable.class);
		try {
			dpc.start();
			fail("should have barfed");
		} catch (RuntimeException e) {
			assertEquals("foo", e.getMessage());
		}
	}

	@Test public void testCanUseCustomLifecycleStrategyForInstanceRegistrations() {
		DefaultPicoContainer dpc = new DefaultPicoContainer(
                null, new FailingLifecycleStrategy());
		Startable myStartable = new MyStartable();
		dpc.addComponent(Startable.class, myStartable);
		try {
			dpc.start();
			fail("should have barfed");
		} catch (RuntimeException e) {
			assertEquals("foo", e.getMessage());
		}
	}

	public static class FailingLifecycleStrategy implements LifecycleStrategy {
		public void start(final Object component) {
			throw new RuntimeException("foo");
		}

		public void stop(final Object component) {
		}

		public void dispose(final Object component) {
		}

		public boolean hasLifecycle(final Class type) {
			return true;
		}

        public boolean isLazy(final ComponentAdapter<?> adapter) {
            return false;
        }
    }

	public static class MyStartable implements Startable {
		public MyStartable() {
		}

		public void start() {
		}

		public void stop() {
		}
	}

	public static interface A {

	}

	public static class SimpleA implements A {

	}

	public static class WrappingA implements A {
		private final A wrapped;

		public WrappingA(final A wrapped) {
			this.wrapped = wrapped;
		}
	}

	@Test public void testCanRegisterTwoComponentsImplementingSameInterfaceOneWithInterfaceAsKey()
			throws Exception {
		MutablePicoContainer container = createPicoContainer(null);

		container.addComponent(SimpleA.class);
		container.addComponent(A.class, WrappingA.class);

		container.start();

		assertEquals(WrappingA.class, container.getComponent(A.class)
				.getClass());
	}

	@Test public void testCanRegisterTwoComponentsWithSameImplementionAndDifferentKey()
			throws Exception {
		MutablePicoContainer container = createPicoContainer(null);

		container.addComponent(SimpleA.class);
		container.addComponent("A", SimpleA.class);

		container.start();

		assertNotNull(container.getComponent("A"));
		assertNotNull(container.getComponent(SimpleA.class));
		assertNotSame(container.getComponent("A"), container
				.getComponent(SimpleA.class));
	}

	@Test public void testPicoCanDifferentiateBetweenNamedStringsThatWouldOtherwiseBeAmbiguous() {
		MutablePicoContainer mpc = createPicoContainer(null);
		mpc.addComponent("greeting", "1");
		mpc.addComponent("message", "2");
		mpc.as(Characteristics.USE_NAMES).addComponent(
				PicoCompositionException.class, PicoCompositionException.class);
		assertEquals("2", mpc.getComponent(PicoCompositionException.class)
				.getMessage());
	}

	@Test public void testPicoCanDifferentiateBetweenNamedObjectsThatWouldOtherwiseBeAmbiguous() {
		MutablePicoContainer mpc = createPicoContainer(null);
		Horse dobbin = new Horse();
		Horse redRum = new Horse();
		mpc.addComponent("dobbin", dobbin);
		mpc.addComponent("horse", redRum);
		mpc.as(Characteristics.USE_NAMES).addComponent(CdiTurtle.class);
		assertEquals(redRum, mpc.getComponent(CdiTurtle.class).horse);
	}

	@Test public void testPicoCanDifferentiateBetweenNamedIntsThatWouldOtherwiseBeAmbiguous() {
		MutablePicoContainer mpc = createPicoContainer(null);
		mpc.addComponent("one", 1);
		mpc.addComponent("two", 2);
		mpc.as(Characteristics.USE_NAMES).addComponent(NeedsTwo.class);
		assertEquals(2, mpc.getComponent(NeedsTwo.class).two);
	}

	public static class ListComponentsInStartClass implements Startable {
		private MutablePicoContainer _pico;

		public void start() {
			List<SimpleTouchable> cps = _pico
					.getComponents(SimpleTouchable.class);
			assertNotNull(cps);
		}

		public void stop() {
		}

	}

	/**
	 * JIRA: PICO-295 reported by Erik Putrycz
	 */
	@Test public void testListComponentsInStart() {
		DefaultPicoContainer dpc = new DefaultPicoContainer();
		dpc.addComponent(SimpleTouchable.class);
		ListComponentsInStartClass cl = new ListComponentsInStartClass();
		cl._pico = dpc;
		dpc.addComponent(cl);
		dpc.start();
	}

	public static class NeedsTwo {
		private final int two;

		public NeedsTwo(final Integer two) {
			this.two = two;
		}
	}

	public static class Horse {
	}

	public static class CdiTurtle {
		public final Horse horse;

		public CdiTurtle(final Horse horse) {
			this.horse = horse;
		}
	}

	public static class SdiDonkey {
		public Horse horse;

		public void setHorse(final Horse horse) {
			this.horse = horse;
		}
	}

	public static class SdiRabbit {
		public Horse horse;

		public void setHorse(final Horse horse) {
			this.horse = horse;
		}
	}

	@Test public void testMixingOfSDIandCDI() {

		MutablePicoContainer container = createPicoContainer(null).change(
				Characteristics.CACHE);
		container.addComponent(Horse.class);
		container.change(SDI);
		container.addComponent(SdiDonkey.class);
		container.addComponent(SdiRabbit.class);
		container.change(CDI);
		container.addComponent(CdiTurtle.class);

		SdiDonkey donkey = container.getComponent(SdiDonkey.class);
		SdiRabbit rabbit = container.getComponent(SdiRabbit.class);
		CdiTurtle turtle = container.getComponent(CdiTurtle.class);

		assertions(donkey, rabbit, turtle);
	}

	@Test public void testMixingOfSDIandCDIDifferently() {

		MutablePicoContainer container = createPicoContainer(null).change(
				Characteristics.CACHE);
		container.addComponent(Horse.class);
		container.addComponent(CdiTurtle.class);
		container.change(SDI);
		container.addComponent(SdiDonkey.class);
		container.addComponent(SdiRabbit.class);

		SdiDonkey donkey = container.getComponent(SdiDonkey.class);
		SdiRabbit rabbit = container.getComponent(SdiRabbit.class);
		CdiTurtle turtle = container.getComponent(CdiTurtle.class);

		assertions(donkey, rabbit, turtle);
	}

	@Test public void testMixingOfSDIandCDIInBuilderStyle() {

		MutablePicoContainer container = createPicoContainer(null).change(
				Characteristics.CACHE);
		container.addComponent(Horse.class).change(SDI).addComponent(
				SdiDonkey.class).addComponent(SdiRabbit.class).change(CDI)
				.addComponent(CdiTurtle.class);

		SdiDonkey donkey = container.getComponent(SdiDonkey.class);
		SdiRabbit rabbit = container.getComponent(SdiRabbit.class);
		CdiTurtle turtle = container.getComponent(CdiTurtle.class);

		assertions(donkey, rabbit, turtle);
	}

	private void assertions(final SdiDonkey donkey, final SdiRabbit rabbit, final CdiTurtle turtle) {
		assertNotNull(rabbit);
		assertNotNull(donkey);
		assertNotNull(turtle);
		assertNotNull(turtle.horse);
		assertNotNull(donkey.horse);
		assertNotNull(rabbit.horse);
		assertSame(donkey.horse, turtle.horse);
		assertSame(rabbit.horse, turtle.horse);
	}

	@Test public void testMixingOfSDIandCDIWithTemporaryCharacterizations() {

		MutablePicoContainer container = createPicoContainer(null).change(
				Characteristics.CACHE);
		container.addComponent(Horse.class);
		container.addComponent(CdiTurtle.class);
		container.as(SDI).addComponent(SdiDonkey.class);
		container.as(SDI).addComponent(SdiRabbit.class);

		SdiDonkey donkey = container.getComponent(SdiDonkey.class);
		SdiRabbit rabbit = container.getComponent(SdiRabbit.class);
		CdiTurtle turtle = container.getComponent(CdiTurtle.class);

		assertions(donkey, rabbit, turtle);
	}

	@Test public void testMixingOfSDIandCDIWithTemporaryCharacterizationsDifferently() {

		MutablePicoContainer container = createPicoContainer(null).change(
				Characteristics.CACHE);
		container.as(SDI).addComponent(SdiDonkey.class);
		container.as(SDI).addComponent(SdiRabbit.class);
		container.addComponent(Horse.class);
		container.addComponent(CdiTurtle.class);

		SdiDonkey donkey = container.getComponent(SdiDonkey.class);
		SdiRabbit rabbit = container.getComponent(SdiRabbit.class);
		CdiTurtle turtle = container.getComponent(CdiTurtle.class);

		assertions(donkey, rabbit, turtle);
	}

	@Test public void testChainingOfTemporaryCharacterizationsIsNotAllowed() {

		MutablePicoContainer container = createPicoContainer(null);
        try {
            container.as(Characteristics.CACHE).as(SDI).addComponent(HashMap.class);
            fail("shoulf barf");
        } catch (PicoCompositionException e) {
            assertTrue(e.getMessage().contains("as(FOO).as(BAR)"));
        }
    }

    public static class NeedsString {
        String string;

        public NeedsString(final String string) {
            this.string = string;
        }
    }

    @SuppressWarnings("serial")
	@Test public void testNoComponentIsMonitoredAndPotentiallyLateProvided() {
		final Class[] missingKey = new Class[1];

        DefaultPicoContainer container = new DefaultPicoContainer(
                new NullComponentMonitor() {
                    @Override
					public Object noComponentFound(
                            final MutablePicoContainer container, final Object key) {
                        missingKey[0] = (Class) key;
                        return "foo";
                    }
                });
        container.addComponent(NeedsString.class);
        NeedsString needsString = container.getComponent(NeedsString.class);

		assertNotNull(missingKey[0]);
		assertEquals(String.class, missingKey[0]);
		assertNotNull(needsString);
		assertEquals("foo", needsString.string);

	}

	@Test public void testThatComponentCannotBeRemovedFromStartedContainer() {
		MutablePicoContainer container = createPicoContainer(null);
		container.addComponent(Map.class, HashMap.class);
		container.start();
		try {
			container.removeComponent(Map.class);
			fail("should have barfed");
		} catch (PicoCompositionException e) {
		}
	}

	@Test public void testThatSimpleStringComponentIsAddedOnlyOnce() {
		MutablePicoContainer container = createPicoContainer(null);
		container.addComponent("foo bar");
		assertEquals(1, container.getComponentAdapters().size());
	}

    public static class ConstantParameterTestClass {
    	public ConstantParameterTestClass(final Class<String> type) {
    		assert type != null;
    	}
    }


    @Test
    public void testConstantParameterReferenceClass() {
    	MutablePicoContainer container = createPicoContainer(null);
    	container.addComponent(ConstantParameterTestClass.class, ConstantParameterTestClass.class, new ConstantParameter(String.class));

    	assertNotNull(container.getComponent(ConstantParameterTestClass.class));

    }


    @Test public void canInterceptImplementationViaNewInjectionFactoryMethodOnMonitor() {
        DefaultPicoContainer dpc = new DefaultPicoContainer(new MyNullComponentMonitor());
        dpc.addComponent(Collection.class, HashSet.class);
        dpc.addComponent(List.class, ArrayList.class);
        assertNotNull(dpc.getComponent(List.class));
        assertEquals("doppleganger", dpc.getComponent(List.class).get(0));
    }

    @SuppressWarnings({"serial", "unchecked"})
    private static class MyNullComponentMonitor extends NullComponentMonitor {
		@Override
		public Injector newInjector(final Injector injector) {
            if (injector.getComponentKey() == List.class) {
                return new AbstractInjector(List.class, ArrayList.class, MyNullComponentMonitor.this, false) {
                    @Override
					public Object getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
                        ArrayList list = new ArrayList();
                        list.add("doppleganger");
                        return list;
                    }
                };
            } else {
                return injector;
            }
        }

        @Override
		public ChangedBehavior changedBehavior(final ChangedBehavior changedBehavior) {
            return changedBehavior;
        }
    }

    @Test public void testVarargsComponentFactories() {
        MutablePicoContainer pico = new DefaultPicoContainer(new Caching(), new ConstructorInjection());
        pico.addComponent(List.class, ArrayList.class);
        assertEquals("Cached:LifecycleAdapter:ConstructorInjector-interface java.util.List", pico.getComponentAdapter(List.class).toString());
    }

    @Test public void testMessedUpVarargsComponentFactories() {
        try {
            new DefaultPicoContainer(new ConstructorInjection(), new Caching());
            fail("should have barfed");
        } catch (PicoCompositionException e) {
            assertEquals("Check the order of the BehaviorFactories in the varargs list of ComponentFactories. " +
                    "Index 0 (com.picocontainer.injectors.ConstructorInjection) should be a BehaviorFactory but is not.", e.getMessage());
        }
    }

    public static class NeedsColorProvider {
        private final Provider<Color> colorProvider;
        public NeedsColorProvider(final Provider<Color> colorProvider) {
            this.colorProvider = colorProvider;
        }
    }

    @Test public void testJsr330ProviderAsConstructorArgument() {
        MutablePicoContainer pico = new DefaultPicoContainer(new Caching(), new ConstructorInjection());
        Provider<Color> provider = new Provider<Color>() {
            public Color get() {
                return Color.red;
            }
        };
        pico
        	.addProvider(provider)
        	.addComponent(NeedsColorProvider.class);

        NeedsColorProvider ncp = pico.getComponent(NeedsColorProvider.class);
        assertSame(provider, ncp.colorProvider);
        assertSame(Color.red, ncp.colorProvider.get());
    }


    public static class NeedsColorProviderTwo {
    	public Color color;
    	public NeedsColorProviderTwo(final Color theColor) {
    		color = theColor;
    	}
    }
    @Test
    public void testJsr330ProviderWithCicaCallingGetMethod() {
        MutablePicoContainer pico = new DefaultPicoContainer(new Caching(), new ConstructorInjection());
        Provider<Color> provider = new Provider<Color>() {
            public Color get() {
                return Color.green;
            }
        };

        pico.addProvider(provider).addComponent(NeedsColorProviderTwo.class);
        NeedsColorProviderTwo ncp = pico.getComponent(NeedsColorProviderTwo.class);
        assertEquals(Color.green, ncp.color);
    }

    @Override
	@Test public void testUnsatisfiableDependenciesExceptionGivesVerboseEnoughErrorMessage() {
        super.testUnsatisfiableDependenciesExceptionGivesVerboseEnoughErrorMessage();
    }


    /**
     * @todo Total Wish list:  Would be nice to be able to completely operate independent of JSR-330's jar.
     */
    @Test
    @Ignore
    public void testDefaultPicoContainerWillFunctionWithoutJavaxInjectInItsClasspath() throws Exception {
    	File srcPath = new File("target/classes");
    	assertTrue(srcPath.exists());


    	URLClassLoader cl = new URLClassLoader(new URL[] {srcPath.toURI().toURL()}, System.class.getClassLoader());

    	try {
	    	try {
		    	cl.loadClass("javax.inject.Inject");
		    	fail("javax.inject.Inject is still in the classpath");
	    	} catch (ClassNotFoundException e) {
	    		//a-ok
	    		assertNotNull(e.getMessage());
	    	}


	    	Class<?> defaultPico = cl.loadClass("com.picocontainer.containers.JSR330PicoContainer");

	    	Method addComponent = defaultPico.getMethod("addComponent", Object.class);
	    	Method getComponent = defaultPico.getMethod("getComponent", Object.class);

	    	//Will use
	    	Object picoInstance = defaultPico.newInstance();

	    	Object picoResult = addComponent.invoke(picoInstance, StringBuilder.class);
	    	assertNotNull(picoResult);

	    	StringBuilder result = (StringBuilder) getComponent.invoke(picoInstance, StringBuilder.class);

	    	assertNotNull(result);
    	} finally {
    		try {
				Method closeMethod = cl.getClass().getMethod("close");
				closeMethod.invoke(cl);
			} catch (Exception e) {
				//ignore, close doesn't exist.
			}
    	}


    }

	@Test
	public void testNoMatterWhatHappensInChildContainersThatLifecycleStateIsSetProperly() {
		MutablePicoContainer mpc = new PicoBuilder().withCaching().withLifecycle().build();
		mpc.addComponent(ErrorProne.class);
		
		mpc.start();
		
		try {
			mpc.stop();
			fail("Error should have been thrown by component");
		} catch (PicoLifecycleException e) {
			assertNotNull(e.getMessage());
		}
		
		assertTrue(mpc.getLifecycleState().isStopped());
		
		try {
			mpc.dispose();
		} catch (PicoLifecycleException e) {
			assertNotNull(e.getMessage());
		}
		
		assertTrue(mpc.getLifecycleState().isDisposed());
	}    

}