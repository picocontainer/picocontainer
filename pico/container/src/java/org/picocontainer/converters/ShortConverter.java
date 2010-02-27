package org.picocontainer.converters;

import org.picocontainer.converters.Converter;

/**
 * Converts values to 'short' data type objects
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
class ShortConverter implements Converter<Short> {

    public Short convert(String paramValue) {
        return Short.valueOf(paramValue);
    }
}
