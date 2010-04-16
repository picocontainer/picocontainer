/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.lifecycle.NullLifecycleStrategy;

public class InterceptingTestCase {

    public static interface Person {
        String greeting();
        String parting(String who);
        void sleep(int howLong);
        public static class nullobject implements Person {
            public String greeting() {
                return null;
            }
            public String parting(String who) {
                return null;
            }
            public void sleep(int howLong) {
            }
        }

    }

    public static class Englishman implements Person {
        private StringBuilder sb;

        public Englishman(StringBuilder sb) {
            this.sb = sb;
        }

        public String greeting() {
            String phrase = "How do you do?";
            sb.append(phrase);
            return phrase;
        }

        public String parting(String who) {
            String phrase = "Goodbye " + who + ".";
            sb.append(phrase);
            return phrase;
        }

        public void sleep(int howLong) {
            sb.append("Nap for " + howLong);
        }
    }

    @Test public void testPreAndPostObservation() {
        final StringBuilder sb = new StringBuilder();
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new Intercepting());
        pico.addComponent(StringBuilder.class, sb);
        pico.addComponent(Person.class, Englishman.class);

        Intercepted intercepted = pico.getComponentAdapter(Person.class).findAdapterOfType(Intercepted.class);
        final Intercepted.Controller interceptor = intercepted.getController();
        intercepted.addPostInvocation(Person.class, new Person.nullobject() {
            public String greeting() {
                sb.append("</english-greeting>");
                return null;
            }
        });
        intercepted.addPreInvocation(Person.class, new Person.nullobject() {
            public String greeting() {
                sb.append("<english-greeting>");
                return null;
            }
        });


        Person foo = pico.getComponent(Person.class);
        assertNotNull(foo);
        assertEquals("How do you do?", foo.greeting());
        assertEquals("<english-greeting>How do you do?</english-greeting>", sb.toString());
        assertEquals("Intercepted:ConstructorInjector-interface org.picocontainer.behaviors.InterceptingTestCase$Person", pico.getComponentAdapter(Person.class).toString());
    }

    @Test public void testPreAndPostObservationWithParameter() {
        final StringBuilder sb = new StringBuilder();
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new Intercepting());
        pico.addComponent(StringBuilder.class, sb);
        pico.addComponent(Person.class, Englishman.class);

        Intercepted intercepted = pico.getComponentAdapter(Person.class).findAdapterOfType(Intercepted.class);
        final Intercepted.Controller interceptor = intercepted.getController();
        intercepted.addPostInvocation(Person.class, new Person.nullobject() {
            public String parting(String a) {
                assertEquals("Goodbye Fred.", interceptor.getOriginalRetVal().toString());
                sb.append("</english-parting>");
                return null;
            }
        });
        intercepted.addPreInvocation(Person.class, new Person.nullobject() {
            public String parting(String who) {
                sb.append("<english-parting who='"+who+"'>");
                return null;
            }
        });

        Person foo = pico.getComponent(Person.class);
        assertNotNull(foo);
        assertEquals("Goodbye Fred.", foo.parting("Fred").trim());
        assertEquals("<english-parting who='Fred'>Goodbye Fred.</english-parting>", sb.toString());
        assertEquals("Intercepted:ConstructorInjector-interface org.picocontainer.behaviors.InterceptingTestCase$Person", pico.getComponentAdapter(Person.class).toString());
    }

    @Test public void testPreCanPreventInvocationWithAlternateReturnValue() {
        final StringBuilder sb = new StringBuilder();
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new Intercepting());
        pico.addComponent(Person.class, Englishman.class);
        pico.addComponent(StringBuilder.class, sb);

        Intercepted intercepted = pico.getComponentAdapter(Person.class).findAdapterOfType(Intercepted.class);
        final Intercepted.Controller interceptor = intercepted.getController();
        intercepted.addPreInvocation(Person.class, new Person.nullobject() {
            public String parting(String who) {
                interceptor.veto();
                return "Au revoir " + who + ".";
            }
        });

        Person foo = pico.getComponent(Person.class);
        assertNotNull(foo);
        assertEquals("Au revoir Fred.", foo.parting("Fred"));
        assertEquals("", sb.toString());
        assertEquals("Intercepted:ConstructorInjector-interface org.picocontainer.behaviors.InterceptingTestCase$Person", pico.getComponentAdapter(Person.class).toString());
    }

    @Test public void testOverrideOfReturnValue() {
        final StringBuilder sb = new StringBuilder();
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new Intercepting());
        pico.addComponent(Person.class, Englishman.class);
        pico.addComponent(StringBuilder.class, sb);
        Intercepted intercepted = pico.getComponentAdapter(Person.class).findAdapterOfType(Intercepted.class);
        final Intercepted.Controller interceptor = intercepted.getController();
        intercepted.addPreInvocation(Person.class, new Person.nullobject() {
            public String parting(String who) {
                sb.append("[Before parting]");
                return null;
            }
        });
        intercepted.addPostInvocation(Person.class, new Person() {
            public String greeting() {
                return null;
             }

            public String parting(String who) {
                interceptor.override();
                sb.append("[After parting]");
                return "Arrivederci " + who;
            }

            public void sleep(int howLong) {
            }
        });

        Person foo = pico.getComponent(Person.class);
        assertNotNull(foo);
        assertEquals("Arrivederci Fred", foo.parting("Fred"));
        assertEquals("[Before parting]Goodbye Fred.[After parting]", sb.toString());
        assertEquals("Intercepted:ConstructorInjector-interface org.picocontainer.behaviors.InterceptingTestCase$Person", pico.getComponentAdapter(Person.class).toString());
    }

    @Test public void testNothingHappensIfNoPreOrPost() {
        final StringBuilder sb = new StringBuilder();
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new Intercepting());
        pico.addComponent(Person.class, Englishman.class);
        pico.addComponent(StringBuilder.class, sb);
        Person foo = pico.getComponent(Person.class);
        assertNotNull(foo);
        assertEquals("Goodbye Fred.", foo.parting("Fred"));
        assertEquals("Goodbye Fred.", sb.toString());
        assertEquals("Intercepted:ConstructorInjector-interface org.picocontainer.behaviors.InterceptingTestCase$Person", pico.getComponentAdapter(Person.class).toString());
    }



}