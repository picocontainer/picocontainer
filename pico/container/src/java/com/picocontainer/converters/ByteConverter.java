package com.picocontainer.converters;


/**
 * Converts values to 'byte' data type objects
 *
 * @author Paul Hammant
 * @author Michael Rimov
 */
class ByteConverter implements Converter<Byte> {

    public Byte convert(final String paramValue) {
        return Byte.valueOf(paramValue);
    }

}
