/*****************************************************************************
 * Copyright (C) 2003-2013 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Serban Iordache                                          *
 *****************************************************************************/
package com.picocontainer.script.json;

import com.picocontainer.MutablePicoContainer;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class JsonPicoContainerTest {

    private static final String PKG_DIR =  JsonPicoContainerTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "com/picocontainer/script/json/";

    private int getResult(String jsonFileName) {
        // InputStreamReader reader = new InputStreamReader(JsonPicoContainerTest.class.getResourceAsStream(jsonFileName));
        MutablePicoContainer pico = new JsonPicoContainer(PKG_DIR + jsonFileName);
        SeriesHandler handler = (SeriesHandler) pico.getComponent("handler");
        assertNotNull(handler);
        handler.handleSeries(1, -2, 3, -4, 5);
        return handler.getResult();
    }

    @Test
    public void test1() {
        assertEquals(3, getResult("cfg1.json"));
    }

    @Test
    public void test_missing_file() {
        try {
            getResult("missing.json");
            fail("should have barfed");
        } catch (PicoJsonException e) {
            assertThat(e.getCause(), is(FileNotFoundException.class));
            assertThat(e.getMessage(), containsString("script/json/missing.json"));
        }
    }

    @Test
    public void test2() {
        assertEquals(6, getResult("cfg2.json"));
    }

    @Test
    public void test3() {
        assertEquals(9, getResult("cfg3.json"));
    }

    @Test
    public void test4() {
        assertEquals(60, getResult("cfg4.json"));
    }

    @Test
    public void test_bad_classname_in_json_throws_exception() {
        try {
            getResult("bad_classname_inside.json");
            fail("should have barfed");
        } catch (PicoJsonException e) {
            Assert.assertEquals("java.lang.ClassNotFoundException: com.picocontainer.script.json.Blahhhhhhhhh", e.getMessage());
        }
    }
}
