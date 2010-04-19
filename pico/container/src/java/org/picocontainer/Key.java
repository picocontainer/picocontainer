/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/
package org.picocontainer;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/** @author Paul Hammant */
@SuppressWarnings("serial")
public class Key<T> implements Serializable {

	private final Class<T> type;

    public Key(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }

    public String toString() {
        return "K{" + getType().getName() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (!type.equals(key.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    public static <T> Key<T> annotatedKey(Class<T> type, Class<? extends Annotation> annotation) {
        return new AnKey<T>(type, annotation);
    }

    public static <T> Key<T> namedKey(Class<T> type, String name) {
        return new StrKey<T>(type, name);
    }

    public static class StrKey<T> extends Key<T> {
        private final String name;

        public StrKey(Class<T> type, String name) {
            super(type);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return "K{" + getType().getName() + ":" + name + "}";
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StrKey strKey = (StrKey) o;

            if (!name.equals(strKey.name)) return false;
            if (!getType().equals(strKey.getType())) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + getType().hashCode();
            return result;
        }
    }

    public static class AnKey<T> extends Key<T> {
        private final Class<? extends Annotation> annotation;

        public AnKey(Class<T> type, Class<? extends Annotation> annotation) {
            super(type);
            this.annotation = annotation;
        }

        public Class<? extends Annotation> getAnnotation() {
            return annotation;
        }

        public String toString() {
            return "K{" + getType().getName() + ":" + annotation.getName() + "}";
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key<?> key = (Key<?>)o;

            if (!annotation.equals(annotation)) return false;
            if (!getType().equals(key.type)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = getType().hashCode();
            result = 31 * result + annotation.hashCode();
            return result;
        }


    }

}
