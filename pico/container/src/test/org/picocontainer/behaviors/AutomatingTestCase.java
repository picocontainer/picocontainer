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
import static org.picocontainer.Characteristics.AUTOMATIC;
import static org.picocontainer.behaviors.Behaviors.automatic;
import static org.picocontainer.behaviors.Behaviors.caching;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.monitors.NullComponentMonitor;

public class AutomatingTestCase {

    private static String MESSAGE =
        "Foo was instantiated, even though it was not required to be given it was not depended on by anything looked up";

    public static class Foo {
        public Foo(StringBuilder sb) {
            sb.append(MESSAGE);
        }
    }

    public static class Bar {
    }

    @Test public void testAutomaticBehavior() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new Caching().wrap(new Automating()));
        pico.addComponent(StringBuilder.class);
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        ComponentAdapter<?> adapter = pico.getComponentAdapter(Foo.class);
        String s = adapter.toString();
        assertEquals("Cached+Lifecycle:Automated:LifecycleAdapter:ConstructorInjector-class org.picocontainer.behaviors.AutomatingTestCase$Foo", s);
    }

    @Test public void testAutomaticBehaviorViaAdapter() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new Caching().wrap(new Automating()));
        pico.addComponent(StringBuilder.class);
        pico.addAdapter(new ConstructorInjection.ConstructorInjector(Foo.class, Foo.class, null, new NullComponentMonitor(), false));
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class org.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
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
        pico.addAdapter(new ConstructorInjection.ConstructorInjector(Foo.class, Foo.class, null, new NullComponentMonitor(), false));
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
        assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class org.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testAutomaticBehaviorByBuilderViaAdapter() {
        MutablePicoContainer pico = new PicoBuilder().withCaching().withAutomatic().build();
        pico.addComponent(StringBuilder.class);
        pico.addAdapter(new ConstructorInjection.ConstructorInjector(Foo.class, Foo.class, null, new NullComponentMonitor(), false));
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class org.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
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
        assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class org.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

        @Test public void testAutomaticBehaviorByBuilderADifferentWayViaAdapter() {
        MutablePicoContainer pico = new PicoBuilder().withBehaviors(caching(), automatic()).build();
        pico.addComponent(StringBuilder.class);
        pico.addAdapter(new ConstructorInjection.ConstructorInjector(Foo.class, Foo.class, null, new NullComponentMonitor(), false));
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
            assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class org.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
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
        assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class org.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testAutomaticBehaviorWorksForAdaptiveBehaviorTooViaAdapter() {
        MutablePicoContainer pico = new PicoBuilder().withBehaviors(caching(), automatic()).build();
        pico.addComponent(StringBuilder.class);
        pico.as(AUTOMATIC).addAdapter(new ConstructorInjection.ConstructorInjector(Foo.class, Foo.class, null, new NullComponentMonitor(), false));
        pico.addComponent(Bar.class);
        pico.start();
        assertNotNull(pico.getComponent(Bar.class));
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(MESSAGE, sb.toString());
        assertEquals("Cached+Lifecycle:Automated:ConstructorInjector-class org.picocontainer.behaviors.AutomatingTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

}
