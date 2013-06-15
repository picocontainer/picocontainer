package org.picocontainer.converters;

import java.io.File;

/**
 * Converts values to File data type objects
 *
 * @author Paul Hammant
 * @author Michael Rimov
 */
class FileConverter implements Converter<File> {

    public File convert(final String paramValue) {
        return new File(paramValue);
    }
}
