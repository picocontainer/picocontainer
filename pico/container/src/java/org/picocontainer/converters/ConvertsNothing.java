package org.picocontainer.converters;

import java.lang.reflect.Type;

import org.picocontainer.Converters;

/**
 * Null-object implementation of Converters
 */
public class ConvertsNothing implements Converters {
    public boolean canConvert(final Type type) {
        return false;
    }

    public Object convert(final String paramValue, final Type type) {
        return null;
    }
}
