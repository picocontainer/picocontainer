package org.picocontainer.converters;



/**
 * Converts values to 'long' data type objects
 *
 * @author Paul Hammant
 * @author Michael Rimov
 */
class LongConverter implements Converter<Long> {

    public Long convert(final String paramValue) {
        return Long.valueOf(paramValue);
    }
}
