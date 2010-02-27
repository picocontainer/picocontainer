/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

@SuppressWarnings("serial")
public class NamedMethodInjector<T> extends SetterInjector<T> {

    private final boolean optional;

    public NamedMethodInjector(Object key,
                                   Class<?> impl,
                                   Parameter[] parameters,
                                   ComponentMonitor monitor,
                                   boolean optional) {
        this(key, impl, parameters, monitor, "set", optional);
    }

    public NamedMethodInjector(Object key,
                                   Class<?> impl,
                                   Parameter[] parameters,
                                   ComponentMonitor monitor) {
        this(key, impl, parameters, monitor, "set", true);
    }

    public NamedMethodInjector(Object key,
                               Class<?> impl,
                               Parameter[] parameters,
                               ComponentMonitor monitor,
                               String prefix) {
        this(key, impl, parameters, monitor, prefix, true);
    }

    public NamedMethodInjector(Object key,
                               Class<?> impl,
                               Parameter[] parameters,
                               ComponentMonitor monitor,
                               String prefix,
                               boolean optional) {
        super(key, impl, parameters, monitor, prefix, true);
        this.optional = optional;
    }

    @Override
    protected NameBinding makeParameterNameImpl(final AccessibleObject member) {
        return new NameBinding() {
            public String getName() {
                String name = ((Method)member).getName().substring(prefix.length()); // string off 'set' or chosen prefix
                return name.substring(0,1).toLowerCase() + name.substring(1);  // change "SomeThing" to "someThing" 
            }
        };
    }

    @Override
    protected void unsatisfiedDependencies(PicoContainer container, Set<Type> unsatisfiableDependencyTypes) {
        if (!optional) {
            super.unsatisfiedDependencies(container, unsatisfiableDependencyTypes);
        }
    }

    @Override
    public String getDescriptor() {
        return "NamedMethodInjection";
    }

}