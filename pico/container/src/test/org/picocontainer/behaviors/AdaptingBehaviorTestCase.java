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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.annotations.Cache;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.injectors.SetterInjector;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;

import com.thoughtworks.xstream.XStream;

@SuppressWarnings("serial")
public class AdaptingBehaviorTestCase {

    @Test public void testCachingBehaviorCanBeAddedByCharacteristics() {
        AdaptingBehavior abf = new AdaptingBehavior();
        Properties cc = new Properties();
        mergeInto(Characteristics.CACHE,cc);
        ComponentAdapter ca = abf.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), cc, Map.class, HashMap.class);
        assertTrue(ca instanceof Cached);
        Map map = (Map)ca.getComponentInstance(new EmptyPicoContainer(), ComponentAdapter.NOTHING.class);
        assertNotNull(map);
        Map map2 = (Map)ca.getComponentInstance(new EmptyPicoContainer(), ComponentAdapter.NOTHING.class);
        assertSame(map, map2);
        assertEquals(0, cc.size());
        assertEquals("Cached:ConstructorInjector-interface java.util.Map",ca.toString());
    }

    @Test public void testCachingBehaviorCanBeAddedByAnnotation() {
        AdaptingBehavior abf = new AdaptingBehavior();
        Properties cc = new Properties();
        ComponentAdapter ca = abf.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), cc, Map.class, MyHashMap.class);
        assertTrue(ca instanceof Cached);
        Map map = (Map)ca.getComponentInstance(new EmptyPicoContainer(), ComponentAdapter.NOTHING.class);
        assertNotNull(map);
        Map map2 = (Map)ca.getComponentInstance(new EmptyPicoContainer(), ComponentAdapter.NOTHING.class);
        assertSame(map, map2);
        assertEquals(0, cc.size());
        assertEquals("Cached:ConstructorInjector-interface java.util.Map",ca.toString());
    }

    @Cache
    public static class MyHashMap extends HashMap {
        public MyHashMap() {
        }
    }

    public static class MyHashMap2 extends HashMap {
        private String foo;

        public MyHashMap2() {
        }
        public void setFoo(String foo) {
            this.foo = foo;
        }
    }

    @Test public void testImplementationHidingBehaviorCanBeAddedByCharacteristics() {
        AdaptingBehavior abf = new AdaptingBehavior();
        Properties cc = new Properties();
        mergeInto(Characteristics.HIDE_IMPL,cc);
        ComponentAdapter ca = abf.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), cc, Map.class, HashMap.class);
        assertTrue(ca instanceof HiddenImplementation);
        Map map = (Map)ca.getComponentInstance(new EmptyPicoContainer(), ComponentAdapter.NOTHING.class);
        assertNotNull(map);
        assertTrue(!(map instanceof HashMap));

        assertEquals(0, cc.size());
        assertEquals("Hidden:ConstructorInjector-interface java.util.Map",ca.toString());

    }

    @Test public void testPropertyApplyingBehaviorCanBeAddedByCharacteristics() {
        AdaptingBehavior abf = new AdaptingBehavior();
        Properties cc = new Properties();
        mergeInto(Characteristics.PROPERTY_APPLYING,cc);
        ComponentAdapter ca = abf.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), cc, Map.class, MyHashMap2.class);
        assertTrue(ca instanceof PropertyApplicator);
        PropertyApplicator pa = (PropertyApplicator)ca;
        pa.setProperty("foo", "bar");
        Map map = (Map)ca.getComponentInstance(new EmptyPicoContainer(), ComponentAdapter.NOTHING.class);
        assertNotNull(map);
        assertTrue(map instanceof HashMap);
        assertTrue(map instanceof MyHashMap2);
        assertEquals("bar", ((MyHashMap2) map).foo);

        assertEquals(0, cc.size());
        assertEquals("PropertyApplied:ConstructorInjector-interface java.util.Map",ca.toString());

    }

    @Test public void testSetterInjectionCanBeTriggereedMeaningAdaptiveInjectorIsUsed() {
        AdaptingBehavior abf = new AdaptingBehavior();
        Properties cc = new Properties();
        mergeInto(Characteristics.SDI,cc);
        ComponentAdapter ca = abf.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), cc, Map.class, HashMap.class);
        assertTrue(ca instanceof SetterInjector);
        Map map = (Map)ca.getComponentInstance(new EmptyPicoContainer(), ComponentAdapter.NOTHING.class);
        assertNotNull(map);
        assertEquals(0, cc.size());
        assertEquals("SetterInjector-interface java.util.Map",ca.toString());

    }

    @Test public void testCachingAndImplHidingAndThreadSafetySetupCorrectly() {
        AdaptingBehavior abf = new AdaptingBehavior();
        Properties cc = new Properties();
        mergeInto(Characteristics.CACHE,cc);
        mergeInto(Characteristics.HIDE_IMPL,cc);
        mergeInto(Characteristics.SYNCHRONIZE,cc);
        ComponentAdapter ca = abf.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), cc, Map.class, HashMap.class);
        assertTrue(ca instanceof Cached);
        Map map = (Map)ca.getComponentInstance(new EmptyPicoContainer(), null);
        assertNotNull(map);
        assertTrue(!(map instanceof HashMap));

        XStream xs = new XStream();
        String foo = xs.toXML(ca);

        int ih = foo.indexOf(HiddenImplementation.class.getName());
        int sb = foo.indexOf(Synchronized.class.getName());

        // check right nesting order
        assertTrue(ih>0);
        assertTrue(sb>0);
        assertTrue(sb>ih);

        assertEquals(0, cc.size());
        assertEquals("Cached:Hidden:Synchronized:ConstructorInjector-interface java.util.Map",ca.toString());

        


    }

    @Test public void testCachingAndImplHidingAndThreadSafetySetupCorrectlyForExtraCaching() {
        Caching cbf = new Caching();
        AdaptingBehavior abf = new AdaptingBehavior();
        cbf.wrap(abf);
        Properties cc = new Properties();
        mergeInto(Characteristics.CACHE,cc);
        mergeInto(Characteristics.HIDE_IMPL,cc);
        mergeInto(Characteristics.SYNCHRONIZE,cc);
        ComponentAdapter ca = cbf.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), cc, Map.class, HashMap.class);
        assertTrue(ca instanceof Cached);
        Map map = (Map)ca.getComponentInstance(new EmptyPicoContainer(), ComponentAdapter.NOTHING.class);
        assertNotNull(map);
        assertTrue(!(map instanceof HashMap));

        XStream xs = new XStream();
        String foo = xs.toXML(ca);

        assertTrue(foo.indexOf("<" + Cached.class.getName() + ">", 0)  > -1);  // xml does start with CB
        assertFalse(foo.indexOf("<" + Cached.class.getName() + ">", 1)  > -1); // but only contains it once.
        assertEquals("Cached:Hidden:Synchronized:ConstructorInjector-interface java.util.Map",ca.toString());

    }

    @Test public void testCachingAndImplHidingAndThreadSafetySetupCorrectlyForExtraCachingForAdapter() {
        Caching cbf = new Caching();
        AdaptingBehavior abf = new AdaptingBehavior();
        cbf.wrap(abf);
        Properties cc = new Properties();
        mergeInto(Characteristics.CACHE,cc);
        mergeInto(Characteristics.HIDE_IMPL,cc);
        mergeInto(Characteristics.SYNCHRONIZE,cc);
        ComponentAdapter ca = cbf.addComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), cc, new InstanceAdapter(Map.class, new HashMap(), new NullLifecycleStrategy(), new NullComponentMonitor()));
        assertTrue(ca instanceof Cached);
        Map map = (Map)ca.getComponentInstance(new EmptyPicoContainer(), ComponentAdapter.NOTHING.class);
        assertNotNull(map);
        assertTrue(!(map instanceof HashMap));

        XStream xs = new XStream();
        String foo = xs.toXML(ca);

        assertTrue(foo.indexOf("<" + Cached.class.getName() + ">", 0)  > -1);  // xml does start with CB
        assertFalse(foo.indexOf("<" + Cached.class.getName() + ">", 1)  > -1); // but only contains it once.
        assertEquals("Cached:Hidden:Synchronized:Instance-interface java.util.Map",ca.toString());

    }



    public void mergeInto(Properties p, Properties into) {
        Enumeration e = p.propertyNames();
        while (e.hasMoreElements()) {
            String s = (String)e.nextElement();
            into.setProperty(s, p.getProperty(s));
        }

    }


}
