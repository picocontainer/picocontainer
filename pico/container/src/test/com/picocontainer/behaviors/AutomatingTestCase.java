/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.behaviors;

import static com.picocontainer.Characteristics.AUTOMATIC;
import static com.picocontainer.behaviors.Behaviors.automatic;
import static com.picocontainer.behaviors.Behaviors.caching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.behaviors.Automating;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.lifecycle.NullLifecycleStrategy;

public class AutomatingTestCase {

    private static String MESSAGE =
        "Foo was instantiated, even though it was not required to be given it was not depended on by anything looked up";

    public static class Foo {
        public Foo(final StringBuilder sb) {
            sb.append(MESSAGE);
        }
    }

    public static class Bar {
    }

    @Test public void testAutomaticBehavior() {
        DefaultPicoContainer pico = new DefaultPicoContainer(null, new NullLifecycleStrategy(), new Caching().wrap(new Automating()));
        pico.addComponent(StringBuilder.class);
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        ComponentAdapter<?> adapter = pico.getComponentAdapter(Foo.class);
        String s = adapter.toString();
        assertEquals("Cached+Lifecycle:Automated:CompositeInjector(ConstructorInjector)-class com.picocontainer.behaviors.AutomatingTestCase$Foo", s);
    }

    @Test public void testAutomaticBehaviorViaAdapter() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new Caching().wrap(new Automating()));
        pico.addComponent(StringBuilder.class);
        pico.addAdapter(new ConstructorInjection.ConstructorInjector<Foo>(Foo.class, Foo.class));
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class com.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testNonAutomaticBehaviorAsContrastToTheAbove() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new Caching());
        pico.addComponent(StringBuilder.class);
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals("", sb.toString());
    }

    @Test public void testNonAutomaticBehaviorAsContrastToTheAboveViaAdapter() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new Caching());
        pico.addComponent(StringBuilder.class);
        pico.addAdapter(new ConstructorInjection.ConstructorInjector(Foo.class, Foo.class));
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals("", sb.toString());
    }

    @Test public void testAutomaticBehaviorByBuilder() {
        MutablePicoContainer pico = new PicoBuilder().withCaching().withAutomatic().build();
        pico.addComponent(StringBuilder.class);
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        assertEquals("Cached+Lifecycle:Automated:CompositeInjector(ConstructorInjector)-class com.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testAutomaticBehaviorByBuilderViaAdapter() {
        MutablePicoContainer pico = new PicoBuilder().withCaching().withAutomatic().build();
        pico.addComponent(StringBuilder.class);
        pico.addAdapter(new ConstructorInjection.ConstructorInjector(Foo.class, Foo.class));
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class com.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testAutomaticBehaviorByBuilderADifferentWay() {
        MutablePicoContainer pico = new PicoBuilder().withBehaviors(caching(), automatic()).build();
        pico.addComponent(StringBuilder.class);
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        assertEquals("Cached+Lifecycle:Automated:CompositeInjector(ConstructorInjector)-class com.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

        @Test public void testAutomaticBehaviorByBuilderADifferentWayViaAdapter() {
        MutablePicoContainer pico = new PicoBuilder().withBehaviors(caching(), automatic()).build();
        pico.addComponent(StringBuilder.class);
        pico.addAdapter(new ConstructorInjection.ConstructorInjector(Foo.class, Foo.class));
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
            assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class com.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testAutomaticBehaviorWorksForAdaptiveBehaviorToo() {
        MutablePicoContainer pico = new PicoBuilder().withBehaviors(caching(), automatic()).build();
        pico.addComponent(StringBuilder.class);
        pico.as(AUTOMATIC).addComponent(Foo.class);
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        assertEquals("Cached+Lifecycle:Automated:CompositeInjector(ConstructorInjector)-class com.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testAutomaticBehaviorWorksForAdaptiveBehaviorTooViaAdapter() {
        MutablePicoContainer pico = new PicoBuilder().withBehaviors(caching(), automatic()).build();
        pico.addComponent(StringBuilder.class);
        pico.as(AUTOMATIC).addAdapter(new ConstructorInjection.ConstructorInjector(Foo.class, Foo.class));
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class com.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

}
