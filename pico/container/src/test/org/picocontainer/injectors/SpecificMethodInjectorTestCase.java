package org.picocontainer.injectors;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.injectors.SpecificFieldInjectorTestCase.TestInjection;

public class SpecificMethodInjectorTestCase {
	
	public static class TestModel {
		
		public static int staticInjectCount;
		
		public int injectCount;
		
		public static void injectSomething() {
			staticInjectCount++;
		}
		
		public void injectSomethingElse() {
			injectCount++;
		}
		
	}

	private Method staticInjectMethod = null;
	
	private Method injectSomethingElse = null;
	
	@Before
	public void setUp() throws Exception {
		TestModel.staticInjectCount = 0;
		staticInjectMethod = TestModel.class.getDeclaredMethod("injectSomething");
		injectSomethingElse = TestModel.class.getDeclaredMethod("injectSomethingElse");
	}

	@After
	public void tearDown() throws Exception {
		TestModel.staticInjectCount = 0;
	}

	
	@Test(expected=PicoCompositionException.class)
	public void testCallingInjectStaticsWithNonStaticFieldsThrowsCompositionException() {
		SpecificMethodInjector<TestModel> adapter = new SpecificMethodInjector<TestModel>(TestModel.class, TestModel.class, injectSomethingElse);
		adapter.injectStatics(null, null, null);
	}
	
	
	@Test(expected=PicoCompositionException.class)
	public void testCallingGetComponetInstanceWithStaticFieldsThrowsCompositionException() {
		SpecificMethodInjector<TestModel> adapter = new SpecificMethodInjector<TestModel>(TestModel.class, TestModel.class, staticInjectMethod);
		adapter.getComponentInstance(null, null);
	}	
	
	@Test
	public void testStaticMethodInjection() {
		SpecificMethodInjector<TestModel> adapter = new SpecificMethodInjector<TestModel>(TestModel.class, TestModel.class, staticInjectMethod);
		adapter.injectStatics(null, null, null);

		assertEquals(1, TestModel.staticInjectCount);
	}
	
	@Test
	public void testNormalMethodInjection() {
		SpecificMethodInjector<TestModel> adapter = new SpecificMethodInjector<TestModel>(TestModel.class, TestModel.class, injectSomethingElse);
		TestModel model = adapter.getComponentInstance(null, null);
		assertNotNull(model);
		
		assertEquals(0, TestModel.staticInjectCount);
		assertEquals(1, model.injectCount);
	}
	
	@Test
	public void testStaticInjectionWithReferenceHandlerMakesSureStaticsAreOnlyinitializedOnce() {
		StaticsInitializedReferenceSet referenceSet = new StaticsInitializedReferenceSet();
		SpecificMethodInjector<TestModel> adapter = new SpecificMethodInjector<TestModel>(TestModel.class, TestModel.class, staticInjectMethod);
		adapter.injectStatics(null, null, referenceSet);
		assertEquals(1, TestModel.staticInjectCount);

		adapter.injectStatics(null, null, referenceSet);
		assertEquals(1, TestModel.staticInjectCount);
	}

	
}
