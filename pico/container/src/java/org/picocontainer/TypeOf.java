/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeOf<T> implements Type{

    private Type type;

    protected TypeOf() {
        type = getTypeFromSuperOfSubclass();
    }

    protected Type getTypeFromSuperOfSubclass() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        return type.getActualTypeArguments()[0];
    }

    public static <T> TypeOf<T> fromClass(Class<T> type) {
        TypeOf typeOf = new ClassType<T>();
        typeOf.type = type;
        return typeOf;
    }

    public Type getType() {
        return type;
    }

    private static class ClassType<T> extends TypeOf<T> {
        @Override
        protected Type getTypeFromSuperOfSubclass() {
            return null;
        }
    }



}
