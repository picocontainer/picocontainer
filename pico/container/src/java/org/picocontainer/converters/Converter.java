/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.converters;

/**
 * Interface for type converters. 
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
public interface Converter<T> {
    
    /**
     * Performs a conversion between the given parameter value and the target type.
     * @param parameterValue the string value to convert.
     * @return the resulting object.
     */
    T convert(String parameterValue);

}
