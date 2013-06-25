/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Michael Rimov                                            *
 *****************************************************************************/
package com.picocontainer.gems.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class DelegateMethodTestCase {

	@Test
	@SuppressWarnings("rawtypes")
	public void testVoidReturnType() {
		DelegateMethod<Map, Void> method = new DelegateMethod<Map, Void>(
				Map.class, "clear");
		HashMap<String, String> testMap = new HashMap<String, String>();
		testMap.put("a", "A");
		method.invoke(testMap);
		assertEquals(0, testMap.size());
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testTypeMatching() {
		HashMap<String, String> testMap = new HashMap<String, String>();
		DelegateMethod<Map, String> method = new DelegateMethod<Map, String>(
				Map.class, "put", "A", "A Value");

		assertNull(method.invoke(testMap));
		assertEquals("A Value", method.invoke(testMap));

		assertEquals("A Value", testMap.get("A"));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testPrimitiveTypeMatching() {
		HashMap<Integer, Integer> testMap = new HashMap<Integer, Integer>();
		DelegateMethod<Map, Integer> method = new DelegateMethod<Map, Integer>(
				Map.class, "put", 1, 3);

		assertNull(method.invoke(testMap));
		assertEquals(3, method.invoke(testMap).intValue());

		assertEquals(3, testMap.get(1).intValue());
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testNullTypeMatching() {
		HashMap<Integer, Integer> testMap = new HashMap<Integer, Integer>();
		DelegateMethod<Map, Integer> method = new DelegateMethod<Map, Integer>(
				Map.class, "put", 1, null);

		assertNull(method.invoke(testMap));
		assertNull(testMap.get(1));

	}

	@Test
	public void testStaticInvocation() {
		long startTime = System.currentTimeMillis();
		DelegateMethod<System, Long> method = new DelegateMethod<System, Long>(
				System.class, "currentTimeMillis");

		long result = method.invoke();
		assertTrue(result >= startTime);
	}

	@Test
	public void testMethodMispellingsAreCaught() {
		try {
			DelegateMethod<System, Long> method = new DelegateMethod<System, Long>(
					System.class, "currentTimeMills");
			fail("Should have thrown NoSuchMethodRuntimeException: " + method);
		} catch (NoSuchMethodRuntimeException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	public void testArgumentMismatchesAreCaught() {
		try {
			DelegateMethod<System, Long> method = new DelegateMethod<System, Long>(
					System.class, "currentTimeMillis", "3");
			fail("Should have thrown NoSuchMethodRuntimeException : " + method);
		} catch (NoSuchMethodRuntimeException e) {
			assertNotNull(e.getMessage());
		}

	}

	@Test
	public void testPrimitivesAreAllowedViaAutoboxing() {
		DelegateMethod<DelegatePrimitiveTest, Boolean> method1 =
				new DelegateMethod<DelegatePrimitiveTest, Boolean>(DelegatePrimitiveTest.class, "getSomething", true);

		DelegatePrimitiveTest dt = new DelegatePrimitiveTest();
		assertTrue( method1.invoke(dt) );

		DelegateMethod<DelegatePrimitiveTest, Byte> method2 =
			new DelegateMethod<DelegatePrimitiveTest, Byte>(DelegatePrimitiveTest.class, "getSomething", (byte)1);
		assertTrue(((byte)1) == method2.invoke(dt));

		DelegateMethod<DelegatePrimitiveTest, Short> method3 =
			new DelegateMethod<DelegatePrimitiveTest, Short>(DelegatePrimitiveTest.class, "getSomething", (short)3);
		assertTrue(((short)3) == method3.invoke(dt));

		DelegateMethod<DelegatePrimitiveTest, Integer> method4 =
			new DelegateMethod<DelegatePrimitiveTest, Integer>(DelegatePrimitiveTest.class, "getSomething", 4);
		assertTrue(4 == method4.invoke(dt));

		DelegateMethod<DelegatePrimitiveTest, Long> method5 =
			new DelegateMethod<DelegatePrimitiveTest, Long>(DelegatePrimitiveTest.class, "getSomething", 5L);
		assertTrue(5 == method5.invoke(dt));

		DelegateMethod<DelegatePrimitiveTest, Float> method6 =
			new DelegateMethod<DelegatePrimitiveTest, Float>(DelegatePrimitiveTest.class, "getSomething", 3.14f);
		assertTrue(Math.abs(method6.invoke(dt)) - 3.14 < .1);

		DelegateMethod<DelegatePrimitiveTest, Double> method7 =
			new DelegateMethod<DelegatePrimitiveTest, Double>(DelegatePrimitiveTest.class, "getSomething", 3.14);
		assertTrue(Math.abs(method7.invoke(dt)) - 3.14 < .1);

		DelegateMethod<DelegatePrimitiveTest, Character> method8 =
			new DelegateMethod<DelegatePrimitiveTest, Character>(DelegatePrimitiveTest.class, "getSomething", 'c');
		assertTrue('c' == method8.invoke(dt));



	}

	public static class DelegatePrimitiveTest {
		public byte getSomething(final byte aValue) {return aValue;}

		public short getSomething(final short aValue) {return aValue;}

		public int getSomething(final int aValue) {return aValue;}

		public long getSomething(final long aValue) {return aValue;}

		public float getSomething(final float aValue) {return aValue;}

		public double getSomething(final double aValue) {return aValue;}

		public boolean getSomething(final boolean aValue) {return aValue;}

		public char getSomething(final char aValue) {return aValue;}
	};

}
