package org.picocontainer.converters;

import org.picocontainer.converters.Converter;


/**
 * Converts values to 'double' data type objects
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
class DoubleConverter implements Converter<Double> {

    public Double convert(String paramValue) {
        return Double.valueOf(paramValue);
    }
}
