package com.picocontainer.injectors;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.picocontainer.Characteristics;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.injectors.AnnotatedStaticInjection;
import com.picocontainer.injectors.ConstructorInjection;

public class AnnotatedStaticInjectionTestClass {

	public static class TestModel {

		@Inject
		public static String something;

		public static boolean injectCalled = false;

		@Inject
		public static void inject() {
			if (injectCalled) {
				fail("Static Method Injection called twice");
			}

			if (something == null) {
				fail("Static Field not called before static method injection");
			}

			injectCalled = true;
		}

		public TestModel() {
			assertTrue(something != null);
			assertTrue(injectCalled);
		}
	}


	public static class TestDerived extends TestModel {

		@Inject
		public static String somethingElse;

		public static boolean derivedInjectCalled = false;

		@Inject
		public static void injectSomethingElse() {
			derivedInjectCalled = true;
		}


		public TestDerived() {
			assertTrue(TestModel.something != null);
			assertTrue(TestModel.injectCalled);
			assertTrue(somethingElse != null);
			assertTrue(derivedInjectCalled);
		}

	}

	private DefaultPicoContainer pico;

	@Before
	public void setUp() throws Exception {
		TestModel.something = null;
		TestModel.injectCalled = false;
		TestDerived.somethingElse = null;
		TestDerived.derivedInjectCalled = false;

		pico = new DefaultPicoContainer(new AnnotatedStaticInjection(), new ConstructorInjection());
	}

	@After
	public void tearDown() throws Exception {
		pico = null;
		TestModel.something = null;
		TestModel.injectCalled = false;
		TestDerived.somethingElse = null;
		TestDerived.derivedInjectCalled = false;
	}

	/**
	 * Main assertions about order of initialization
	 * are in the test model itself.
	 */
	@Test
	public void testStaticInjection() {
		pico.as(Characteristics.STATIC_INJECTION).addComponent(TestModel.class)
			.as(Characteristics.STATIC_INJECTION).addComponent(TestDerived.class)
			.addComponent(String.class, "Testing");

		TestDerived derived = pico.getComponent(TestDerived.class);
		TestDerived derived2 = pico.getComponent(TestDerived.class);

		//Caching should be turned off.
		assertNotSame(derived, derived2);

		TestModel base = pico.getComponent(TestModel.class);
		TestModel base2 = pico.getComponent(TestModel.class);

		assertNotSame(base, base2);

	}

}
