/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.gems.monitors;

import java.lang.reflect.Constructor;

import com.picocontainer.gems.monitors.prefuse.ComponentDependencyListener;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.PicoContainer;
import com.picocontainer.monitors.AbstractComponentMonitor;

/**
 * Understands how to capture component dependency information from
 * picocontainer.
 *
 * @author Peter Barry
 * @author Kent R. Spillner
 */
@SuppressWarnings("serial")
public final class ComponentDependencyMonitor extends AbstractComponentMonitor {



	private final ComponentDependencyListener listener;

    public ComponentDependencyMonitor(final ComponentDependencyListener listener) {
        this.listener = listener;
    }

    @Override
	public <T> void instantiated(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                             final Constructor<T> constructor,
                             final Object instantiated,
                             final Object[] injected,
                             final long duration) {
        Class<?> componentType = instantiated.getClass();
        int count = injected.length;

        if (count == 0) {
            listener.addDependency(new Dependency(componentType, null));
        }

        for (int i = 0; i < count; i++) {
            Object dependent = injected[i];
            Dependency dependency = new Dependency(componentType, dependent.getClass());
            listener.addDependency(dependency);
        }
    }

    /**
     * Understands which other classes are required to instantiate a component.
     *
     * @author Peter Barry
     * @author Kent R. Spillner
     */
    public static final class Dependency {

        private final Class<?> componentType;

        private final Class<?> dependencyType;

        public Dependency(final Class<?> componentType, final Class<?> dependencyType) {
            this.componentType = componentType;
            this.dependencyType = dependencyType;
        }

        public boolean dependsOn(final Class<?> type) {
            return (type != null) && type.equals(dependencyType);
        }

        @Override
		public boolean equals(final Object other) {
            if (other instanceof Dependency) {
                Dependency otherDependency = (Dependency) other;
                return areEqualOrNull(componentType, otherDependency.componentType)
                        && areEqualOrNull(dependencyType, otherDependency.dependencyType);
            }
            return false;
        }

        public Class<?> getComponentType() {
            return componentType;
        }

        public Class<?> getDependencyType() {
            return dependencyType;
        }

        @Override
		public String toString() {
            return componentType + " depends on " + dependencyType;
        }

        private static boolean areEqualOrNull(final Class<?> type, final Class<?> otherType) {
            if (type != null) {
                return type.equals(otherType);
            }
            return (otherType == null);
        }
    }
}
