/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeOf<T> {

    public static final TypeOf INTEGER = TypeOf.fromClass(Integer.class);
    public static final TypeOf LONG = TypeOf.fromClass(Long.class);
    public static final TypeOf FLOAT = TypeOf.fromClass(Float.class);
    public static final TypeOf DOUBLE = TypeOf.fromClass(Double.class);
    public static final TypeOf BOOLEAN = TypeOf.fromClass(Boolean.class);
    public static final TypeOf CHARACTER = TypeOf.fromClass(Character.class);
    public static final TypeOf SHORT = TypeOf.fromClass(Short.class);
    public static final TypeOf BYTE = TypeOf.fromClass(Byte.class);
    public static final TypeOf VOID = TypeOf.fromClass(Void.TYPE);

    private Type type;

    protected TypeOf() {
        type = getTypeFromSuperOfSubclass();
    }

    protected Type getTypeFromSuperOfSubclass() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        return type.getActualTypeArguments()[0];
    }

    public static <T> TypeOf<T> fromClass(final Class<T> type) {
        TypeOf<T> typeOf = new ClassType<T>();
        typeOf.type = type;
        return typeOf;
    }

    public static TypeOf fromParameterizedType(final ParameterizedType parameterizedType) {
        TypeOf typeOf = new Dummy();
        typeOf.type = parameterizedType.getRawType();
        return typeOf;
    }

    public Type getType() {
        return type;
    }

    public boolean isPrimitive() {
        return type instanceof Class && ((Class) type).isPrimitive();
    }

    public String getName() {
        if (type instanceof Class) {
            return ((Class) type).getName();
        } else {
            return type.toString();
        }
    }

    public boolean isAssignableFrom(final Class<?> aClass) {
        if (type instanceof Class) {
            return ((Class) type).isAssignableFrom(aClass);
        }
        return false;
    }

    public boolean isAssignableTo(final Class aClass) {
        if (type instanceof Class) {
            return aClass.isAssignableFrom((Class<?>) type);
        }
        return false;
    }

    private static class ClassType<T> extends TypeOf<T> {
        @Override
        protected Type getTypeFromSuperOfSubclass() {
            return null;
        }
    }

    private static class Dummy<T> extends TypeOf<T> {
        @Override
        protected Type getTypeFromSuperOfSubclass() {
            return null;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
			return true;
		}
        if (!(o instanceof TypeOf)) {
			return false;
		}

        TypeOf typeOf = (TypeOf) o;

        if (type != null ? !type.equals(typeOf.type) : typeOf.type != null) {
			return false;
		}

        return true;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
