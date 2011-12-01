/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.script.xml;

public class AttributeUtils {
	
    public static final String EMPTY = "";

	
	public static boolean notSet(Object string) {
        return string == null || string.equals(EMPTY);
    }
    
    public static boolean isSet(Object string) {
        return !notSet(string);
    }

    public static boolean boolValue(String string, boolean defaultValue) {
        if (notSet(string)) {
            return defaultValue;
        }
        boolean aBoolean = Boolean.valueOf(string).booleanValue();
        return aBoolean;
    }
}
