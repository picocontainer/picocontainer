/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved. *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * license.txt file. *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant *
 ******************************************************************************/
package org.picocontainer.gems.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;

/**
 * test capabilities of constructable properties 
 * @author Konstantin Pribluda 
 */
public class ConstructablePropertiesTest {

	@Test public void testPropertiesLoading() throws Exception {

		Properties properties = new ConstructableProperties("test.properties");
		assertNotNull(properties);
		assertEquals("bar", properties.getProperty("foo"));
	}
}