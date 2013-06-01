package org.picocontainer.injectors;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JSRAccessibleObjectOrderComparatorTestCase {
	
	public static class Base {
		
		public static int one;
		
		public static int two;
		
		public int three;
		
		public static void one() {
			
		}
		
		public static void two() {
			
		}
		
		public  void three() {
			
		}
	}
	
	public static class Derived extends Base {
		
		public static int four;
		
		public int five;
		
		public int six;
		
		public static void four() {
			
		}
		
		public  void five() {
			
		}
		
	}
	
	private Field oneField;
	
	private Field twoField;
	
	private Field threeField;
	
	private Field fourField;
	
	private Field fiveField;
	
	private Field sixField;
	
	private Method oneMethod;
	
	private Method twoMethod;
	
	private Method threeMethod;
	
	private Method fourMethod;
	
	private Method fiveMethod;
	
	private JSRAccessibleObjectOrderComparator comparator;
	

	@Before
	public void setUp() throws Exception {
		oneField = Base.class.getField("one");
		twoField = Base.class.getField("two");
		threeField = Base.class.getField("three");
		fourField = Derived.class.getField("four");
		fiveField = Derived.class.getField("five");
		sixField = Derived.class.getField("six");
		
		oneMethod = Base.class.getMethod("one");
		twoMethod = Base.class.getMethod("two");
		threeMethod = Base.class.getMethod("three");
		fourMethod = Derived.class.getMethod("four");
		fiveMethod = Derived.class.getMethod("five");
		comparator = new JSRAccessibleObjectOrderComparator();
	}

	@Test
	public void testMixingNulls() {
		assertEquals(0, comparator.compare(null, null));
		assertEquals(-1, comparator.compare(null, oneField));
		assertEquals(1, comparator.compare(oneField, null));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMixingDifferentAccessibleObjectTypesCausesIllegalArgumentException() {
		comparator.compare(oneField, oneMethod);
	}
	
	@Test
	public void testIdentityComparison() {
		assertEquals(0, comparator.compare(oneField, oneField));
	}
	
	@Test
	public void testBaseFieldsAreLessThanDerivedFields() {
		assertEquals(-1, comparator.compare(oneField, fourField));
		assertEquals(1, comparator.compare(fiveMethod, twoMethod));
		assertEquals(-1, comparator.compare(oneField,fourField)); //Both statics
	}
	
	@Test
	public void testStaticsOfSameClassAreEqual() {
		assertEquals(0, comparator.compare(oneField, twoField));
		assertEquals(0, comparator.compare(oneMethod, twoMethod));
	}
	
	@Test
	public void testNonStaticsOfSameClassAreEqual() {
		assertEquals(0, comparator.compare(fiveField, sixField));
	}
	
	@Test
	public void testStaticsAreLessThanNonStaticsOfSameClass() {
		assertEquals(-1, comparator.compare(oneField, threeField));
		assertEquals(-1, comparator.compare(oneMethod, threeMethod));
		
		//Fields in reverse order to double-check logic.
		assertEquals(1, comparator.compare(fiveField, fourField));
		assertEquals(1, comparator.compare(fiveMethod, fourMethod));
		
	}

}
