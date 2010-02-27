package org.picocontainer.converters;

import org.picocontainer.converters.Converter;


/**
 * Converts values to 'int' data type objects
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
class IntegerConverter implements Converter<Integer> {

    public Integer convert(String paramValue) {
        return Integer.valueOf(paramValue);
    }
}
