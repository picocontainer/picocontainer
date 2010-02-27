package org.picocontainer.gems.monitors.prefuse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.picocontainer.gems.monitors.ComponentDependencyMonitor.Dependency;

public final class PrefuseDependencyGraphTestCase {
    final PrefuseDependencyGraph prefuseGraph = new PrefuseDependencyGraph();

    @Test public void testAComponentWithoutAnyDependenciesShouldOnlyCreateOneNode2() throws Exception {
        prefuseGraph.addDependency(new Dependency(Object.class, null));
        assertEquals(1, prefuseGraph.getNodes().length);
        assertEquals(Object.class, prefuseGraph.getNodes()[0].get("type"));
    }

    @Test public void testDependencyShouldAddTwoNodes() throws Exception {
        prefuseGraph.addDependency(new Dependency(Object.class, Boolean.class));
        assertEquals(2, prefuseGraph.getNodes().length);
        Collection types = prefuseGraph.getTypes();
        assertTrue(types.contains(Object.class));
        assertTrue(types.contains(Boolean.class));
    }

    @Test public void testDependencyShouldAddOneNewNode() throws Exception {
        prefuseGraph.addDependency(new Dependency(Object.class, Boolean.class));
        prefuseGraph.addDependency(new Dependency(Object.class, String.class));
        assertEquals(3, prefuseGraph.getNodes().length);

        Collection types = prefuseGraph.getTypes();

        assertTrue(types.contains(Object.class));
        assertTrue(types.contains(Boolean.class));
        assertTrue(types.contains(String.class));
    }

    @Test public void testDependencyShouldAddThreeNodes() throws Exception {
        prefuseGraph.addDependency(new Dependency(Object.class, Boolean.class));
        assertEquals(2, prefuseGraph.getNodes().length);
        prefuseGraph.addDependency(new Dependency(String.class, Boolean.class));
        assertEquals(3, prefuseGraph.getNodes().length);

        Collection types = prefuseGraph.getTypes();

        assertTrue(types.contains(Object.class));
        assertTrue(types.contains(Boolean.class));
        assertTrue(types.contains(String.class));
    }

    @Test public void testDependencyShouldAddOneEdge() throws Exception {
        prefuseGraph.addDependency(new Dependency(Object.class, Boolean.class));
        assertEquals(1, prefuseGraph.getEdges().getTupleCount());
    }

    @Test public void testDependencyShouldAddTwoEdges() throws Exception {
        prefuseGraph.addDependency(new Dependency(Object.class, Boolean.class));
        prefuseGraph.addDependency(new Dependency(String.class, Boolean.class));
        assertEquals(2, prefuseGraph.getEdges().getTupleCount());
    }

    @Test public void testDependencyShouldAddOneEdgeWithSame() throws Exception {
        prefuseGraph.addDependency(new Dependency(Object.class, Boolean.class));
        prefuseGraph.addDependency(new Dependency(Object.class, Boolean.class));
        assertEquals(2, prefuseGraph.getEdges().getTupleCount());
    }

    @Test public void testGraphShouldNotContainAnyEdges() throws Exception {
        prefuseGraph.addDependency(new Dependency(Object.class, null));
        assertEquals(0, prefuseGraph.getEdges().getTupleCount());
    }
}
