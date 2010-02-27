/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joe Walnes                                               *
 *****************************************************************************/


package org.picocontainer.script.util;

import org.picocontainer.PicoCompositionException;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class StringConversions {

    public interface StringConverter<T> {
        T convert(String in);
    }

    public static class InvalidConversionException extends PicoCompositionException {
        public InvalidConversionException(String message) {
        super(message);
    }
}

    private final Map<Class<?>, StringConverter<?>> converters = new HashMap<Class<?>, StringConverter<?>>();

    public StringConversions() {
        register(String.class, new StringConverter<String>() {
            public String convert(String in) {
                return in;
            }
        });

        register(Integer.class, new StringConverter<Integer>() {
            public Integer convert(String in) {
                return in == null ? 0 : Integer.valueOf(in);
            }
        });

        register(Long.class, new StringConverter<Long>() {
            public Long convert(String in) {
                return in == null ? (long) 0 : Long.valueOf(in);
            }
        });

        register(Boolean.class, new StringConverter<Boolean>() {
            public Boolean convert(String in) {
                if (in == null || in.length() == 0) {
                    return Boolean.FALSE;
                }
                char c = in.toLowerCase().charAt(0);
                return c == '1' || c == 'y' || c == 't' ? Boolean.TRUE : Boolean.FALSE;
            }
        });
    }

    public Object convertTo(Class<?> desiredClass, String inputString) {
        StringConverter<?> converter = converters.get(desiredClass);
        if (converter == null) {
            throw new InvalidConversionException("Cannot convert to type " + desiredClass.getName());
        }
        return converter.convert(inputString);
    }

    public void register(Class<?> type, StringConverter<?> converter) {
        converters.put(type, converter);
    }
}
