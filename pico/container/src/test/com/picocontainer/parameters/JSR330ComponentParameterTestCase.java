package com.picocontainer.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.containers.JSR330PicoContainer;
import com.picocontainer.injectors.AbstractInjector.AmbiguousComponentResolutionException;
import com.picocontainer.parameters.JSR330ComponentParameter;

public class JSR330ComponentParameterTestCase {


	public static class A {
		public String value;
	}

	public static class B {
		public String value;
	}


	public static class AmbiguousTest {
		public A value;

		public AmbiguousTest(final A value) {
			this.value = value;
		}
	}

	public static class Provider1 implements Provider<A> {
		public A get() {
			A returnValue = new A();
			returnValue.value = "Test";
			return returnValue;
		}
	}

	public static class Provider2 implements Provider<A> {
		public A get() {
			A returnValue = new A();
			returnValue.value = "Test 2";
			return returnValue;
		}
	}

	public static class Provider3 implements Provider<A> {
		public A get() {
			A returnValue = new A();
			returnValue.value = "Test3";
			return returnValue;
		}
	}

	public static class ProviderB implements Provider<B> {

		public B get() {
			B returnValue = new B();
			returnValue.value = "To Bee";
			return returnValue;
		}

	}


	@Test
	public void testHappyPathTrimmingOfMatches() {
		MutablePicoContainer pico = new JSR330PicoContainer(new PicoBuilder().build());
		pico.addComponent(AmbiguousTest.class, AmbiguousTest.class, new JSR330ComponentParameter())
			.addProvider("test", new Provider1()) // <-- Should be skipped
			.addProvider("test2", new Provider2()) // <-- Should be skipped
			.addProvider(new Provider3());  	//<-- Should be chosen

		AmbiguousTest result = pico.getComponent(AmbiguousTest.class);
		assertEquals("Test3", result.value.value);
	}


	@Test(expected=AmbiguousComponentResolutionException.class)
	public void testMoreThanOnePossibleResultAmbiguousComponentResolutionException() {
		MutablePicoContainer pico = new JSR330PicoContainer(new PicoBuilder().build());
		pico.addComponent(AmbiguousTest.class, AmbiguousTest.class, new JSR330ComponentParameter())
			.addProvider("test", new Provider1())
			.addProvider("test2", new Provider2())
			.addProvider("test3", new Provider3());

		AmbiguousTest test = pico.getComponent(AmbiguousTest.class);
		assertNotNull(test);
	}

	public static class MultiProviderBaseTest {

		@Inject
		public Provider<A> theProvider;


	}


	@Test
	public void testSortingByProviderBaseClass() {
		MutablePicoContainer pico = new JSR330PicoContainer(new DefaultPicoContainer());

		Provider2 provider2 = new Provider2();
		pico.addComponent(MultiProviderBaseTest.class)
			.addProvider(new ProviderB())
			.addProvider(provider2);

		MultiProviderBaseTest test =  pico.getComponent(MultiProviderBaseTest.class);
		assertNotNull(test);
		assertTrue(provider2 == test.theProvider);
	}

}
