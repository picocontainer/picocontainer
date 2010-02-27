/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.remoting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.picocontainer.web.remoting.JsonPicoWebRemotingServlet.makeJsonDriver;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.web.NONE;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonWriter;

/**
 * @author Paul Hammant
 */
public final class PicoWebRemotingTestCase {

    private XStream xStream = new XStream(makeJsonDriver(JsonWriter.DROP_ROOT_MODE));
    PicoWebRemotingMonitor monitor = new NullPicoWebRemotingMonitor();

    @Test
    public void testPaths() throws Exception {

        PicoWebRemoting pwr = new PicoWebRemoting(xStream, "alpha/", null, "y", false, true);

        pwr.directorize("foo/bar/baz1");
        pwr.directorize("foo/bar/baz2");

        assertEquals(3, pwr.getPaths().size());
        assertTrue(pwr.getPaths().get("foo") instanceof PicoWebRemoting.Directories);

        PicoWebRemoting.Directories dirs = (PicoWebRemoting.Directories) pwr.getPaths().get("foo");
        assertEquals(1, dirs.size());
        assertEquals("bar", dirs.toArray()[0]);

        dirs = (PicoWebRemoting.Directories) pwr.getPaths().get("foo/bar");
        List<String> sorted = sortedListOf(dirs);
        assertEquals(2, sorted.size());
        assertEquals("baz1", sorted.get(0));
        assertEquals("baz2", sorted.get(1));

        dirs = (PicoWebRemoting.Directories) pwr.getPaths().get("");
        assertEquals(1, dirs.size());
        assertEquals("foo", dirs.toArray()[0]);
    }

    private static List<String> sortedListOf(PicoWebRemoting.Directories dirs) {
        List<String> list = new ArrayList<String>(dirs);
        Collections.sort(list);
        return list;
    }

    @Test
    public void testMissingMethodWillCauseAMethodList() throws Exception {
        PicoWebRemoting pwr = new PicoWebRemoting(xStream, "alpha/", null, "y", false, true);
        pwr.directorize("alpha/beta", Foo.class, Foo.class);

        DefaultPicoContainer pico = new DefaultPicoContainer();

        String result = pwr.processRequest("/beta/", pico, "GET", new NullComponentMonitor());
        assertEquals(
                "[\n" +
                    "  \"aCol\",\n" +
                    "  \"aList\",\n" +
                    "  \"color\",\n" +
                    "  \"goodbye\",\n" +
                    "  \"hello\"\n" +
                    "]\n", result);
    }

    @Test
    public void testMissingParamWillCauseASuitableMessage() throws Exception {
        PicoWebRemoting pwr = new PicoWebRemoting(xStream, "alpha/", null, "y", false, true);
        pwr.setMonitor(new NullPicoWebRemotingMonitor());
        pwr.directorize("alpha/Foo", Foo.class, Foo.class);

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Foo.class);

        String result = pwr.processRequest("/Foo/hello", pico, "GET", new NullComponentMonitor());
        assertEquals(
                "{\n" +
                "  \"ERROR\": true,\n" +
                "  \"message\": \"Parameter 'longArg' missing\"\n" +
                "}\n", result);
    }

    @Test
    public void testRightParamWillCauseInvocation() throws Exception {
        PicoWebRemoting pwr = new PicoWebRemoting(xStream, "alpha/", null, "y", false, true);
        pwr.directorize("alpha/Foo", Foo.class, Foo.class);

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Foo.class);
        pico.addComponent("longArg", new Long(123));

        String result = pwr.processRequest("/Foo/hello", pico, "GET", new NullComponentMonitor());
        assertEquals("11\n", result);

        result = pwr.processRequest("/Foo/goodbye", pico, "GET", new NullComponentMonitor());
        assertEquals("33\n", result);
    }

    @Test
    public void testCanBeVisited() throws Exception {
        PicoWebRemoting pwr = new PicoWebRemoting(xStream, "alpha/", null, "y", false, true);
        pwr.directorize("alpha/Foo", Foo.class, Foo.class);

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Foo.class);
        pico.addComponent("longArg", new Long(123));

        final StringBuffer sb = new StringBuffer();

        MethodVisitor visitor = new MethodVisitor() {
            public void method(String methodName, Method m) throws IOException {
                sb.append("m:").append(methodName).append(",").append(m.getName()).append(";");
            }

            public void superClass(String superClass) throws IOException {
                sb.append("sc:").append(superClass).append(";");
            }
        };
        
        pwr.visitClass("Foo", pico, visitor);

        assertEquals(
                "sc:java/lang/Object;" +
                      "m:aCol,aCol;" +
                      "m:aList,aList;" +
                      "m:color,getColor;" +
                      "m:goodbye,goodbye;" +
                      "m:hello,hello;", sb.toString());
    }

    @Test
    public void testRightParamWillCauseInvocationForCaseInsensitive() throws Exception {
        PicoWebRemoting pwr = new PicoWebRemoting(xStream, "org/picocontainer/web/remoting/", null, "y", true, true);
        pwr.setMonitor(new NullPicoWebRemotingMonitor());

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Foo.class);
        pwr.publishAdapters(pico.getComponentAdapters(), "y");

        pico.addComponent("longArg", new Long(123));

        String result = pwr.processRequest("/picowebremotingtestcase$foo/hello", pico, "GET", new NullComponentMonitor());
        assertEquals("11\n", result);

        result = pwr.processRequest("/picowebremotingtestcase$foo/goodbye", pico, "GET", new NullComponentMonitor());
        assertEquals("33\n", result);
    }

    @Test
    public void testRightParamWillCauseInvocation2() throws Exception {
        PicoWebRemoting pwr = new PicoWebRemoting(xStream, "alpha/", ".ajax", "y", false, true);
        pwr.directorize("alpha/Foo", Foo.class, Foo.class);

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Foo.class);
        pico.addComponent("longArg", new Long(123));

        String result = pwr.processRequest("/Foo/hello.ajax", pico, "GET", new NullComponentMonitor());
        assertEquals("11\n", result);

    }

    @Test
    public void testRightParamWillCauseInvocationWithNoPrefix() throws Exception {
        PicoWebRemoting pwr = new PicoWebRemoting(xStream, "", null, "y", false, true);
        pwr.setMonitor(new NullPicoWebRemotingMonitor());
        pwr.directorize("Foo", Foo.class, Foo.class);

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Foo.class);
        pico.addComponent("longArg", new Long(123));

        String result = pwr.processRequest("/Foo/hello", pico, "GET", new NullComponentMonitor());
        assertEquals("11\n", result);

        result = pwr.processRequest("/Foo/goodbye", pico, "GET", new NullComponentMonitor());
        assertEquals("33\n", result);
        
        result = pwr.processRequest("/Foo/color", pico, "GET", new NullComponentMonitor());
        assertEquals("{\n" +
                "  \"red\": 255,\n" +
                "  \"green\": 0,\n" +
                "  \"blue\": 0,\n" +
                "  \"alpha\": 255\n" +
                "}\n", result);

        result = pwr.processRequest("/Foo/color", pico, "DELETE", new NullComponentMonitor());
        assertEquals("true\n", result);

        result = pwr.processRequest("/Foo/color", pico, "POST", new NullComponentMonitor());
        assertEquals("\"posted color\"\n", result);

        result = pwr.processRequest("/Foo/color", pico, "PUT", new NullComponentMonitor());
        assertEquals("\"put color\"\n", result);
    }

    @Test
    public void testHiddenMethodNotPublished() throws Exception {
        PicoWebRemoting pwr = new PicoWebRemoting(xStream, "alpha/", null, "y", false, true);
        pwr.setMonitor(new NullPicoWebRemotingMonitor());

        pwr.directorize("alpha/Foo", Foo.class, Foo.class);

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Foo.class);

        String result = pwr.processRequest("/Foo/shhh", pico, "GET", new NullComponentMonitor());
        assertEquals("{\n" +
                "  \"ERROR\": true,\n" +
                "  \"message\": \"Nothing matches the path requested\"\n" +
                "}\n", result);

    }

    @Test
    public void testNonExistantMethodNotPublished() throws Exception {
        PicoWebRemoting pwr = new PicoWebRemoting(xStream, "alpha/", null, "y", false, true);
        pwr.setMonitor(new NullPicoWebRemotingMonitor());

        pwr.directorize("alpha/Foo", Foo.class, Foo.class);

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Foo.class);

        String result = pwr.processRequest("/Foo/kjhsdfjhsdfjhgasdfadfsdf", pico, "GET", new NullComponentMonitor());
        assertEquals("{\n" +
                "  \"ERROR\": true,\n" +
                "  \"message\": \"Nothing matches the path requested\"\n" +
                "}\n", result);

    }

    @Test
    public void testCollectionAndListResults() throws Exception {
        PicoWebRemoting pwr = new PicoWebRemoting(new XStream(makeJsonDriver(JsonWriter.DROP_ROOT_MODE)), "org/picocontainer/web/remoting/", null, "y", true, true);
        pwr.setMonitor(new NullPicoWebRemotingMonitor());

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Foo.class);
        pwr.publishAdapters(pico.getComponentAdapters(), "y");

        pico.addComponent("longArg", new Long(123));

        assertEquals("[\n" +
                "  12,\n" +
                "  13\n" +
                "]\n", pwr.processRequest("/picowebremotingtestcase$foo/aCol", pico, "GET", new NullComponentMonitor()));

        assertEquals("[\n" +
                "  33,\n" +
                "  34\n" +
                "]\n", pwr.processRequest("/picowebremotingtestcase$foo/aList", pico, "GET", new NullComponentMonitor()));

    }


    public static class Foo {
        public int hello(long longArg) {
            return 11;
        }

        public int goodbye() {
            return 33;
        }

        public Color getColor() {
            return Color.red;
        }

        public boolean deleteColor() {
            return true;
        }

        public String putColor() {
            return "put color";
        }

        public String postColor() {
            return "posted color";
        }

        @NONE
        public boolean shhh() {
            return true;
        }

        public Collection<Integer> aCol() {
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(12);
            list.add(13);
            return list;
        }

        public List<Integer> aList() {
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(33);
            list.add(34);
            return list;
        }
    }


}
