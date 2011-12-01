/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer;

import java.lang.reflect.Type;

/**
 * A facade for a collection of converters that provides string-to-type conversions. 
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
public interface Converters {
    
    /**
     * Returns true if a converters is available to convert to the given object type
     * 
     * @param type the object Type to convert to
     * @return true if the type can be converted to
     */
    boolean canConvert(Type type);
    
    /**
     * Converts a particular string value into the target type
     * 
     * @param value the String value to convert
     * @param type the object Type to convert to
     * @return The converted Object instance
     */
    Object convert(String value, Type type);
}
