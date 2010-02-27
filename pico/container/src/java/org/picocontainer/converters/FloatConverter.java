package org.picocontainer.converters;

import org.picocontainer.converters.Converter;


/**
 * Converts values to 'float' data type objects
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
class FloatConverter implements Converter<Float> {

    public Float convert(String paramValue) {
        return Float.valueOf(paramValue);
    }
}
