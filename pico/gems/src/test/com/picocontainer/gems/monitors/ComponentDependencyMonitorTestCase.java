/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.gems.monitors;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import com.picocontainer.gems.monitors.ComponentDependencyMonitor.Dependency;
import com.picocontainer.gems.monitors.prefuse.ComponentDependencyListener;
import com.picocontainer.testmodel.DependsOnList;

public class ComponentDependencyMonitorTestCase implements ComponentDependencyListener {
    private ComponentDependencyMonitor monitor;

    private Dependency dependency;

    @Before
    public void setUp() throws Exception {
        monitor = new ComponentDependencyMonitor(this);
        dependency = new Dependency(Object.class, String.class);
    }

    @Test public void testShouldDependOnList() throws Exception {
        List<Object> list = new ArrayList<Object>();
        DependsOnList dol = new DependsOnList(list);
        monitor.instantiated(null, null, DependsOnList.class.getConstructors()[0], dol, new Object[] { list }, 10);
        assertEquals(new Dependency(DependsOnList.class, ArrayList.class), dependency);
    }

    public void addDependency(final Dependency dependency) {
        this.dependency = dependency;
    }

    @Test public void testAShouldBeDependentOnB() throws Exception {
        assertEquals(true, dependency.dependsOn(String.class));
    }

    @Test public void testADoesntDependOnB() throws Exception {
        assertEquals(false, dependency.dependsOn(Boolean.class));
    }

    @Test public void testADoesntDependOnNullB() throws Exception {
        assertEquals(false, dependency.dependsOn(null));
    }

    @Test public void testShouldNotEqualNull() throws Exception {
        assertEquals("not equal to null", false, dependency.equals(null));
    }

    @Test public void testShouldEqualSelf() throws Exception {
        assertEquals("equal to self", dependency, dependency);
    }

    @Test public void testShouldEqualSimilarDependency() throws Exception {
        assertEquals(dependency, new Dependency(Object.class, String.class));
    }

    @Test public void testShouldNotEqualDifferentDependency() throws Exception {
        assertEquals("not equal to different dependency", false, dependency.equals(new Dependency(Object.class,
                Object.class)));
        assertEquals("not equal to different dependency", false, dependency.equals(new Dependency(String.class,
                String.class)));
    }

    @Test public void testShouldNotEqualObjectsWhichArentDependencies() throws Exception {
        assertEquals("not equal to different type", false, dependency.equals(new Object()));
    }

    @Test public void testShouldNotThrowNullPointerExceptionsWhenComparingEmptyDependencies() throws Exception {
        Dependency emptyDependency = new Dependency(null, null);
        assertEquals("not equal to empty dependency", false, dependency.equals(emptyDependency));
        assertEquals("not equal to empty dependency", false, emptyDependency.equals(dependency));
    }

    @Test public void testShouldNotThrowNullPointerExceptionsWhenComparingPartialDependencies() throws Exception {
        Dependency partialDependency = new Dependency(Boolean.class, null);
        assertEquals("not equal to empty dependency", false, dependency.equals(partialDependency));
        assertEquals("not equal to empty dependency", false, partialDependency.equals(dependency));
    }
}
