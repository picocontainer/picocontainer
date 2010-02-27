package org.picocontainer.converters;

import org.picocontainer.Converters;

import java.lang.reflect.Type;

/**
 * Null-object implementation of Converters
 */
public class ConvertsNothing implements Converters {
    public boolean canConvert(Type type) {
        return false;
    }

    public Object convert(String paramValue, Type type) {
        return null;
    }
}
