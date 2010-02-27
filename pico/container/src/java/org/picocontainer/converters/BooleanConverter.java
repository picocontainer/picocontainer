package org.picocontainer.converters;

import org.picocontainer.converters.Converter;


/**
 * Converts values to 'boolean' data type objects
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
class BooleanConverter implements Converter<Boolean> {

    public Boolean convert(String paramValue) {
        return Boolean.valueOf(paramValue);
    }
}
