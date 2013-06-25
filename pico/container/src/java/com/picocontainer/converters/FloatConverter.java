package com.picocontainer.converters;



/**
 * Converts values to 'float' data type objects
 *
 * @author Paul Hammant
 * @author Michael Rimov
 */
class FloatConverter implements Converter<Float> {

    public Float convert(final String paramValue) {
        return Float.valueOf(paramValue);
    }
}
