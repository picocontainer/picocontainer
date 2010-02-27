/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Michael Rimov                                            *
 *****************************************************************************/
package org.picocontainer.gems.util;

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
	@SuppressWarnings("unchecked")
	public void testVoidReturnType() {
		DelegateMethod<Map, Void> method = new DelegateMethod<Map, Void>(
				Map.class, "clear");
		HashMap<String, String> testMap = new HashMap<String, String>();
		testMap.put("a", "A");
		method.invoke(testMap);
		assertEquals(0, testMap.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTypeMatching() {
		HashMap<String, String> testMap = new HashMap<String, String>();
		DelegateMethod<Map, String> method = new DelegateMethod<Map, String>(
				Map.class, "put", "A", "A Value");

		assertNull(method.invoke(testMap));
		assertEquals("A Value", method.invoke(testMap));

		assertEquals("A Value", testMap.get("A"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testPrimitiveTypeMatching() {
		HashMap<Integer, Integer> testMap = new HashMap<Integer, Integer>();
		DelegateMethod<Map, Integer> method = new DelegateMethod<Map, Integer>(
				Map.class, "put", 1, 3);

		assertNull(method.invoke(testMap));
		assertEquals(3, method.invoke(testMap).intValue());

		assertEquals(3, testMap.get(1).intValue());
	}

	@Test
	@SuppressWarnings("unchecked")
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

}
