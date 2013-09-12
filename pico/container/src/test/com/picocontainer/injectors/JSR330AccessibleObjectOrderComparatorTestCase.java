package com.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import com.picocontainer.injectors.packageseparatetests.PackageSeparateDerivedTestModel;

import com.picocontainer.injectors.JSR330AccessibleObjectOrderComparator;

public class JSR330AccessibleObjectOrderComparatorTestCase {

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

	private JSR330AccessibleObjectOrderComparator comparator;


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
		comparator = new JSR330AccessibleObjectOrderComparator();
	}

	@Test
	public void testMixingNulls() {
		assertEquals(0, comparator.compare(null, null));
		assertEquals(-1, comparator.compare(null, oneField));
		assertEquals(1, comparator.compare(oneField, null));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testMixingDifferentAccessibleObjectTypesCausesIllegalArgumentException() {
		Constructor<?> defaultConstructor = Derived.class.getConstructors()[0];
		comparator.compare(oneField, defaultConstructor);
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

	@Test
	public void testPackagePrivacyStillDoesntAffectOrdering() throws NoSuchMethodException, SecurityException, NoSuchFieldException {
		Method baseTestSomething = PackageSeparateBaseTestModel.class.getDeclaredMethod("testSomething");
		Method baseTestSomethingElse = PackageSeparateBaseTestModel.class.getDeclaredMethod("testSomethingElse");

		Method derivedInjectSomething = PackageSeparateDerivedTestModel.class.getDeclaredMethod("injectSomething");
		Method derivedTestSomethingElse = PackageSeparateDerivedTestModel.class.getDeclaredMethod("testSomethingElse");

		Field baseAValue = PackageSeparateBaseTestModel.class.getDeclaredField("aValue");
		Field derivedAValue = PackageSeparateDerivedTestModel.class.getDeclaredField("aValue");

		assertEquals(-1, comparator.compare(baseTestSomething, derivedInjectSomething));
		assertEquals(-1, comparator.compare(baseTestSomethingElse, derivedTestSomethingElse));
		assertEquals(-1, comparator.compare(baseTestSomething, derivedTestSomethingElse));
		assertEquals(-1, comparator.compare(baseAValue, derivedAValue));
		assertEquals(1, comparator.compare(baseTestSomething, baseTestSomethingElse));

		List<AccessibleObject> allMembers = new ArrayList<AccessibleObject>();
		allMembers.add(baseTestSomething);
		allMembers.add(baseTestSomethingElse);
		allMembers.add(derivedInjectSomething);
		allMembers.add(derivedTestSomethingElse);
		allMembers.add(baseAValue);
		allMembers.add(derivedAValue);

		Collections.sort(allMembers, comparator);

		assertSame(Arrays.deepToString(allMembers.toArray()),baseAValue, allMembers.get(0));
		assertSame(Arrays.deepToString(allMembers.toArray()),baseTestSomethingElse, allMembers.get(1));
		assertSame(Arrays.deepToString(allMembers.toArray()),baseTestSomething, allMembers.get(2));

		assertSame(Arrays.deepToString(allMembers.toArray()),derivedAValue, allMembers.get(3));
		assertSame(Arrays.deepToString(allMembers.toArray()), derivedTestSomethingElse, allMembers.get(4));
		assertSame(Arrays.deepToString(allMembers.toArray()),derivedInjectSomething, allMembers.get(5));
	}

}
