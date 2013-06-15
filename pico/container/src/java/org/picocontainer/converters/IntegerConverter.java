package org.picocontainer.converters;



/**
 * Converts values to 'int' data type objects
 *
 * @author Paul Hammant
 * @author Michael Rimov
 */
class IntegerConverter implements Converter<Integer> {

    public Integer convert(final String paramValue) {
        return Integer.valueOf(paramValue);
    }
}
