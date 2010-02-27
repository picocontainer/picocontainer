package org.picocontainer.converters;

import org.picocontainer.converters.Converter;

import java.io.File;

/**
 * Converts values to File data type objects
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
class FileConverter implements Converter<File> {

    public File convert(String paramValue) {
        return new File(paramValue);
    }
}
