/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Paul Hammant                                             *
 *****************************************************************************/

package org.picocontainer.gems.monitors;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.AbstractComponentMonitor;

@SuppressWarnings("serial")
public final class DotDependencyGraphComponentMonitor extends AbstractComponentMonitor implements ComponentMonitor {

	
	final List<Instantiation> allInstantiated = new ArrayList<Instantiation>();

    public DotDependencyGraphComponentMonitor(final ComponentMonitor delegate) {
        super(delegate);
    }

    public DotDependencyGraphComponentMonitor() {
    }

    @Override
	public <T> void instantiated(final PicoContainer container, final ComponentAdapter<T> componentAdapter,
                             final Constructor<T> constructor,
                             final Object instantiated,
                             final Object[] injected,
                             final long duration) {

        this.allInstantiated.add(new Instantiation(constructor, instantiated, injected, duration));

        super.instantiated(container, componentAdapter, constructor, instantiated, injected, duration);
    }


    public String getClassDependencyGraph() {

        Set<String> lines = new HashSet<String>();

        for (Object anAllInstantiated : allInstantiated) {
            Instantiation instantiation = (Instantiation)anAllInstantiated;
            for (int j = 0; j < instantiation.getInjected().length; j++) {
                Object instantiated = instantiation.getInstantiated();
                Object injected = instantiation.getInjected()[j];
                lines.add(
                    "  '" + instantiated.getClass().getName() + "' -> '" + injected.getClass().getName() + "';\n");
            }
        }

        return sortLines(lines);
    }

    private String sortLines(final Set<String> lines) {
        List<String> list = new ArrayList<String>(lines);
        Collections.sort(list);

        String dependencies = "";
        for (Object aList : list) {
            String dep = (String)aList;
            dependencies = dependencies + dep;
        }
        return dependencies.replaceAll("'","\"");
    }

    public String getInterfaceDependencyGraph() {
        Set<String> lines = new HashSet<String>();

        for (Object anAllInstantiated : allInstantiated) {
            Instantiation instantiation = (Instantiation)anAllInstantiated;
            for (int j = 0; j < instantiation.getInjected().length; j++) {
                Object injected = instantiation.getInjected()[j];
                Class<?> injectedType = instantiation.getConstructor().getParameterTypes()[j];
                Object instantiated = instantiation.getInstantiated();
                if (injected.getClass() != injectedType) {
                    lines.add("  '" + instantiated.getClass().getName() + "' -> '" + injectedType.getName() +
                              "' [style=dotted,label='needs'];\n");
                    lines.add("  '" + injected.getClass().getName() + "' -> '" + injectedType.getName() +
                              "' [style=dotted, color=red,label='isA'];\n");
                    lines.add("  '" + injectedType.getName() + "' [shape=box, label=" + printClassName(injectedType) +
                              "];\n");
                } else {
                    lines.add("  '" + instantiated.getClass().getName() + "' -> '" + injected.getClass().getName() +
                              "' [label='needs'];\n");
                }
                lines.add("  '" + instantiated.getClass().getName() + "' [label=" +
                          printClassName(instantiated.getClass()) + "];\n");

            }
        }

        return sortLines(lines);
    }

    private String printClassName(final Class<?> clazz) {
        String className = clazz.getName();
        return "'" + className.substring(className.lastIndexOf(".")+1) + "\\n" + clazz.getPackage().getName() + "'";

    }

    private static final class Instantiation {
        final Constructor<?> constructor;
        final Object instantiated;
        final Object[] injected;
        final long duration;
        
        public Instantiation(final Constructor<?> constructor, final Object instantiated, final Object[] injected, final long duration) {
            this.constructor = constructor;
            this.instantiated = instantiated;
            this.injected = injected;
            this.duration = duration;
        }

        public Constructor<?> getConstructor() {
            return constructor;
        }

        public Object getInstantiated() {
            return instantiated;
        }
        public Object[] getInjected() {
            return injected;
        }
    }
}
