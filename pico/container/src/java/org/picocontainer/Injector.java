/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer;

import java.lang.reflect.Type;

/**
 * Implementers are responsible for instantiating and injecting dependancies into
 * Constructors, Methods and Fields.
 */
public interface Injector<T> extends ComponentAdapter<T> {

    /**
     * A preexiting component instance can be injected into after instantiation
     *
     *
     * @param container the container that can provide injectable dependencies
     * @param into
     * @param instance the instance to
     * @return
     */
    Object decorateComponentInstance(PicoContainer container, Type into, T instance);

}
