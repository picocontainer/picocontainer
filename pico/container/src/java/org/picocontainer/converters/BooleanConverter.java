package org.picocontainer.converters;



/**
 * Converts values to 'boolean' data type objects
 *
 * @author Paul Hammant
 * @author Michael Rimov
 */
class BooleanConverter implements Converter<Boolean> {

    public Boolean convert(final String paramValue) {
        return Boolean.valueOf(paramValue);
    }
}
