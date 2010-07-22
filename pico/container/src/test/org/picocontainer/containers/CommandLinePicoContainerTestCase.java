/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.containers;

import org.junit.Ignore;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.annotations.Inject;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.SetterInjection;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class CommandLinePicoContainerTestCase {

    @Test public void testBasicParsing() {
        CommandLinePicoContainer apc = new CommandLinePicoContainer(new String[] {
            "foo=bar", "foo2=12", "foo3=true", "foo4="
        });
        assertEquals("bar",apc.getComponent("foo"));
        assertEquals("12",apc.getComponent("foo2"));
        assertEquals("true",apc.getComponent("foo3"));
        assertEquals("true",apc.getComponent("foo4"));
    }

    @Test public void testAsParentContainer() {
        CommandLinePicoContainer apc = new CommandLinePicoContainer("a=aaa", "b=bbb", "d=22");
        assertEquals("aaa",apc.getComponent("a"));
        assertEquals("bbb",apc.getComponent("b"));
        assertEquals("22",apc.getComponent("d"));

        DefaultPicoContainer dpc = new DefaultPicoContainer(apc);
        dpc.addComponent(NeedsString.class);
        assertEquals("bbb", dpc.getComponent(NeedsString.class).val);
    }

    public static class NeedsString {
        public String val;
        public NeedsString(String b) {
            val = b;
        }
    }

    @Test public void testParsingWithDiffSeparator() {
        CommandLinePicoContainer apc = new CommandLinePicoContainer(':', "foo:bar", "foo2:12", "foo3:true");
        assertEquals("bar",apc.getComponent("foo"));
        assertEquals("12",apc.getComponent("foo2"));
        assertEquals("true",apc.getComponent("foo3"));
    }

    @Test public void testParsingWithWrongSeparator() {
        CommandLinePicoContainer apc = new CommandLinePicoContainer(':', "foo=bar", "foo2=12", "foo3=true");
        assertEquals("true",apc.getComponent("foo=bar"));
        assertEquals("true",apc.getComponent("foo2=12"));
        assertEquals("true",apc.getComponent("foo3=true"));
    }

    @Test public void testParsingOfPropertiesFile() throws IOException {
        CommandLinePicoContainer apc = new CommandLinePicoContainer(':',
                               new StringReader("foo:bar\nfoo2:12\nfoo3:true\n"));
        assertEquals("bar",apc.getComponent("foo"));
        assertEquals("12",apc.getComponent("foo2"));
        assertEquals("true",apc.getComponent("foo3"));
    }

    @Test public void testParsingOfPropertiesFileAndArgs() throws IOException {
        CommandLinePicoContainer apc = new CommandLinePicoContainer(':',
                               new StringReader("foo:bar\nfoo2:12\n"), "foo3:true");
        assertEquals("bar",apc.getComponent("foo"));
        assertEquals("12",apc.getComponent("foo2"));
        assertEquals("true",apc.getComponent("foo3"));
    }

    @Test public void testParsingOfPropertiesFileAndArgsWithClash() throws IOException {
        CommandLinePicoContainer apc = new CommandLinePicoContainer(':',
                               new StringReader("foo:bar\nfoo2:99\n"), "foo2:12","foo3:true");
        assertEquals("bar",apc.getComponent("foo"));
        assertEquals("12",apc.getComponent("foo2"));
        assertEquals("true",apc.getComponent("foo3"));
    }

    @Test public void testByTypeFailsEvenIfOneOfSameType() {
        CommandLinePicoContainer apc = new CommandLinePicoContainer("foo=bar");
        assertEquals("bar", apc.getComponent("foo"));
        assertNull(apc.getComponent(String.class));
    }

    @Test public void testUnsatisfiableIfNoSuitableTyesForInjection() {
        CommandLinePicoContainer apc = new CommandLinePicoContainer("zz=zz");
        DefaultPicoContainer pico = new DefaultPicoContainer(apc);
        pico.as(Characteristics.USE_NAMES).addComponent(NeedsAFew.class);
        try {
            Object foo = pico.getComponent(NeedsAFew.class);
            fail();
        } catch (AbstractInjector.UnsatisfiableDependenciesException e) {
            // expetced;
        }
    }
    public static class NeedsAFew {
        private final String a;
        private final int b;
        private final boolean c;
        public NeedsAFew(String a, int b, boolean c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    @Test public void testConstructorInjectionComponentCanDependOnConfig() {
        CommandLinePicoContainer apc = new CommandLinePicoContainer("a=a", "b=2", "c=true");
        DefaultPicoContainer pico = new DefaultPicoContainer(apc);
        pico.addConfig("zzz","zzz");
        pico.as(Characteristics.USE_NAMES).addComponent(NeedsAFew.class);
        NeedsAFew needsAFew = pico.getComponent(NeedsAFew.class);
        assertNotNull(needsAFew);
        assertEquals("a", needsAFew.a);
        assertEquals(2, needsAFew.b);
        assertEquals(true, needsAFew.c);
    }

    public static class NeedsAFew2 {
        private String a;
        private int b;
        private boolean c;

        public void setA(String a) {
            this.a = a;
        }

        public void setB(int b) {
            this.b = b;
        }

        public void setC(boolean c) {
            this.c = c;
        }
    }

    @Test public void testSetterInjectionComponentCanDependOnConfig() {
        CommandLinePicoContainer apc = new CommandLinePicoContainer("a=a", "b=2", "c=true");
        DefaultPicoContainer pico = new DefaultPicoContainer(apc, new SetterInjection());
        pico.addConfig("zzz","zzz");
        pico.as(Characteristics.USE_NAMES).addComponent(NeedsAFew2.class);
        NeedsAFew2 needsAFew = pico.getComponent(NeedsAFew2.class);
        assertNotNull(needsAFew);
        assertEquals("a", needsAFew.a);
        assertEquals(2, needsAFew.b);
        assertEquals(true, needsAFew.c);
    }

    public static class NeedsAFew3 {
        @Inject
        private String a;
        @Inject
        private int b;
        @Inject
        private boolean c;
    }

    @Test public void testAnnotatedFieldInjectionComponentCanDependOnConfig() {
        CommandLinePicoContainer apc = new CommandLinePicoContainer("a=a", "b=2", "c=true");
        DefaultPicoContainer pico = new DefaultPicoContainer(apc, new AnnotatedFieldInjection());
        pico.addConfig("zzz","zzz");
        pico.as(Characteristics.USE_NAMES).addComponent(NeedsAFew3.class);
        NeedsAFew3 needsAFew = pico.getComponent(NeedsAFew3.class);
        assertNotNull(needsAFew);
        assertEquals("a", needsAFew.a);
        assertEquals(2, needsAFew.b);
        assertEquals(true, needsAFew.c);
    }

    @Test public void testRepresentationOfContainerTree() {
        CommandLinePicoContainer parent = new CommandLinePicoContainer("a=a", "b=2", "c=true");
        parent.setName("parent");
        DefaultPicoContainer child = new DefaultPicoContainer(parent);
        child.setName("child");
		child.addComponent("hello", "goodbye");
        child.addComponent("bonjour", "aurevior");
        assertEquals("child:2<I<D<parent:3<|", child.toString());
    }


}
