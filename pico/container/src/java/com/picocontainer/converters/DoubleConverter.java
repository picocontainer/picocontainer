package com.picocontainer.converters;



/**
 * Converts values to 'double' data type objects
 *
 * @author Paul Hammant
 * @author Michael Rimov
 */
class DoubleConverter implements Converter<Double> {

    public Double convert(final String paramValue) {
        return Double.valueOf(paramValue);
    }
}
