package org.picocontainer.injectors;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StaticsInitializedReferenceSetTestCase {

	public static class TestClass {
		public static String staticField;
		
		public static void staticMethod() {
			
		}
		
		public String nonStaticField;
		
		public void nonStaticMethod() {
			
		}
	}
	
	private Field staticFieldRef;
	
	private Field nonStaticFieldRef;
	
	private Method staticMethodRef;
	
	private Method nonStaticMethodRef;
	
	private StaticsInitializedReferenceSet referenceSet;
	
	@Before
	public void setUp() throws NoSuchFieldException, NoSuchMethodException {
		referenceSet = new StaticsInitializedReferenceSet();
		staticFieldRef = TestClass.class.getField("staticField");
		nonStaticFieldRef = TestClass.class.getField("nonStaticField");
		staticMethodRef = TestClass.class.getMethod("staticMethod");
		nonStaticMethodRef = TestClass.class.getMethod("nonStaticMethod");
	}

	
	@Test
	public void testReferenceSetWorksForStaticFields() {
		assertFalse(referenceSet.isMemberAlreadyInitialized(staticFieldRef));
		referenceSet.markMemberInitialized(staticFieldRef);
		assertTrue(referenceSet.isMemberAlreadyInitialized(staticFieldRef));
	}
	
	@Test
	public void testReferenceSetWorksForStaticMethods() {
		assertFalse(referenceSet.isMemberAlreadyInitialized(staticMethodRef));
		referenceSet.markMemberInitialized(staticMethodRef);
		assertTrue(referenceSet.isMemberAlreadyInitialized(staticMethodRef));
	}
	
	@Test
	public void testReferenceSetThrowsNullPointerExceptionWithProperErrorMessages() {
		try {
			referenceSet.markMemberInitialized(null);
		} catch (NullPointerException e) {
			assertEquals("member", e.getMessage());
		}

	
		try {
			referenceSet.isMemberAlreadyInitialized(null);
		} catch (NullPointerException e) {
			assertEquals("member", e.getMessage());
		}
	}
	
	@Test
	public void testReferenceSetThrowsIllegalArgumentExceptionIfMemberIsntStatic() {
		try {
			referenceSet.markMemberInitialized(nonStaticFieldRef);
			fail("Should havethrown exception");
		} catch (IllegalArgumentException e) {
			
		}
		
		try {
			referenceSet.markMemberInitialized(nonStaticMethodRef);
			fail("Should havethrown exception");
		} catch (IllegalArgumentException e) {
			
		}
		
		
	}

}
