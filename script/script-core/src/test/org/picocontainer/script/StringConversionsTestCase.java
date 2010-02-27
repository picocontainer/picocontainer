/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.picocontainer.script.util.StringConversions;

public final class StringConversionsTestCase {
    private final StringConversions converter = new StringConversions();

    @Test public void testConversionToString() {
        assertEquals("hello", converter.convertTo(String.class, "hello"));
        assertEquals("", converter.convertTo(String.class, ""));
    }

    @Test public void testConversionToInts() {
        assertEquals(22, converter.convertTo(Integer.class, "22"));
        assertEquals(-9, converter.convertTo(Integer.class, "-9"));
    }

    @Test public void testConversionToLong() {
        assertEquals(123456789012L, converter.convertTo(Long.class, "123456789012"));
        assertEquals(-123456789012L, converter.convertTo(Long.class, "-123456789012"));
        assertEquals((long)0, converter.convertTo(Long.class, "0"));
    }

    @Test public void testConversionToBooleanUsingBestGuess() {
        assertEquals(Boolean.TRUE, converter.convertTo(Boolean.class, "t"));
        assertEquals(Boolean.TRUE, converter.convertTo(Boolean.class, "true"));
        assertEquals(Boolean.TRUE, converter.convertTo(Boolean.class, "T"));
        assertEquals(Boolean.TRUE, converter.convertTo(Boolean.class, "TRUE"));
        assertEquals(Boolean.TRUE, converter.convertTo(Boolean.class, "1"));
        assertEquals(Boolean.TRUE, converter.convertTo(Boolean.class, "yes"));
        assertEquals(Boolean.TRUE, converter.convertTo(Boolean.class, "Yo!"));

        assertEquals(Boolean.FALSE, converter.convertTo(Boolean.class, "f"));
        assertEquals(Boolean.FALSE, converter.convertTo(Boolean.class, "false"));
        assertEquals(Boolean.FALSE, converter.convertTo(Boolean.class, "FALSE"));
        assertEquals(Boolean.FALSE, converter.convertTo(Boolean.class, "0"));
        assertEquals(Boolean.FALSE, converter.convertTo(Boolean.class, "no"));
        assertEquals(Boolean.FALSE, converter.convertTo(Boolean.class, "nada!"));
        assertEquals(Boolean.FALSE, converter.convertTo(Boolean.class, ""));
        assertEquals(Boolean.FALSE, converter.convertTo(Boolean.class, "I'm a lumberjack and I'm okay"));
    }

    @SuppressWarnings("unchecked")
    @Test public void testCustomConversionsCanBeRegistered() {
        converter.register(File.class, new StringConversions.StringConverter() {
            public Object convert(String in) {
                return new File(in);
            }
        });
        assertEquals("hello", converter.convertTo(String.class, "hello"));
        assertEquals(new File("hello"), converter.convertTo(File.class, "hello"));
    }

    @Test public void testNullsMapToDefaultValues() {
        assertNull(converter.convertTo(String.class, null));
        assertEquals(0, converter.convertTo(Integer.class, null));
        assertEquals((long)0, converter.convertTo(Long.class, null));
        assertEquals(Boolean.FALSE, converter.convertTo(Boolean.class, null));
    }

    @Test public void testExceptionThrownIfConverterNotRegistered() {
        try {
            converter.convertTo(File.class, "hello");
            fail("Should have thrown exception");
        } catch (StringConversions.InvalidConversionException e) {
            // good
        }
    }

    @Test public void testDodgyFormatThrowExceptions() {
        try {
            converter.convertTo(Integer.class, "fooo");
            fail("Should have thrown exception");
        } catch (NumberFormatException e) {
            // good
        }
    }

}
