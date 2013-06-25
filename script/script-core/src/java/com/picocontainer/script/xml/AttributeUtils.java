/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.script.xml;

public class AttributeUtils {

    public static final String EMPTY = "";


	public static boolean notSet(final Object string) {
        return string == null || string.equals(EMPTY);
    }

    public static boolean isSet(final Object string) {
        return !notSet(string);
    }

    public static boolean boolValue(final String string, final boolean defaultValue) {
        if (notSet(string)) {
            return defaultValue;
        }
        boolean aBoolean = Boolean.valueOf(string).booleanValue();
        return aBoolean;
    }
}
