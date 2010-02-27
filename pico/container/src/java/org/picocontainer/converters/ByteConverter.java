package org.picocontainer.converters;

import org.picocontainer.converters.Converter;

/**
 * Converts values to 'byte' data type objects
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
class ByteConverter implements Converter<Byte> {
    
    public Byte convert(String paramValue) {
        return Byte.valueOf(paramValue);
    }

}
