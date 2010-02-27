package org.picocontainer.converters;

import org.picocontainer.converters.Converter;


/**
 * Converts values to 'long' data type objects
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
class LongConverter implements Converter<Long> {

    public Long convert(String paramValue) {
        return Long.valueOf(paramValue);
    }
}
