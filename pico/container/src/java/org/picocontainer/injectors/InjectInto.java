/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import java.lang.reflect.Type;

public class InjectInto implements Type {
    private Type intoType;
    private Object intoKey;

    public InjectInto(Type intoType, Object intoKey) {
        this.intoType = intoType;
        this.intoKey = intoKey;
    }

    public Type getIntoType() {
        return intoType;
    }

    // at FactoryInjector implementor's risk
    public Class getIntoClass() {
        return (Class) getIntoType();
    }

    public Object getIntoKey() {
        return intoKey;
    }
}
