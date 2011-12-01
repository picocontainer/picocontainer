/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.gems.monitors.prefuse;

import java.util.HashSet;
import java.util.Set;

import org.picocontainer.gems.monitors.ComponentDependencyMonitor.Dependency;

/**
 * Understands non-duplicated dependencies.
 * 
 * @author Peter Barry
 * @author Kent R. Spillner
 */
public final class DependencySet implements ComponentDependencyListener {

    private final Set uniqueDependencies = new HashSet();

    private final ComponentDependencyListener listener;

    public DependencySet(final ComponentDependencyListener listener) {
        this.listener = listener;
    }

    public void addDependency(final Dependency dependency) {
        if (uniqueDependencies.add(dependency)) {
            listener.addDependency(dependency);
        }
    }

    public Dependency[] getDependencies() {
        return (Dependency[]) uniqueDependencies.toArray(new Dependency[uniqueDependencies.size()]);
    }
}
