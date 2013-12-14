/*****************************************************************************
 * Copyright (C) 2003-2013 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Serban Iordache                                          *
 *****************************************************************************/
package com.picocontainer.gems.containers;

import static org.junit.Assert.assertEquals;

import java.io.InputStreamReader;

import org.junit.Test;

import com.picocontainer.MutablePicoContainer;

public class JsonPicoContainerTest {
	public int getResult(String jsonFileName) {
		InputStreamReader reader = new InputStreamReader(JsonPicoContainerTest.class.getResourceAsStream(jsonFileName));
		MutablePicoContainer pico = new JsonPicoContainer(reader);
		SeriesHandler handler = (SeriesHandler)pico.getComponent("handler");
		if(handler == null) throw new PicoJsonException("handler is null.");
		handler.handleSeries(1, -2, 3, -4, 5);
		return handler.getResult();
	}

	@Test public void test1() { assertEquals(3, getResult("cfg1.json")); }

	@Test public void test2() { assertEquals(6, getResult("cfg2.json")); }

	@Test public void test3() { assertEquals(9, getResult("cfg3.json")); }

	@Test public void test4() { assertEquals(60, getResult("cfg4.json")); }
}
