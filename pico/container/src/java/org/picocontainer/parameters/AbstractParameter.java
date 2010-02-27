/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.parameters;

import org.picocontainer.PicoContainer;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;

import java.lang.reflect.Type;
import java.lang.annotation.Annotation;

public abstract class AbstractParameter implements Parameter {
    
    @Deprecated
    public final Object resolveInstance(PicoContainer container, ComponentAdapter<?> forAdapter, Type expectedType, NameBinding expectedNameBinding, boolean useNames, Annotation binding) {
        return resolve(container, forAdapter, null, expectedType, expectedNameBinding, useNames, binding).resolveInstance();
    }

    @Deprecated
    public final boolean isResolvable(PicoContainer container, ComponentAdapter<?> forAdapter, Type expectedType, NameBinding expectedNameBinding, boolean useNames, Annotation binding) {
        return resolve(container, forAdapter, null, expectedType, expectedNameBinding, useNames, binding).isResolved();
    }
}
