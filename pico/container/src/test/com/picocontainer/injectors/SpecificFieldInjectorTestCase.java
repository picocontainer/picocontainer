package com.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.injectors.SpecificFieldInjector;
import com.picocontainer.injectors.StaticsInitializedReferenceSet;

public class SpecificFieldInjectorTestCase {


	public static class TestInjection {

		public static String something;

		public String somethingElse;

	}

	private Field somethingField = null;

	private Field somethingElseField = null;

	@Before
	public void setupReflectionFields() throws NoSuchFieldException{
		somethingField = TestInjection.class.getDeclaredField("something");
		 somethingElseField = TestInjection.class.getDeclaredField("somethingElse");
	}

	@Test
	public void testStaticInjection() throws NoSuchFieldException {
		MutablePicoContainer pico = new DefaultPicoContainer().addComponent(String.class,"Testing");

		SpecificFieldInjector<TestInjection> adapter = new SpecificFieldInjector<TestInjection>(TestInjection.class, TestInjection.class, somethingField);
		adapter.injectStatics(pico, null, null);

		assertEquals("Testing", TestInjection.something);
	}


	@Test
	public void testNonStaticInjection() throws NoSuchFieldException {
		TestInjection.something = null;
		MutablePicoContainer pico = new DefaultPicoContainer().addComponent(String.class,"Testing");

		SpecificFieldInjector<TestInjection> adapter = new SpecificFieldInjector<TestInjection>(TestInjection.class, TestInjection.class, somethingElseField);
		TestInjection ti = adapter.getComponentInstance(pico, null);
		assertNotNull(ti);
		assertNull(TestInjection.something);
		assertEquals("Testing", ti.somethingElse);
	}

	@Test(expected=PicoCompositionException.class)
	public void testMixingStaticAndNotStaticFieldsResultsInPicoCompositionException() {
		new SpecificFieldInjector<TestInjection>(TestInjection.class, TestInjection.class, somethingField, somethingElseField);
	}

	@Test(expected=PicoCompositionException.class)
	public void testCallingInjectStaticsWithNonStaticFieldsThrowsCompositionException() {
		SpecificFieldInjector<TestInjection> adapter = new SpecificFieldInjector<TestInjection>(TestInjection.class, TestInjection.class, somethingElseField);
		adapter.injectStatics(null, null, null);
	}


	@Test(expected=PicoCompositionException.class)
	public void testCallingGetComponetInstanceWithStaticFieldsThrowsCompositionException() {
		SpecificFieldInjector<TestInjection> adapter = new SpecificFieldInjector<TestInjection>(TestInjection.class, TestInjection.class, somethingField);
		adapter.getComponentInstance(null, null);
	}

	@Test
	public void testStaticInjectionWithReferenceHandlerMakesSureStaticsAreOnlyinitializedOnce() {
		//Do a dummy initialization to prove that
		//we're setting at least once.
		TestInjection.something = "Do-Da";
		MutablePicoContainer pico = new DefaultPicoContainer().addComponent(String.class,"Testing");
		StaticsInitializedReferenceSet referenceSet = new StaticsInitializedReferenceSet();
		SpecificFieldInjector<TestInjection> adapter = new SpecificFieldInjector<TestInjection>(TestInjection.class, TestInjection.class, somethingField);

		adapter.injectStatics(pico, null, referenceSet);
		assertEquals("Testing", TestInjection.something);

		//Injection shouldn't overwrite this since its been initialized once already.
		TestInjection.something = "Do-Da";

		adapter.injectStatics(pico, null, referenceSet);
		assertEquals("Do-Da", TestInjection.something);
	}
}
