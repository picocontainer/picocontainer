/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.gems.monitors.prefuse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.picocontainer.gems.monitors.ComponentDependencyMonitor.Dependency;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;

public final class PrefuseDependencyGraph implements ComponentDependencyListener {
    private Graph graph;

    private final Map nodes;

    public PrefuseDependencyGraph() {
        this.graph = initializeGraph();
        this.nodes = new HashMap();
    }

    public void addDependency(final Dependency dependency) {
        Node componentNode = addNode(dependency.getComponentType());
        Node dependencyNode = addNode(dependency.getDependencyType());
        if (dependencyNode != null) {
            graph.addEdge(componentNode, dependencyNode);
        }
    }

    Collection getTypes() {
        return nodes.keySet();
    }

    Node[] getNodes() {
        return (Node[]) nodes.values().toArray(new Node[nodes.size()]);
    }

    private Node addNode(final Class type) {
        if (type != null && !nodes.containsKey(type)) {
            Node node = graph.addNode();
            node.set("type", type);
            nodes.put(type, node);
        }
        return (Node) nodes.get(type);
    }

    private Graph initializeGraph() {
        return getGraph(getSchema());
    }

    private Graph getGraph(final Schema schema) {
        graph = new Graph(true);
        graph.addColumns(schema);
        return graph;
    }

    private Schema getSchema() {
        Schema schema = new Schema();
        schema.addColumn("type", Class.class, null);
        return schema;
    }

    public TupleSet getEdges() {
        return graph.getEdges();
    }

    public Graph getGraph() {
        return graph;
    }
}
