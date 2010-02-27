/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/
package org.picocontainer.gems.monitors;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.testmodel.DependsOnDependsOnListAndVector;
import org.picocontainer.testmodel.DependsOnList;

public class DotDependencyGraphComponentMonitorTestCase {

    DotDependencyGraphComponentMonitor monitor;

    @Before
    public void setUp() throws Exception {
        monitor = new DotDependencyGraphComponentMonitor();

        Vector vec = new Vector();
        List list = new ArrayList();
        DependsOnList dol = new DependsOnList(list);
        DependsOnDependsOnListAndVector dodolav = new DependsOnDependsOnListAndVector(vec, dol);

        monitor.instantiated(null, null, Vector.class.getConstructor(), vec, new Object[]{}, 8);
        monitor.instantiated(null,
                             null, ArrayList.class.getConstructor(Collection.class), list, new Object[]{vec}, 12);
        monitor.instantiated(null, null, DependsOnList.class.getConstructors()[0], dol, new Object[]{list}, 10);
        monitor.instantiated(null,
                             null,
                             DependsOnDependsOnListAndVector.class.getConstructors()[0], dodolav, new Object[]{vec, dol}, 16);
        monitor.instantiated(null,
                             null,
                             DependsOnDependsOnListAndVector.class.getConstructors()[0], dodolav, new Object[]{vec, dol}, 12);
        monitor.instantiated(null,
                             null,
                             DependsOnDependsOnListAndVector.class.getConstructors()[0], dodolav, new Object[]{vec, dol}, 9);
    }

    @Test public void testSimpleClassDependencyGraphCanEliminateDupes() throws NoSuchMethodException {
        String expected = ("" +
                "  'java.util.ArrayList' -> 'java.util.Vector';\n" +
                "  'org.picocontainer.testmodel.DependsOnDependsOnListAndVector' -> 'java.util.Vector';\n" +
                "  'org.picocontainer.testmodel.DependsOnDependsOnListAndVector' -> 'org.picocontainer.testmodel.DependsOnList';\n" +
                "  'org.picocontainer.testmodel.DependsOnList' -> 'java.util.ArrayList';\n" +
                "").replaceAll("'","\"");
        assertEquals(expected, monitor.getClassDependencyGraph());
    }

    @Test public void testSimpleInterfaceDependencyGraphCanEliminateDupes() throws NoSuchMethodException {
        String expected = ("" +
                "  'java.util.ArrayList' -> 'java.util.Collection' [style=dotted,label='needs'];\n" +
                "  'java.util.ArrayList' -> 'java.util.List' [style=dotted, color=red,label='isA'];\n" +
                "  'java.util.ArrayList' [label='ArrayList\\njava.util'];\n" +
                "  'java.util.Collection' [shape=box, label='Collection\\njava.util'];\n" +
                "  'java.util.List' [shape=box, label='List\\njava.util'];\n" +
                "  'java.util.Vector' -> 'java.util.Collection' [style=dotted, color=red,label='isA'];\n" +
                "  'org.picocontainer.testmodel.DependsOnDependsOnListAndVector' -> 'java.util.Vector' [label='needs'];\n" +
                "  'org.picocontainer.testmodel.DependsOnDependsOnListAndVector' -> 'org.picocontainer.testmodel.DependsOnList' [label='needs'];\n" +
                "  'org.picocontainer.testmodel.DependsOnDependsOnListAndVector' [label='DependsOnDependsOnListAndVector\\norg.picocontainer.testmodel'];\n" +
                "  'org.picocontainer.testmodel.DependsOnList' -> 'java.util.List' [style=dotted,label='needs'];\n" +
                "  'org.picocontainer.testmodel.DependsOnList' [label='DependsOnList\\norg.picocontainer.testmodel'];\n" +
                "").replaceAll("'","\"");
        String interfaceDependencyGraph = monitor.getInterfaceDependencyGraph();
        assertEquals(expected, interfaceDependencyGraph);
    }


}
