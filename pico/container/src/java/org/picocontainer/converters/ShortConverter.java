package org.picocontainer.converters;


/**
 * Converts values to 'short' data type objects
 *
 * @author Paul Hammant
 * @author Michael Rimov
 */
class ShortConverter implements Converter<Short> {

    public Short convert(final String paramValue) {
        return Short.valueOf(paramValue);
    }
}
